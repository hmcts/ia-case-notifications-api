package uk.gov.hmcts.reform.iacasenotificationsapi.component;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.WithNotificationEmailStub;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.AsylumCaseForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CallbackForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.COMPLETE_CASE_REVIEW_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.COMPLETE_CASE_REVIEW;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.APPEAL_SUBMITTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL;

@Slf4j
@SuppressWarnings("unchecked")
public class StatutoryTimeframe24WeeksNotificationsTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
    WithNotificationEmailStub {

    public static final String APPELLANT_MAIL = "appellant@domain.com";
    public static final String APPELLANT_SMS = "07123456789";
    public static final String LR_EMAIL = "legalrep@domain.com";
    private static final String someNotificationId = UUID.randomUUID().toString();

    @MockitoBean
    private GovNotifyNotificationSender notificationSender;

    // --- Test data builders / helpers ---
    private enum TestJourneyType {
        AIP_MANUAL, AIP, LR, LR_MANUAL
    }

    private static AsylumCaseForTest mockCaseData(TestJourneyType testJourneyType,
                                                  boolean inCountry,
                                                  boolean wantsEmail,
                                                  boolean wantsSms,
                                                  boolean is24wCase) {
        AsylumCaseForTest someCase = anAsylumCase()
            .with(HEARING_CENTRE, HearingCentre.MANCHESTER)
            .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
            .with(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, StatutoryTimeframe24WeeksNotificationsTest.LR_EMAIL)
            .with(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, APPEAL_SUBMITTED)
            .with(CONTACT_PREFERENCE, wantsEmail ? ContactPreference.WANTS_EMAIL : wantsSms ? ContactPreference.WANTS_SMS : null)
            .with(COMPLETE_CASE_REVIEW_DATE, "2002-02-02")
            .with(APPEAL_SUBMISSION_DATE, "2002-02-02")
            .with(TRIBUNAL_RECEIVED_DATE, "2002-02-02")
            .with(HOME_OFFICE_DECISION_DATE, "2002-02-02")
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

    private static void assertNotificationsContain(PreSubmitCallbackResponseForTest response, int expectedSize, List<String> expectedIds) {
        List<IdValue<String>> notifications = readNotifications(response).orElse(Collections.emptyList());
        String idsString = String.join(",", notifications.stream().map(IdValue::getId).toList());
        assertEquals(expectedSize, notifications.size());

        for (String id : expectedIds) {
            assertTrue(idsString.contains(id));
        }
    }

    private PreSubmitCallbackResponseForTest mockResponse(AsylumCaseForTest caseData, Event event) {
        addServiceAuthStub(server);
        addNotificationEmailStub(server);

        when(notificationSender.sendEmail(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
            .thenReturn(someNotificationId);

        when(notificationSender.sendLetter(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
            .thenReturn(someNotificationId);

        when(notificationSender.sendSms(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
            .thenReturn(someNotificationId);

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
            Arguments.of(TestJourneyType.AIP_MANUAL, false, false, false, 2, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.AIP_MANUAL, false, true, true, 2, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.AIP_MANUAL, true, false, false, 2, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.AIP_MANUAL, true, true, true, 2, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),

            Arguments.of(TestJourneyType.AIP, false, false, false, 1, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.AIP, false, false, true, 2, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.AIP, false, true, false, 2, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.AIP, false, true, true, 3, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.AIP, true, false, false, 1, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.AIP, true, false, true, 2, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.AIP, true, true, false, 2, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.AIP, true, true, true, 3, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),

            Arguments.of(TestJourneyType.LR, false, false, false, 2, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.LR, false, false, true, 3, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS)),
            Arguments.of(TestJourneyType.LR, false, true, false, 3, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL)),
            Arguments.of(TestJourneyType.LR, true, false, false, 2, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.LR, true, false, true, 3, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS)),
            Arguments.of(TestJourneyType.LR, true, true, false, 3, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL)),

            Arguments.of(TestJourneyType.LR_MANUAL, false, false, false, 1, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.LR_MANUAL, false, false, true, 1, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.LR_MANUAL, false, true, false, 1, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.LR_MANUAL, true, false, false, 1, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.LR_MANUAL, true, false, true, 1, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL)),
            Arguments.of(TestJourneyType.LR_MANUAL, true, true, false, 1, List.of(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL))
        );
    }

    private static Stream<Arguments> completeCaseReviewCaseDataPermutations() {
        return Stream.of(
            Arguments.of(true, TestJourneyType.AIP_MANUAL, false, false, false, 2, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER)),
            Arguments.of(true, TestJourneyType.AIP_MANUAL, false, true, true, 2, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER)),
            Arguments.of(true, TestJourneyType.AIP_MANUAL, true, false, false, 2, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER)),
            Arguments.of(true, TestJourneyType.AIP_MANUAL, true, true, true, 2, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER)),

            Arguments.of(true, TestJourneyType.AIP, false, false, false, 1, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
            Arguments.of(true, TestJourneyType.AIP, false, false, true, 2, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS)),
            Arguments.of(true, TestJourneyType.AIP, false, true, false, 2, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
            Arguments.of(true, TestJourneyType.AIP, false, true, true, 3, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS)),
            Arguments.of(true, TestJourneyType.AIP, true, false, false, 1, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
            Arguments.of(true, TestJourneyType.AIP, true, false, true, 2, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS)),
            Arguments.of(true, TestJourneyType.AIP, true, true, false, 2, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
            Arguments.of(true, TestJourneyType.AIP, true, true, true, 3, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS)),

            Arguments.of(true, TestJourneyType.LR, false, false, false, 2, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
            Arguments.of(true, TestJourneyType.LR, false, false, true, 3, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS)),
            Arguments.of(true, TestJourneyType.LR, false, true, false, 4, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL)),
            Arguments.of(true, TestJourneyType.LR, true, false, false, 2, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
            Arguments.of(true, TestJourneyType.LR, true, false, true, 3, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS)),
            Arguments.of(true, TestJourneyType.LR, true, true, false, 4, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL)),

            Arguments.of(true, TestJourneyType.LR_MANUAL, false, false, false, 1, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
            Arguments.of(true, TestJourneyType.LR_MANUAL, false, true, false, 1, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
            Arguments.of(true, TestJourneyType.LR_MANUAL, false, false, true, 1, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
            Arguments.of(true, TestJourneyType.LR_MANUAL, true, false, false, 1, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
            Arguments.of(true, TestJourneyType.LR_MANUAL, true, true, false, 1, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),
            Arguments.of(true, TestJourneyType.LR_MANUAL, true, false, true, 1, List.of(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL)),

            Arguments.of(false, TestJourneyType.AIP_MANUAL, true, true, true, 0, Collections.emptyList()),
            Arguments.of(false, TestJourneyType.AIP, true, true, true, 0, Collections.emptyList()),
            Arguments.of(false, TestJourneyType.LR, true, true, true, 0, Collections.emptyList()),
            Arguments.of(false, TestJourneyType.LR_MANUAL, true, true, true, 0, Collections.emptyList())
        );
    }

    // --- Tests ---
    @ParameterizedTest(name = "JourneyType: {0}, inCountry: {1}, wantsEmail: {2}, wantsSms: {3}")
    @MethodSource("remove24wCaseDataPermutations")
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_notifications_correctly(TestJourneyType testJourneyType,
                                                            boolean inCountry,
                                                            boolean wantsEmail,
                                                            boolean wantsSms,
                                                            int expectedSize,
                                                            List<String> expectedIds) {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(testJourneyType, inCountry, wantsEmail, wantsSms, true), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        assertNotificationsContain(response, expectedSize, expectedIds);
    }

    @ParameterizedTest(name = "Is 24w case: {0}, JourneyType: {1}, inCountry: {2}, wantsEmail: {3}, wantsSms: {4}")
    @MethodSource("completeCaseReviewCaseDataPermutations")
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_complete_case_review_notifications_correctly(boolean is24w,
                                                                  TestJourneyType testJourneyType,
                                                                  boolean inCountry,
                                                                  boolean wantsEmail,
                                                                  boolean wantsSms,
                                                                  int expectedSize,
                                                                  List<String> expectedIds) {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(testJourneyType, inCountry, wantsEmail, wantsSms, is24w), COMPLETE_CASE_REVIEW);
        assertNotificationsContain(response, expectedSize, expectedIds);
    }
}