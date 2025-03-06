package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CHANGE_ORGANISATION_REQUEST_FIELD;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_EMAIL_EJP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepEmailInternalOrLegalRepJourney;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isEjpCase;

@Service
@Slf4j
public class LegalRepresentativeAppealStartedDisposalPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String appealStartedLegalRepresentativeDisposalTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    public LegalRepresentativeAppealStartedDisposalPersonalisation(
        @NotNull(message = "appealStartedLegalRepresentativeDisposalTemplateId cannot be null")
        @Value("${govnotify.template.appealStarted.legalRep.disposal.email}") String appealStartedLegalRepresentativeDisposalTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.appealStartedLegalRepresentativeDisposalTemplateId = appealStartedLegalRepresentativeDisposalTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;

        log.info(
            "-------------3LegalRepresentativeAppealStartedDisposalPersonalisation appealStartedLegalRepresentativeDisposalTemplateId {}",
            appealStartedLegalRepresentativeDisposalTemplateId
        );
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        log.info("-------------LegalRepresentativeAppealStartedDisposalPersonalisation.getRecipientsList ");
        if (asylumCase.read(JOURNEY_TYPE, JourneyType.class)
                .map(type -> Objects.equals(type.getValue(), JourneyType.AIP.getValue()))
                .orElse(false)) {
            log.info("-------------LegalRepresentativeAppealStartedDisposalPersonalisation.getRecipientsList 111");

            return Collections.emptySet();
        }

        if (asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class)
                .map(it -> it.getCaseRoleId() == null)
                .orElse(false)) {

            log.info("-------------LegalRepresentativeAppealStartedDisposalPersonalisation.getRecipientsList 222");
            return Collections.emptySet();
        }

        if (isEjpCase(asylumCase)) {
            log.info("-------------LegalRepresentativeAppealStartedDisposalPersonalisation.getRecipientsList 333");
            return Collections.singleton(asylumCase
                    .read(LEGAL_REP_EMAIL_EJP, String.class)
                    .orElseThrow(() -> new IllegalStateException("legalRepEmailEjp is not present")));
        }

        log.info(
            "-------------LegalRepresentativeAppealStartedDisposalPersonalisation.getRecipientsList 444 {}",
            getLegalRepEmailInternalOrLegalRepJourney(asylumCase)
        );
        return Collections.singleton(getLegalRepEmailInternalOrLegalRepJourney(asylumCase));
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        log.info(
            "-------------3LegalRepresentativeAppealStartedDisposalPersonalisation getTemplateId {}",
            appealStartedLegalRepresentativeDisposalTemplateId
        );
        return appealStartedLegalRepresentativeDisposalTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_STARTED_LEGAL_REP_DISPOSAL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        ImmutableMap<String, String> res = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("creationDate", LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")))
            .build();

        log.info(
            "--------------------3LegalRepresentativeAppealStartedDisposalPersonalisation.getPersonalisation {}",
            res
        );

        return res;
    }
}
