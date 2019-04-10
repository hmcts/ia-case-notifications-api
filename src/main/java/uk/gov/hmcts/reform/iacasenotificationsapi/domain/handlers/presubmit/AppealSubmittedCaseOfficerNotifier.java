package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.CaseOfficerPersonalisationFactory;

@Component
public class AppealSubmittedCaseOfficerNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final String appealSubmittedCaseOfficerTemplateId;
    private final CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory;
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;
    private final NotificationSender notificationSender;

    public AppealSubmittedCaseOfficerNotifier(
        @Value("${govnotify.template.appealSubmittedCaseOfficer}") String appealSubmittedCaseOfficerTemplateId,
        CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory,
        Map<HearingCentre, String> hearingCentreEmailAddresses,
        NotificationSender notificationSender
    ) {
        requireNonNull(appealSubmittedCaseOfficerTemplateId, "appealSubmittedCaseOfficerTemplateId must not be null");

        this.appealSubmittedCaseOfficerTemplateId = appealSubmittedCaseOfficerTemplateId;
        this.caseOfficerPersonalisationFactory = caseOfficerPersonalisationFactory;
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
        this.notificationSender = notificationSender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final HearingCentre hearingCentre =
            asylumCase
                .getHearingCentre()
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));

        final String hearingCentreEmailAddress =
            hearingCentreEmailAddresses
                .get(hearingCentre);

        if (hearingCentreEmailAddress == null) {
            throw new IllegalStateException("Hearing centre email address not found: " + hearingCentre.toString());
        }

        Map<String, String> personalisation =
            caseOfficerPersonalisationFactory
                .create(asylumCase);

        String reference =
            callback.getCaseDetails().getId()
            + "_APPEAL_SUBMITTED_CASE_OFFICER";

        String notificationId =
            notificationSender.sendEmail(
                appealSubmittedCaseOfficerTemplateId,
                hearingCentreEmailAddress,
                personalisation,
                reference
            );

        List<IdValue<String>> notificationsSent =
            asylumCase
                .getNotificationsSent()
                .orElseGet(ArrayList::new);

        notificationsSent.add(new IdValue<>(reference, notificationId));

        asylumCase.setNotificationsSent(notificationsSent);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
