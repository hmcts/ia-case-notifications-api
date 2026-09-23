package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.hasStf24WeeksStatus;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

@Service
public class HomeOfficeEditListingPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeCaseEditedNonAdaTemplateId;
    private final String homeOfficeCaseEditedAdaTemplateId;
    private final String listAssistHearingHomeOfficeCaseEditedTemplateId;
    private final String homeOfficeCaseEdited24wTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final EmailAddressFinder emailAddressFinder;
    private final CustomerServicesProvider customerServicesProvider;

    public HomeOfficeEditListingPersonalisation(
        @Value("${govnotify.template.caseEdited.homeOffice.email.nonAda}") String homeOfficeCaseEditedNonAdaTemplateId,
        @Value("${govnotify.template.caseEdited.homeOffice.email.ada}") String homeOfficeCaseEditedAdaTemplateId,
        @Value("${govnotify.template.listAssistHearing.caseEdited.homeOffice.email}") String listAssistHearingHomeOfficeCaseEditedTemplateId,
        @Value("${govnotify.template.reListCase24Weeks.homeOffice.email}") String homeOfficeCaseEdited24wTemplateId,
        EmailAddressFinder emailAddressFinder,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider
    ) {

        this.homeOfficeCaseEditedNonAdaTemplateId = homeOfficeCaseEditedNonAdaTemplateId;
        this.homeOfficeCaseEditedAdaTemplateId = homeOfficeCaseEditedAdaTemplateId;
        this.listAssistHearingHomeOfficeCaseEditedTemplateId = listAssistHearingHomeOfficeCaseEditedTemplateId;
        this.homeOfficeCaseEdited24wTemplateId = homeOfficeCaseEdited24wTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (hasStf24WeeksStatus(asylumCase)) {
            return homeOfficeCaseEdited24wTemplateId;
        }
        if (isAcceleratedDetainedAppeal(asylumCase)) {
            return homeOfficeCaseEditedAdaTemplateId;
        }
        if (asylumCase.read(IS_INTEGRATED, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            return listAssistHearingHomeOfficeCaseEditedTemplateId;
        }
        return homeOfficeCaseEditedNonAdaTemplateId;
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

        final Map<String, String> listCaseFields = new HashMap<>();
        listCaseFields.putAll(customerServicesProvider.getCustomerServicesPersonalisation(callback));
        listCaseFields.putAll(personalisationProvider.getPersonalisation(callback));
        return ImmutableMap.copyOf(listCaseFields);
    }
}
