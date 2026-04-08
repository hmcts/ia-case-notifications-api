package uk.gov.hmcts.reform.iacasenotificationsapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.AsylumCaseCollectionForTest.someListOf;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.COMPLETE_CASE_REVIEW_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.INTERNAL_APPELLANT_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.SEND_DIRECTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.APPEAL_SUBMITTED;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.WithNotificationEmailStub;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CallbackForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

@Slf4j
@SuppressWarnings("unchecked")
class SendsDirectionTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
    WithNotificationEmailStub {

    private static final String someNotificationId = UUID.randomUUID().toString();
    private static final String UUID_PATTERN =
        "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}";

    private static final String REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_HOME_OFFICE_EMAIL = "REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL";
    private static final String REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_APPELLANT_EMAIL = "REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL";
    private static final String REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_APPELLANT_LETTER = "REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER";
    private static final String REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_LEGAL_REP_EMAIL = "REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL";

    @MockBean
    private GovNotifyNotificationSender notificationSender;

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void sends_notification() {

        addServiceAuthStub(server);
        addNotificationEmailStub(server);

        when(notificationSender.sendEmail(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
            .thenReturn(someNotificationId);

        PreSubmitCallbackResponseForTest response = aboutToSubmit(callback()
            .event(SEND_DIRECTION)
            .caseDetails(someCaseDetailsWith()
                .state(APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(HEARING_CENTRE, HearingCentre.MANCHESTER)
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, "legalrep@domain.com")
                    .with(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, APPEAL_SUBMITTED)
                    .with(DIRECTIONS, someListOf(Direction.class)
                        .with(new Direction(
                            "exp",
                            Parties.RESPONDENT,
                            "1980-04-12",
                            "1980-04-12",
                            DirectionTag.NONE,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            UUID.randomUUID().toString(),
                            "someDirectionType")
                        )))));

        Optional<List<IdValue<String>>> notificationsSent =
            response
                .getData()
                .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(2);
        assertThat(notifications.get(0).getId()).contains("_RESPONDENT_NON_STANDARD_DIRECTION");
        assertThat(notifications.get(0).getValue()).matches(UUID_PATTERN);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_email_to_all_three_users() {
        PreSubmitCallbackResponseForTest response = mockResponse("legalrep@domain.com", "appellant@domain.com", null);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();
        assertThat(notifications.size()).isEqualTo(3);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_APPELLANT_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_LEGAL_REP_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_HOME_OFFICE_EMAIL);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_letter_to_appellant_and_email_to_legal_rep_and_home_office() {
        PreSubmitCallbackResponseForTest response = mockResponse("legalrep@domain.com", null, null);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(3);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_LEGAL_REP_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_HOME_OFFICE_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_APPELLANT_LETTER);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_letter_to_appellant_and_legal_rep_and_home_office_when_appellant_emails_are_empty() {
        PreSubmitCallbackResponseForTest response = mockResponse("legalrep@domain.com", "", "");
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(3);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_LEGAL_REP_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_HOME_OFFICE_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_APPELLANT_LETTER);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_email_to_appellant_and_ho_office() {
        PreSubmitCallbackResponseForTest response = mockResponse(null, "appellant@domain.com", null);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(2);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_APPELLANT_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_HOME_OFFICE_EMAIL);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_email_to_appellant_with_internal_email_and_ho_office() {
        PreSubmitCallbackResponseForTest response = mockResponse(null, null, "appellant@domain.com");
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);
        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(2);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_APPELLANT_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_HOME_OFFICE_EMAIL);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_letter_to_appellant_and_email_to_ho_office() {
        PreSubmitCallbackResponseForTest response = mockResponse(null, null, null);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(2);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_HOME_OFFICE_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS_APPELLANT_LETTER);
    }

    private PreSubmitCallbackResponseForTest mockResponse(String lrEmail, String appellantEmail, String internalAppellantEmail) {
        addServiceAuthStub(server);
        addNotificationEmailStub(server);
        when(notificationSender.sendEmail(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
                .thenReturn(someNotificationId);

        when(notificationSender.sendLetter(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
                .thenReturn(someNotificationId);

        return aboutToSubmit(callback()
                .event(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS)
                .caseDetails(someCaseDetailsWith()
                        .state(APPEAL_SUBMITTED)
                        .caseData(anAsylumCase()
                                .with(HEARING_CENTRE, HearingCentre.MANCHESTER)
                                .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                                .with(EMAIL, appellantEmail)
                                .with(INTERNAL_APPELLANT_EMAIL, internalAppellantEmail)
                                .with(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, lrEmail)
                                .with(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, APPEAL_SUBMITTED)
                                .with(SUBSCRIPTIONS, anySubscriptions())
                                .with(COMPLETE_CASE_REVIEW_DATE, "2002-02-02")
                                .with(APPELLANT_IN_UK, YesOrNo.YES)
                                .with(APPELLANTS_REPRESENTATION,  YesOrNo.YES)
                                .with(AsylumCaseDefinition.APPELLANT_ADDRESS, new AddressUk("l1", "l2", "l2", "pt", "county", "pc", "uk"))
                        )));
    }

    private static @NotNull Optional<List<IdValue<Subscriber>>> anySubscriptions() {
        Subscriber subscriber = new Subscriber(
                SubscriberType.APPELLANT, //subscriberType
                "ppellantInSubscription@gmail.com", //email
                YesOrNo.YES, // wants email
                "", //mobileNumber
                YesOrNo.NO // wants sms
        );

        return Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber)));
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
            // test will fail
            throw new RuntimeException(e);
        }
    }
}
