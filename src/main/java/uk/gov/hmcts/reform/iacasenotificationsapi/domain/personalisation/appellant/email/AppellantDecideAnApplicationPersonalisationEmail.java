package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@Service
public class AppellantDecideAnApplicationPersonalisationEmail implements EmailNotificationPersonalisation {

    private static final String ROLE_CITIZEN = "citizen";
    private final String decideAnApplicationRefusedBeforeListingAppellantEmailTemplateId;
    private final String decideAnApplicationRefusedAfterListingAppellantEmailTemplateId;
    private final String decideAnApplicationAppellantGrantedBeforeListingEmailTemplateId;
    private final String decideAnApplicationAppellantGrantedAfterListingEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final MakeAnApplicationService makeAnApplicationService;

    public AppellantDecideAnApplicationPersonalisationEmail(
            @Value("${govnotify.template.decideAnApplication.refused.applicant.appellant.beforeListing.email}") String decideAnApplicationRefusedBeforeListingAppellantEmailTemplateId,
            @Value("${govnotify.template.decideAnApplication.refused.applicant.appellant.afterListing.email}") String decideAnApplicationRefusedAfterListingAppellantEmailTemplateId,
            @Value("${govnotify.template.decideAnApplication.granted.applicant.appellant.beforeListing.email}") String decideAnApplicationAppellantGrantedBeforeListingEmailTemplateId,
            @Value("${govnotify.template.decideAnApplication.granted.applicant.appellant.afterListing.email}") String decideAnApplicationAppellantGrantedAfterListingEmailTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder,
            MakeAnApplicationService makeAnApplicationService) {
        this.decideAnApplicationRefusedBeforeListingAppellantEmailTemplateId = decideAnApplicationRefusedBeforeListingAppellantEmailTemplateId;
        this.decideAnApplicationRefusedAfterListingAppellantEmailTemplateId = decideAnApplicationRefusedAfterListingAppellantEmailTemplateId;
        this.decideAnApplicationAppellantGrantedBeforeListingEmailTemplateId = decideAnApplicationAppellantGrantedBeforeListingEmailTemplateId;
        this.decideAnApplicationAppellantGrantedAfterListingEmailTemplateId = decideAnApplicationAppellantGrantedAfterListingEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.makeAnApplicationService = makeAnApplicationService;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        Optional<MakeAnApplication> maybeMakeAnApplication = makeAnApplicationService.getMakeAnApplication(asylumCase);

        if (maybeMakeAnApplication.isPresent()) {
            MakeAnApplication makeAnApplication = maybeMakeAnApplication.get();
            String decision = makeAnApplication.getDecision();

            boolean isApplicationListed = makeAnApplicationService.isApplicationListed(State.get(makeAnApplication.getState()));

            if (isApplicationListed) {
                return "Granted".equals(decision) ? decideAnApplicationAppellantGrantedAfterListingEmailTemplateId : decideAnApplicationRefusedAfterListingAppellantEmailTemplateId;
            } else {
                return "Granted".equals(decision) ? decideAnApplicationAppellantGrantedBeforeListingEmailTemplateId : decideAnApplicationRefusedBeforeListingAppellantEmailTemplateId;
            }
        } else {
            return "";
        }
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DECIDE_AN_APPLICATION_APPELLANT_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("HO Ref Number", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("applicationType", makeAnApplicationService.getMakeAnApplication(asylumCase).map(MakeAnApplication::getType).orElse(""))
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
