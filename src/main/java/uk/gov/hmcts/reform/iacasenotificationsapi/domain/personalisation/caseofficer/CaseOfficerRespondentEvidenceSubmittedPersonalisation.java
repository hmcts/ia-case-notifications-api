package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class CaseOfficerRespondentEvidenceSubmittedPersonalisation implements EmailNotificationPersonalisation {

    private final String respondentEvidenceSubmittedTemplateId;
    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final FeatureToggler featureToggler;

    public CaseOfficerRespondentEvidenceSubmittedPersonalisation(
            @NotNull(message = "respondentEvidenceSubmittedTemplateId cannot be null") @Value("${govnotify.template.respondentEvidenceSubmitted.caseOfficer.email}") String respondentEvidenceSubmittedTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
        requireNonNull(iaExUiFrontendUrl, "iaExUiFrontendUrl must not be null");

        this.respondentEvidenceSubmittedTemplateId = respondentEvidenceSubmittedTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return respondentEvidenceSubmittedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return featureToggler.getValue("tcw-notifications-feature", true)
                ? Collections.singleton(emailAddressFinder.getHearingCentreEmailAddress(asylumCase))
                : Collections.emptySet();

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_EVIDENCE_SUBMITTED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("legalRepCompany",   asylumCase.read(LEGAL_REP_COMPANY, String.class).orElse(""))
                .put("legalRepCompanyAddress", formatCompanyAddress(asylumCase))
                .put("legalRepName",   asylumCase.read(LEGAL_REP_NAME, String.class).orElse(""))
                .put("legalRepEmail",   asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class).orElse(""))
                .put("legalRepReference",   asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .build();
    }

    public String formatCompanyAddress(AsylumCase asylumCase) {

        StringBuilder str = new StringBuilder();

        if (asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class).isPresent()) {

            final String addressLine1 =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getAddressLine1).orElse("");

            final String addressLine2 =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getAddressLine2).orElse("");

            final String addressLine3 =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getAddressLine3).orElse("");

            final String postTown =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getPostTown).orElse("");

            final String county =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getCounty).orElse("");

            final String postCode =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getPostCode).orElse("");

            final String country =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getCountry).orElse("");

            if (!Optional.of(addressLine1).get().equals("")) {
                str.append(addressLine1);
                str.append(", ");
            }

            if (!Optional.of(addressLine2).get().isEmpty()) {
                str.append(addressLine2);
                str.append(", ");
            }

            if (!Optional.of(addressLine3).get().isEmpty()) {
                str.append(addressLine3);
                str.append(", ");
            }

            if (!Optional.of(postTown).get().isEmpty()) {
                str.append(postTown);
                str.append(", ");
            }

            if (!Optional.of(county).get().isEmpty()) {
                str.append(county);
                str.append(", ");
            }

            if (!Optional.of(postCode).get().isEmpty()) {
                str.append(postCode);
                str.append(", ");
            }

            if (!Optional.of(country).get().isEmpty()) {
                str.append(country);
            }

        } else {
            return "";
        }

        return str.toString();
    }
}
