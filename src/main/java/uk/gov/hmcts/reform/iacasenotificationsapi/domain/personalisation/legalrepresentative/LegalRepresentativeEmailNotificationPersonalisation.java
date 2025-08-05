package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CHANGE_ORGANISATION_REQUEST_FIELD;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_EMAIL_EJP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepEmailInternalOrLegalRepJourney;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isEjpCase;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

public interface LegalRepresentativeEmailNotificationPersonalisation extends EmailNotificationPersonalisation {

    @Override
    default Set<String> getRecipientsList(AsylumCase asylumCase) {

        if (asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(type -> Objects.equals(type.getValue(), JourneyType.AIP.getValue()))
            .orElse(false)) {
            getLogger(LegalRepresentativeEmailNotificationPersonalisation.class).info("-------111");

            return Collections.emptySet();
        }

        if (asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class)
            .map(it -> it.getCaseRoleId() == null)
            .orElse(false)) {
            getLogger(LegalRepresentativeEmailNotificationPersonalisation.class).info("-------222");

            return Collections.emptySet();
        }

        if (isEjpCase(asylumCase)) {
            getLogger(LegalRepresentativeEmailNotificationPersonalisation.class).info("-------333");
            getLogger(LegalRepresentativeEmailNotificationPersonalisation.class).info(
                    "-------asylumCase.read(LEGAL_REP_EMAIL_EJP, String.class): {}",
                    asylumCase.read(LEGAL_REP_EMAIL_EJP, String.class)
            );
            return Collections.singleton(asylumCase
                .read(LEGAL_REP_EMAIL_EJP, String.class)
                .orElseThrow(() -> new IllegalStateException("legalRepEmailEjp is not present")));
        }
        getLogger(LegalRepresentativeEmailNotificationPersonalisation.class).info("-------444");
        getLogger(LegalRepresentativeEmailNotificationPersonalisation.class).info(
                "-------getLegalRepEmailInternalOrLegalRepJourney(asylumCase): {}",
                getLegalRepEmailInternalOrLegalRepJourney(asylumCase)
        );

        return Collections.singleton(getLegalRepEmailInternalOrLegalRepJourney(asylumCase));
    }
}
