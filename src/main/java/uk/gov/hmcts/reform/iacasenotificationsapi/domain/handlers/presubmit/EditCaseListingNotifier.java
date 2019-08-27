package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealStateNotificationReference.*;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EditCaseListingPersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@Component
public class EditCaseListingNotifier extends AbstractNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    public EditCaseListingNotifier(
            GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration,
            @Value("${endAppealHomeOfficeEmailAddress}") String homeOfficeEmailAddress,
            NotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender,
            EditCaseListingPersonalisationFactory personalisationFactory
    ) {
        super(
                govNotifyTemplateIdConfiguration,
                homeOfficeEmailAddress,
                EDIT_CASE_LISTING_HOME_OFFICE,
                EDIT_CASE_LISTING_LEGAL_REPRESENTATIVE,
                notificationSender,
                notificationIdAppender,
                personalisationFactory
        );

        homeOfficeTemplateId = govNotifyTemplateIdConfiguration.getEditCaseListingHomeOfficeTemplateId();
        legalRepresentativeTemplateId = govNotifyTemplateIdConfiguration.getEditCaseListingLegalRepresentativeTemplateId();
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.EDIT_CASE_LISTING;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

        final AsylumCase asylumCase = caseDetails.getCaseData();
        final AsylumCase oldAsylumCaseDetails = caseDetailsBefore
                                            .orElseThrow(() -> new IllegalStateException("previous case data is not present"))
                                            .getCaseData();

        sendNotificationToHomeOffice(callback, asylumCase, oldAsylumCaseDetails);

        sendNotificationToLegalRepresentative(callback, asylumCase, oldAsylumCaseDetails);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
