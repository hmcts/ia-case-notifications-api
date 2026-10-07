package uk.gov.hmcts.reform.iacasenotificationsapi.component;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.AsylumCaseForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CallbackForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.APPEAL_SUBMITTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.*;


@Slf4j
@SuppressWarnings("unchecked")
public class StatutoryTimeframe24WeeksNotificationsTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
        WithNotificationEmailStub, WithIdamStub, WithUserDetailsStub, WithDocumentDownloadStub {

    public static final String APPELLANT_MAIL = "appellant@domain.com";
    public static final String APPELLANT_SMS = "07123456789";
    public static final String LR_EMAIL = "legalrep@domain.com";
    private static final String someNotificationId = UUID.randomUUID().toString();

    @MockitoBean
    private GovNotifyNotificationSender notificationSender;

    @MockitoBean
    private DateTimeExtractor dateTimeExtractor;

    @MockitoBean
    private HearingDetailsFinder hearingDetailsFinder;

    @Value("${govnotify.template.caseListed.homeOffice.email.nonAda}")
    private String homeOfficeCaseListedNonAdaTemplateId;

    @Value("${govnotify.template.caseListed.homeOffice.email.nonAdaStf24Weeks}")
    private String homeOfficeCaseListedNonAdaStf24WeeksTemplateId;

    // --- Test data builders / helpers ---
    private enum TestJourneyType {
        AIP_MANUAL, AIP, LR, LR_MANUAL
    }

    private static DocumentWithMetadata generateDocumentWithMetadata(int index, DocumentTag documentTag) {
        String filename = "filename-" + index;
        String url = "http://localhost:8080/dm-store/" + filename;
        String binaryUrl = url + "/binary";
        Document document = new Document(url, binaryUrl, filename);
        return new DocumentWithMetadata(document, "", "", documentTag, "");
    }

    private static AsylumCaseForTest mockCaseData(TestJourneyType testJourneyType,
                                                  boolean inCountry,
                                                  boolean wantsEmail,
                                                  boolean wantsSms,
                                                  boolean is24wCase) {
        List<IdValue<DocumentWithMetadata>> letterBundleDocuments = List.of(
                new IdValue<>("1", generateDocumentWithMetadata(1, DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE)),
                new IdValue<>("2", generateDocumentWithMetadata(2, DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER_LR_BUNDLE))
        );
        AsylumCaseForTest someCase = anAsylumCase()
                .with(HEARING_CENTRE, HearingCentre.MANCHESTER)
                .with(LIST_CASE_HEARING_CENTRE, HearingCentre.MANCHESTER)
                .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                .with(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, StatutoryTimeframe24WeeksNotificationsTest.LR_EMAIL)
                .with(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, APPEAL_SUBMITTED)
                .with(CONTACT_PREFERENCE, wantsEmail ? ContactPreference.WANTS_EMAIL : wantsSms ? ContactPreference.WANTS_SMS : null)
                .with(COMPLETE_CASE_REVIEW_DATE, "2002-02-02")
                .with(APPEAL_SUBMISSION_DATE, "2002-02-02")
                .with(TRIBUNAL_RECEIVED_DATE, "2002-02-02")
                .with(HOME_OFFICE_DECISION_DATE, "2002-02-02")
                .with(LETTER_BUNDLE_DOCUMENTS, letterBundleDocuments)
                .with(STF_24W_CURRENT_STATUS_AUTO_GENERATED, is24wCase ? YesOrNo.YES : YesOrNo.NO);

        switch (testJourneyType) {
            case AIP_MANUAL -> {
                someCase.with(IS_ADMIN, YesOrNo.YES)
                        .with(SUBSCRIPTIONS, buildSubscriptions(wantsEmail, wantsSms, StatutoryTimeframe24WeeksNotificationsTest.APPELLANT_MAIL, StatutoryTimeframe24WeeksNotificationsTest.APPELLANT_SMS))
                        .with(APPELLANTS_REPRESENTATION, YesOrNo.YES);
                if (inCountry) {
                    someCase.with(APPELLANT_IN_UK, YesOrNo.YES)
                            .with(AsylumCaseDefinition.APPELLANT_ADDRESS, new AddressUk("l1", "l2", "l3", "pt", "county", "pc", "uk"));
                } else {
                    someCase.with(APPELLANT_IN_UK, YesOrNo.NO)
                            .with(AsylumCaseDefinition.ADDRESS_LINE_1_ADMIN_J, "line1")
                            .with(AsylumCaseDefinition.ADDRESS_LINE_2_ADMIN_J, "line2")
                            .with(AsylumCaseDefinition.ADDRESS_LINE_3_ADMIN_J, "line3")
                            .with(AsylumCaseDefinition.ADDRESS_LINE_4_ADMIN_J, "line4")
                            .with(AsylumCaseDefinition.COUNTRY_GOV_UK_OOC_ADMIN_J, new NationalityFieldValue("GB"));
                }

            }
            case AIP -> {
                someCase.with(IS_ADMIN, YesOrNo.NO)
                        .with(SUBSCRIPTIONS, buildSubscriptions(wantsEmail, wantsSms, StatutoryTimeframe24WeeksNotificationsTest.APPELLANT_MAIL, StatutoryTimeframe24WeeksNotificationsTest.APPELLANT_SMS))
                        .with(JOURNEY_TYPE, JourneyType.AIP)
                        .with(APPELLANT_IN_UK, inCountry ? YesOrNo.YES : YesOrNo.NO);
            }
            case LR -> {
                someCase.with(IS_ADMIN, YesOrNo.NO)
                        .with(JOURNEY_TYPE, JourneyType.REP)
                        .with(LEGAL_REP_HAS_ADDRESS, inCountry ? YesOrNo.YES : YesOrNo.NO);
                buildContactPreference(someCase, wantsEmail, wantsSms);
            }
            case LR_MANUAL -> {
                someCase.with(IS_ADMIN, YesOrNo.YES)
                        .with(APPELLANTS_REPRESENTATION, YesOrNo.NO);
                buildContactPreference(someCase, wantsEmail, wantsSms);
                if (inCountry) {
                    someCase.with(APPELLANT_IN_UK, YesOrNo.YES)
                            .with(AsylumCaseDefinition.APPELLANT_ADDRESS, new AddressUk("l1", "l2", "l3", "pt", "county", "pc", "uk"));
                    someCase.with(LEGAL_REP_HAS_ADDRESS, YesOrNo.YES)
                            .with(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, new AddressUk("lrl1", "lrl2", "lrl2", "lrpt", "lrcounty", "lrpc", "lruk"));
                } else {
                    someCase.with(LEGAL_REP_HAS_ADDRESS, YesOrNo.NO)
                            .with(AsylumCaseDefinition.OOC_ADDRESS_LINE_1, "line1")
                            .with(AsylumCaseDefinition.OOC_ADDRESS_LINE_2, "line2")
                            .with(AsylumCaseDefinition.OOC_ADDRESS_LINE_3, "line3")
                            .with(AsylumCaseDefinition.OOC_ADDRESS_LINE_4, "line4")
                            .with(AsylumCaseDefinition.OOC_COUNTRY_LINE, "country")
                            .with(AsylumCaseDefinition.OOC_LR_COUNTRY_GOV_UK_ADMIN_J, new NationalityFieldValue("GB"));

                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + testJourneyType);
        }
        return someCase;
    }

    private static void buildContactPreference(AsylumCaseForTest someCase, boolean wantsEmail, boolean wantsSms) {
        if (wantsEmail) {
            someCase
                    .with(EMAIL, APPELLANT_MAIL)
                    .with(CONTACT_PREFERENCE, ContactPreference.WANTS_EMAIL);
        } else if (wantsSms) {
            someCase
                    .with(MOBILE_NUMBER, APPELLANT_SMS)
                    .with(CONTACT_PREFERENCE, ContactPreference.WANTS_SMS);
        }
    }

    private static @NotNull Optional<List<IdValue<Subscriber>>> buildSubscriptions(boolean wantsEmail, boolean wantsSms, String appellantEmail, String appellantSms) {
        Subscriber subscriber = new Subscriber(
                SubscriberType.APPELLANT,
                appellantEmail,
                wantsEmail ? YesOrNo.YES : YesOrNo.NO,
                appellantSms,
                wantsSms ? YesOrNo.YES : YesOrNo.NO
        );

        return Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber)));
    }

    // --- Notification assertion helpers to eliminate duplication ---
    private static Optional<List<IdValue<String>>> readNotifications(PreSubmitCallbackResponseForTest response) {
        return response.getData().read(NOTIFICATIONS_SENT);
    }

    private static void assertNotificationsContain(PreSubmitCallbackResponseForTest response, Set<String> expectedIds) {
        List<IdValue<String>> notifications = readNotifications(response).orElse(Collections.emptyList());
        Pattern trailingTimestamp = Pattern.compile("_[0-9]{13}");
        Set<String> actualSet = notifications.stream()
                .map(IdValue::getId)
                .map(id -> trailingTimestamp.matcher(id).replaceAll(""))
                .map(s -> s.replace("1", ""))
                .collect(Collectors.toSet());

        HashSet<String> expectedSet = new HashSet<>(expectedIds);
        HashSet<String> unexpected = new HashSet<>(actualSet);
        if (expectedIds.size() != unexpected.size()) {
            fail("Notification IDs do not match. Expected: " + expectedIds + ", but got: " + actualSet);
        }
        unexpected.removeAll(expectedSet);
        if (!unexpected.isEmpty()) {
            log.error("Actual  : {}    ", actualSet);
            log.error("Expected: {}    ", expectedSet);
            fail("Missed notification ids: " + unexpected);
        }
    }

    private PreSubmitCallbackResponseForTest mockResponse(AsylumCaseForTest caseData, Event event) {
        addServiceAuthStub(server);
        addNotificationEmailStub(server);
        addIdamTokenStub(server);
        addUserInfoStub(server);
        addCaseWorkerUserDetailsStub(server);
        addDocumentDownloadStub(server);

        when(notificationSender.sendEmail(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
                .thenReturn(someNotificationId);

        when(notificationSender.sendLetter(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
                .thenReturn(someNotificationId);

        when(notificationSender.sendPrecompiledLetter(anyString(), any()))
                .thenReturn(someNotificationId);

        when(notificationSender.sendSms(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
                .thenReturn(someNotificationId);

        when(hearingDetailsFinder.getHearingDateTime(Mockito.any(AsylumCase.class))).thenReturn("2002-02-02T12:00:00");
        when(dateTimeExtractor.extractHearingDate(Mockito.anyString())).thenReturn(String.valueOf(LocalDateTime.of(2002, 2, 2, 12, 0)));
        when(dateTimeExtractor.extractHearingTime(Mockito.anyString())).thenReturn("12:00");
        when(hearingDetailsFinder.getHearingCentreAddress(Mockito.any(AsylumCase.class))).thenReturn("Hearing Centre Address");
        when(hearingDetailsFinder.getHearingCentreLocation(Mockito.any(AsylumCase.class))).thenReturn("Hearing Centre Address");

        return aboutToSubmit(callback()
                .event(event)
                .caseDetails(someCaseDetailsWith()
                        .state(APPEAL_SUBMITTED)
                        .caseData(caseData)));
    }

    private PreSubmitCallbackResponseForTest aboutToSubmit(CallbackForTest.CallbackForTestBuilder callback) {
        try {
            MvcResult response = mockMvc
                    .perform(
                            post("/asylum/ccdAboutToSubmit")
                                    .header("Authorization", USER_JWT_TOKEN)
                                    .header("ServiceAuthorization", SERVICE_JWT_TOKEN)
                                    .content(objectMapper.writeValueAsString(callback.build()))
                                    .contentType(APPLICATION_JSON_VALUE)
                    )
                    .andReturn();

            return objectMapper.readValue(
                    response.getResponse().getContentAsString(),
                    PreSubmitCallbackResponseForTest.class
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // --- Scenarios ---
    private static Stream<Arguments> remove24wCaseDataPermutations() {
        return Stream.of(
                Arguments.of(TestJourneyType.AIP_MANUAL, false, false, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, "_STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE")),
                Arguments.of(TestJourneyType.AIP_MANUAL, false, true, true, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, "_STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE")),
                Arguments.of(TestJourneyType.AIP_MANUAL, true, false, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, "_STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE")),
                Arguments.of(TestJourneyType.AIP_MANUAL, true, true, true, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, "_STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE")),

                Arguments.of(TestJourneyType.AIP, false, false, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
                Arguments.of(TestJourneyType.AIP, false, false, true, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
                Arguments.of(TestJourneyType.AIP, false, true, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
                Arguments.of(TestJourneyType.AIP, false, true, true, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
                Arguments.of(TestJourneyType.AIP, true, false, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
                Arguments.of(TestJourneyType.AIP, true, false, true, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
                Arguments.of(TestJourneyType.AIP, true, true, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
                Arguments.of(TestJourneyType.AIP, true, true, true, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),

                Arguments.of(TestJourneyType.LR, false, false, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
                Arguments.of(TestJourneyType.LR, false, false, true, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS)),
                Arguments.of(TestJourneyType.LR, false, true, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL)),
                Arguments.of(TestJourneyType.LR, true, false, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
                Arguments.of(TestJourneyType.LR, true, false, true, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS)),
                Arguments.of(TestJourneyType.LR, true, true, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL)),

                Arguments.of(TestJourneyType.LR_MANUAL, false, false, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, "_STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE", "_STF_24WEEKS_REMOVAL_DECISION_LETTER_LR_BUNDLE")),
                Arguments.of(TestJourneyType.LR_MANUAL, false, false, true, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, "_STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE", "_STF_24WEEKS_REMOVAL_DECISION_LETTER_LR_BUNDLE")),
                Arguments.of(TestJourneyType.LR_MANUAL, false, true, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, "_STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE", "_STF_24WEEKS_REMOVAL_DECISION_LETTER_LR_BUNDLE")),
                Arguments.of(TestJourneyType.LR_MANUAL, true, false, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, "_STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE", "_STF_24WEEKS_REMOVAL_DECISION_LETTER_LR_BUNDLE")),
                Arguments.of(TestJourneyType.LR_MANUAL, true, false, true, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, "_STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE", "_STF_24WEEKS_REMOVAL_DECISION_LETTER_LR_BUNDLE")),
                Arguments.of(TestJourneyType.LR_MANUAL, true, true, false, Set.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, "_STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE", "_STF_24WEEKS_REMOVAL_DECISION_LETTER_LR_BUNDLE"))
        );
    }

    private static Stream<Arguments> completeCaseReviewCaseDataPermutations() {
        return Stream.of(
                Arguments.of(true, TestJourneyType.AIP_MANUAL, false, false, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER)),
                Arguments.of(true, TestJourneyType.AIP_MANUAL, false, true, true, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER)),
                Arguments.of(true, TestJourneyType.AIP_MANUAL, true, false, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER)),
                Arguments.of(true, TestJourneyType.AIP_MANUAL, true, true, true, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER)),

                Arguments.of(true, TestJourneyType.AIP, false, false, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
                Arguments.of(true, TestJourneyType.AIP, false, false, true, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS)),
                Arguments.of(true, TestJourneyType.AIP, false, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
                Arguments.of(true, TestJourneyType.AIP, false, true, true, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS)),
                Arguments.of(true, TestJourneyType.AIP, true, false, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
                Arguments.of(true, TestJourneyType.AIP, true, false, true, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS)),
                Arguments.of(true, TestJourneyType.AIP, true, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
                Arguments.of(true, TestJourneyType.AIP, true, true, true, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS)),

                Arguments.of(true, TestJourneyType.LR, false, false, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
                Arguments.of(true, TestJourneyType.LR, false, false, true, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS)),
                Arguments.of(true, TestJourneyType.LR, false, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_EMAIL)),
                Arguments.of(true, TestJourneyType.LR, true, false, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
                Arguments.of(true, TestJourneyType.LR, true, false, true, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS)),
                Arguments.of(true, TestJourneyType.LR, true, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_EMAIL)),

                Arguments.of(true, TestJourneyType.LR_MANUAL, false, false, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER)),
                Arguments.of(true, TestJourneyType.LR_MANUAL, false, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER)),
                Arguments.of(true, TestJourneyType.LR_MANUAL, false, false, true, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER)),
                Arguments.of(true, TestJourneyType.LR_MANUAL, true, false, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_LETTER)),
                Arguments.of(true, TestJourneyType.LR_MANUAL, true, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_LETTER)),
                Arguments.of(true, TestJourneyType.LR_MANUAL, true, false, true, Set.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_LETTER)),

                Arguments.of(false, TestJourneyType.AIP_MANUAL, true, true, true, Collections.emptySet()),
                Arguments.of(false, TestJourneyType.AIP, true, true, true, Collections.emptySet()),
                Arguments.of(false, TestJourneyType.LR, true, true, true, Collections.emptySet()),
                Arguments.of(false, TestJourneyType.LR_MANUAL, true, true, true, Collections.emptySet())
        );
    }

    // --- Tests ---
    @ParameterizedTest(name = "JourneyType: {0}, inCountry: {1}, wantsEmail: {2}, wantsSms: {3}")
    @MethodSource("remove24wCaseDataPermutations")
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void should_send_24weeks_remove_notifications_correctly(TestJourneyType testJourneyType,
                                                            boolean inCountry,
                                                            boolean wantsEmail,
                                                            boolean wantsSms,
                                                            Set<String> expectedIds) {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(testJourneyType, inCountry, wantsEmail, wantsSms, true), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        assertNotificationsContain(response, expectedIds);
    }

    @ParameterizedTest
    @MethodSource("caseListedTemplatePermutations")
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void should_send_home_office_case_listed_notification_with_correct_template(boolean is24wCase) {
        PreSubmitCallbackResponseForTest response = mockResponse(
                mockCaseData(TestJourneyType.AIP, true, true, false, is24wCase),
                LIST_CASE
        );

        assertNotificationsContain(response, Set.of(
                "_CASE_LISTED_CASE_OFFICER",
                "_CASE_LISTED_HOME_OFFICE",
                "_CASE_LISTED_AIP_APPELLANT_EMAIL"
        ));
        Mockito.verify(notificationSender).sendEmail(
                Mockito.eq(is24wCase
                        ? homeOfficeCaseListedNonAdaStf24WeeksTemplateId
                        : homeOfficeCaseListedNonAdaTemplateId),
                Mockito.anyString(),
                Mockito.anyMap(),
                Mockito.anyString(),
                Mockito.any(Callback.class)
        );
    }

    private static Stream<Arguments> caseListedTemplatePermutations() {
        return Stream.of(
                Arguments.of(true),
                Arguments.of(false)
        );
    }

    @ParameterizedTest(name = "Is 24w case: {0}, JourneyType: {1}, inCountry: {2}, wantsEmail: {3}, wantsSms: {4}")
    @MethodSource("completeCaseReviewCaseDataPermutations")
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_complete_case_review_notifications_correctly(boolean is24w,
                                                                  TestJourneyType testJourneyType,
                                                                  boolean inCountry,
                                                                  boolean wantsEmail,
                                                                  boolean wantsSms,
                                                                  Set<String> expectedIds) {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(testJourneyType, inCountry, wantsEmail, wantsSms, is24w), COMPLETE_CASE_REVIEW);
        assertNotificationsContain(response, expectedIds);
    }

    @ParameterizedTest(name = "Is 24w case: {0}, JourneyType: {1}, inCountry: {2}, wantsEmail: {3}, wantsSms: {4}")
    @MethodSource("reviewHearingRequirementsCaseDataPermutations")
    @WithMockUser(authorities = {"tribunal-caseworker"})
    void should_send_review_hearing_requirements_notifications_correctly(boolean is24w,
                                                                         TestJourneyType testJourneyType,
                                                                         boolean inCountry,
                                                                         boolean wantsEmail,
                                                                         boolean wantsSms,
                                                                         Set<String> expectedIds) {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(testJourneyType, inCountry, wantsEmail, wantsSms, is24w), REVIEW_HEARING_REQUIREMENTS);
        assertNotificationsContain(response, expectedIds);
    }

    private static Stream<Arguments> reviewHearingRequirementsCaseDataPermutations() {
        return Stream.of(
                Arguments.of(true, TestJourneyType.AIP, true, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_APPELLANT_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_HOME_OFFICE_EMAIL)),
                Arguments.of(true, TestJourneyType.LR, true, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_LR_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_HOME_OFFICE_EMAIL)),
                Arguments.of(true, TestJourneyType.AIP_MANUAL, true, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_APPELLANT_LETTER, STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_HOME_OFFICE_EMAIL)),
                Arguments.of(true, TestJourneyType.LR_MANUAL, true, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_LR_LETTER, STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_HOME_OFFICE_EMAIL)),

                Arguments.of(false, TestJourneyType.AIP_MANUAL, true, true, true, Set.of("_REVIEW_HEARING_REQUIREMENTS_ADMIN_OFFICER")),
                Arguments.of(false, TestJourneyType.AIP, true, true, true, Set.of("_REVIEW_HEARING_REQUIREMENTS_ADMIN_OFFICER")),
                Arguments.of(false, TestJourneyType.LR, true, true, true, Set.of("_REVIEW_HEARING_REQUIREMENTS_ADMIN_OFFICER")),
                Arguments.of(false, TestJourneyType.LR_MANUAL, true, true, true, Set.of("_REVIEW_HEARING_REQUIREMENTS_ADMIN_OFFICER"))
        );
    }

    @ParameterizedTest(name = "Is 24w case: {0}, JourneyType: {1}, inCountry: {2}, wantsEmail: {3}, wantsSms: {4}")
    @MethodSource("uploadAdditionalEvidenceCaseDataPermutations")
    @WithMockUser(authorities = {"hearing-centre-admin"})
    void should_send_upload_additional_evidence_notifications_correctly(boolean is24w,
                                                                        TestJourneyType testJourneyType,
                                                                        boolean inCountry,
                                                                        boolean wantsEmail,
                                                                        boolean wantsSms,
                                                                        Set<String> expectedIds) {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(testJourneyType, inCountry, wantsEmail, wantsSms, is24w), UPLOAD_ADDITIONAL_EVIDENCE);
        assertNotificationsContain(response, expectedIds);
    }

    private static Stream<Arguments> uploadAdditionalEvidenceCaseDataPermutations() {
        return Stream.of(
                Arguments.of(true, TestJourneyType.AIP, true, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE_EMAIL)),
                Arguments.of(true, TestJourneyType.AIP_MANUAL, true, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_APPELLANT_LETTER, STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE_EMAIL)),
                Arguments.of(true, TestJourneyType.LR, true, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE_EMAIL)),
                Arguments.of(true, TestJourneyType.LR_MANUAL, true, true, false, Set.of(STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_LR_LETTER, STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE_EMAIL)),

                Arguments.of(false, TestJourneyType.AIP_MANUAL, true, true, true, Set.of("_UPLOADED_ADDITIONAL_EVIDENCE_HOME_OFFICE")),
                Arguments.of(false, TestJourneyType.AIP, true, true, true, Set.of("_UPLOADED_ADDITIONAL_EVIDENCE_AIP_APPELLANT_SMS", "_UPLOADED_ADDITIONAL_EVIDENCE_AIP_APPELLANT_EMAIL", "_UPLOADED_ADDITIONAL_EVIDENCE_HOME_OFFICE")),
                Arguments.of(false, TestJourneyType.LR, true, true, true, Set.of("_UPLOADED_ADDITIONAL_EVIDENCE_HOME_OFFICE")),
                Arguments.of(false, TestJourneyType.LR_MANUAL, true, true, true, Set.of("_UPLOADED_ADDITIONAL_EVIDENCE_HOME_OFFICE"))
        );
    }
}