package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class HomeOfficeEditListingPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeCaseEditedTemplateId;
    private final String listAssistHearingHomeOfficeCaseEditedTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private EmailAddressFinder emailAddressFinder;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;

    public HomeOfficeEditListingPersonalisation(
        @Value("${govnotify.template.caseEdited.homeOffice.email}") String homeOfficeCaseEditedTemplateId,
        @Value("${govnotify.template.listAssistHearing.caseEdited.homeOffice.email}") String listAssistHearingHomeOfficeCaseEditedTemplateId,
        EmailAddressFinder emailAddressFinder,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider,
        HearingDetailsFinder hearingDetailsFinder
    ) {
        this.homeOfficeCaseEditedTemplateId = homeOfficeCaseEditedTemplateId;
        this.listAssistHearingHomeOfficeCaseEditedTemplateId = listAssistHearingHomeOfficeCaseEditedTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return asylumCase.read(IS_INTEGRATED, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES
                ? listAssistHearingHomeOfficeCaseEditedTemplateId : homeOfficeCaseEditedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        return asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).equals(Optional.of(HearingCentre.REMOTE_HEARING))
            ? Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase))
            : Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_RE_LISTED_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .putAll(personalisationProvider.getPersonalisation(callback))
            .put("hearingCentreAddress", hearingDetailsFinder
                    .getHearingCentreLocation(callback.getCaseDetails().getCaseData()));

        return listCaseFields.build();
    }
}
