package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CHANGE_ORGANISATION_REQUEST_FIELD;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

public interface LegalRepresentativeEmailNotificationWithLinkPersonalisation extends EmailWithLinkNotificationPersonalisation{
    @Override
    default Set<String> getRecipientsList(AsylumCase asylumCase) {

        if (asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class)
                .map(it -> it.getCaseRoleId() == null)
                .orElse(false)) {

            return Collections.emptySet();
        } else {
            return Collections.singleton(asylumCase
                    .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
                    .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present")));
        }
    }
}
