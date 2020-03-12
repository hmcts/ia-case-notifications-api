package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class HomeOfficeFtpaAppellantDecisionPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock PersonalisationProvider personalisationProvider;
    @Mock GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;

    private Long caseId = 12345L;
    private String homeOfficeEmailAddress = "homeoffice-allowed@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String legalRepReferenceNumber = "someLegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String applicantGrantedTemplateId = "applicantGrantedTemplateId";

    private HomeOfficeFtpaApplicationDecisionPersonalisation homeOfficeFtpaApplicationDecisionPersonalisation;

    @Before
    public void setup() {
        homeOfficeFtpaApplicationDecisionPersonalisation = new HomeOfficeFtpaApplicationDecisionPersonalisation(
            govNotifyTemplateIdConfiguration,
            personalisationProvider,
            homeOfficeEmailAddress
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
    public void should_return_personalisation_of_all_information_given() {
        when(personalisationProvider.getFtpaDecisionPersonalisation(asylumCase)).thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation = homeOfficeFtpaApplicationDecisionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeRefNumber"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("homeOfficeRefNumber", homeOfficeRefNumber)
            .put("legalRepReferenceNumber", legalRepReferenceNumber)
            .build();
    }

}
