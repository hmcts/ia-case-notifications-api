package uk.gov.hmcts.reform.iacasenotificationsapi.component;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.SEND_DIRECTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.APPEAL_SUBMITTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.testutils.fixtures.AsylumCaseCollectionForTest.someListOf;
import static uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.testutils.fixtures.PreSubmitCallbackResponseForTest;

public class SendsDirectionTest extends SpringBootIntegrationTest {

    private static final String UUID_PATTERN = "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}";
    private final String someNotificationId = UUID.randomUUID().toString();

    @Test
    public void sends_notification() {

        given.someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "caseworker-ia-caseofficer"))
            .id("1")
            .email("some-email@email.com")
            .forename("Case")
            .surname("Officer"));

        given.govNotifyWillHandleEmailNotificationAndReturnNotificationId(someNotificationId);

        PreSubmitCallbackResponseForTest response = iaCaseNotificationApiClient.aboutToSubmit(callback()
            .event(SEND_DIRECTION)
            .caseDetails(someCaseDetailsWith()
                .id(1)
                .state(APPEAL_SUBMITTED)
                .createdDate(LocalDateTime.now())
                .jurisdiction("IA")
                .caseData(anAsylumCase()
                    .with(HEARING_CENTRE, HearingCentre.MANCHESTER)
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, "legalrep@domain.com")
                    .with(DIRECTIONS, someListOf(Direction.class)
                        .with(new Direction(
                            "exp",
                            Parties.RESPONDENT,
                            "1980-04-12",
                            "1980-04-12",
                            DirectionTag.NONE,
                            Collections.emptyList()))))));

        then.govNotifyReceivesAnEmailNotificationRequest();

        Optional<List<IdValue<String>>> notificationsSent =
            response
                .getData()
                .read(NOTIFICATIONS_SENT);

        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(2);
        assertThat(notifications.get(0).getId()).contains("_RESPONDENT_NON_STANDARD_DIRECTION");
        assertThat(notifications.get(0).getValue()).matches(UUID_PATTERN);
    }
}
