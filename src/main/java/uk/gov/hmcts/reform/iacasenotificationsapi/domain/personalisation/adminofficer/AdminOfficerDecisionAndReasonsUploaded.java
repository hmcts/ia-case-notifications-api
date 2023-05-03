package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@Service
public class AdminOfficerDecisionAndReasonsUploaded implements EmailNotificationPersonalisation {

    private final String decisionAndReasonUploadedTemplateId;

    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

//    private final CustomerServicesProvider customerServicesProvider;

    private final EmailAddressFinder emailAddressFinder;


    public AdminOfficerDecisionAndReasonsUploaded(
            @NotNull(message = "decisionAndReasonUploadedTemplateId cannot be null")
            @Value("${govnotify.template.decisionAndReasonsTemplateUploaded.admin.email}") String decisionAndReasonUploadedTemplateId,
            AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider,
            CustomerServicesProvider customerServicesProvider,
            EmailAddressFinder emailAddressFinder) {
        this.decisionAndReasonUploadedTemplateId = decisionAndReasonUploadedTemplateId;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
//        this.customerServicesProvider = customerServicesProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return decisionAndReasonUploadedTemplateId;
    }


    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {

        var a = Collections.singleton(emailAddressFinder.getAdminEmailAddress(asylumCase));
        return Collections.singleton(emailAddressFinder.getAdminEmailAddress(asylumCase));

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_OUTCOME_ADMIN";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        HearingCentre hearingCentre =
                asylumCase.read(HEARING_CENTRE, HearingCentre.class).orElse(HearingCentre.UNKNOWN);

        return
                ImmutableMap
                        .<String, String>builder()
                        .putAll(adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase))
                        .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                        .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                        .put("HearingCentreName", hearingCentre.getValue())
//                        .put("hearingCentre", asylumCase.read(AsylumCaseDefinition. HEARING_CENTRE, String.class).orElse(""))
                        .put("applicationDecision", asylumCase.read(AsylumCaseDefinition.APPLICATION_DECISION, String.class).orElse(""))
                        .build();
    }
}
