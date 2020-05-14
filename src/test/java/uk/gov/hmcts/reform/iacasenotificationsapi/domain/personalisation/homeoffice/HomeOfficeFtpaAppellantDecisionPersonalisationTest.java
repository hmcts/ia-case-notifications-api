package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaAppellantDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class HomeOfficeFtpaAppellantDecisionPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock EmailAddressFinder emailAddressFinder;
    @Mock PersonalisationProvider personalisationProvider;
    @Mock GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;
    @Mock CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String homeOfficeEmailAddressFtpaGranted = "homeoffice-granted@example.com";
    private String homeOfficeEmailAddressFtpaRefused = "homeoffice-refused@example.com";
    private String homeOfficeEmailAddressOther = "homeoffice-other@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String legalRepReferenceNumber = "someLegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String applicantGrantedTemplateId = "applicantGrantedTemplateId";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer.services@example.com";

    private HomeOfficeFtpaApplicationDecisionPersonalisation homeOfficeFtpaApplicationDecisionPersonalisation;

    @Before
    public void setup() {

        when(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeEmailAddressOther);
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        homeOfficeFtpaApplicationDecisionPersonalisation = new HomeOfficeFtpaApplicationDecisionPersonalisation(
            govNotifyTemplateIdConfiguration,
            personalisationProvider,
            homeOfficeEmailAddressFtpaGranted,
            homeOfficeEmailAddressFtpaRefused,
            emailAddressFinder,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        when(personalisationProvider.getApplicantType(asylumCase)).thenReturn(ApplicantType.APPELLANT);
        when(homeOfficeFtpaApplicationDecisionPersonalisation.getTemplateId(asylumCase)).thenReturn(applicantGrantedTemplateId);
        assertEquals(applicantGrantedTemplateId, homeOfficeFtpaApplicationDecisionPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_HOME_OFFICE", homeOfficeFtpaApplicationDecisionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_for_granted_appeal_outcome() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaAppellantDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaAppellantDecisionOutcomeType.FTPA_GRANTED));

        assertTrue(homeOfficeFtpaApplicationDecisionPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddressFtpaGranted));
    }

    @Test
    public void should_return_given_email_address_for_partially_granted_appeal_outcome() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaAppellantDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaAppellantDecisionOutcomeType.FTPA_PARTIALLY_GRANTED));

        assertTrue(homeOfficeFtpaApplicationDecisionPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddressFtpaGranted));
    }

    @Test
    public void should_return_given_email_address_for_refused_appeal_outcome() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaAppellantDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaAppellantDecisionOutcomeType.FTPA_REFUSED));

        assertTrue(homeOfficeFtpaApplicationDecisionPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddressFtpaRefused));
    }

    @Test
    public void should_return_given_email_address_for_not_admitted_reheard_remade_appeal_outcome() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaAppellantDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaAppellantDecisionOutcomeType.FTPA_NOT_ADMITTED));

        assertTrue(homeOfficeFtpaApplicationDecisionPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddressOther));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_personalisation_of_all_information_given() {
        when(personalisationProvider.getFtpaDecisionPersonalisation(asylumCase)).thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation = homeOfficeFtpaApplicationDecisionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeRefNumber"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("homeOfficeRefNumber", homeOfficeRefNumber)
            .put("legalRepReferenceNumber", legalRepReferenceNumber)
            .put("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }
}
