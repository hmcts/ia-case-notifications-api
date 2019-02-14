package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

@Component
public class RespondentNonStandardDirectionNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final List<State> allowedCaseStates =
        Arrays.asList(
            State.APPEAL_SUBMITTED,
            State.APPEAL_SUBMITTED_OUT_OF_TIME,
            State.AWAITING_RESPONDENT_EVIDENCE,
            State.CASE_BUILDING,
            State.CASE_UNDER_REVIEW,
            State.RESPONDENT_REVIEW,
            State.SUBMIT_HEARING_REQUIREMENTS,
            State.LISTING
        );

    private final String govNotifyTemplateId;
    private final String respondentEmailAddress;
    private final DirectionFinder directionFinder;
    private final NotificationSender notificationSender;
    private final StringProvider stringProvider;

    public RespondentNonStandardDirectionNotifier(
        @Value("${govnotify.template.respondentNonStandardDirection}") String govNotifyTemplateId,
        @Value("${respondentEmailAddresses.nonStandardDirectionUntilListing}") String respondentEmailAddress,
        DirectionFinder directionFinder,
        NotificationSender notificationSender,
        StringProvider stringProvider
    ) {
        requireNonNull(govNotifyTemplateId, "govNotifyTemplateId must not be null");
        requireNonNull(respondentEmailAddress, "respondentEmailAddress must not be null");

        this.govNotifyTemplateId = govNotifyTemplateId;
        this.respondentEmailAddress = respondentEmailAddress;
        this.directionFinder = directionFinder;
        this.notificationSender = notificationSender;
        this.stringProvider = stringProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        final State caseState =
            callback
                .getCaseDetails()
                .getState();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SEND_DIRECTION
               && allowedCaseStates.contains(caseState);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Direction nonStandardDirection =
            directionFinder
                .findFirst(asylumCase, DirectionTag.NONE)
                .orElseThrow(() -> new IllegalStateException("non-standard direction is not present"));

        if (!nonStandardDirection.getParties().equals(Parties.RESPONDENT)) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        HearingCentre hearingCentre =
            asylumCase
                .getHearingCentre()
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));

        String hearingCentreForDisplay =
            stringProvider
                .get("hearingCentre", hearingCentre.toString())
                .orElseThrow(() -> new IllegalStateException("hearingCentre display string is not present"));

        String directionDueDate =
            LocalDate
                .parse(nonStandardDirection.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        Map<String, String> personalisation =
            ImmutableMap
                .<String, String>builder()
                .put("HearingCentre", hearingCentreForDisplay)
                .put("Appeal Ref Number", asylumCase.getAppealReferenceNumber().orElse(""))
                .put("HORef", asylumCase.getHomeOfficeReferenceNumber().orElse(""))
                .put("Given names", asylumCase.getAppellantGivenNames().orElse(""))
                .put("Family name", asylumCase.getAppellantFamilyName().orElse(""))
                .put("Explanation", nonStandardDirection.getExplanation())
                .put("due date", directionDueDate)
                .build();

        String reference =
            callback.getCaseDetails().getId()
            + "_RESPONDENT_NON_STANDARD_DIRECTION";

        String notificationId =
            notificationSender.sendEmail(
                govNotifyTemplateId,
                respondentEmailAddress,
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
