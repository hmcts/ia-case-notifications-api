package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CHANGE_ORGANISATION_REQUEST_FIELD;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

public interface EmailWithLinkNotificationPersonalisation extends BaseNotificationPersonalisation<AsylumCase> {
        default Map<String, Object> getPersonalisationForLink(Callback<AsylumCase> callback) {
            try {
                return getPersonalisationForLink(callback.getCaseDetails().getCaseData());
            } catch (NotificationClientException | IOException e) {
                // NotificationClient - if size is more than 2 MB.
                throw new IllegalArgumentException(e);
            }
        }

    default Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        return Collections.emptyMap();
    }

}
