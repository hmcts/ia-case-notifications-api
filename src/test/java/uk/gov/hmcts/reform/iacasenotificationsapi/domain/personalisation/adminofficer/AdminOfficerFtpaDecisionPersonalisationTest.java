package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class AdminOfficerFtpaDecisionPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock PersonalisationProvider personalisationProvider;
    @Mock GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;

    private Long caseId = 12345L;
    private String adminOfficeEmailAddress = "some-email@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String legalRepReferenceNumber = "someLegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String applicantGrantedTemplateId = "applicantGrantedTemplateId";
    private String applicantPartiallyGrantedTemplateId = "applicantPartiallyGrantedTemplateId";

    private AdminOfficerFtpaDecisionPersonalisation adminOfficerFtpaDecisionPersonalisation;

    @Before
    public void setup() {
        adminOfficerFtpaDecisionPersonalisation = new AdminOfficerFtpaDecisionPersonalisation(
            govNotifyTemplateIdConfiguration,
            adminOfficeEmailAddress,
            personalisationProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_GRANTED));
        when(adminOfficerFtpaDecisionPersonalisation.getTemplateId(asylumCase)).thenReturn(applicantGrantedTemplateId);
        assertEquals(applicantGrantedTemplateId, adminOfficerFtpaDecisionPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED));
        when(adminOfficerFtpaDecisionPersonalisation.getTemplateId(asylumCase)).thenReturn(applicantPartiallyGrantedTemplateId);
        assertEquals(applicantPartiallyGrantedTemplateId, adminOfficerFtpaDecisionPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_ftpa_application_decision_type() {
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_GRANTED));
        assertEquals(FtpaDecisionOutcomeType.FTPA_GRANTED, adminOfficerFtpaDecisionPersonalisation.getFtpaApplicationDecision(asylumCase));

        when(asylumCase.read(AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_GRANTED));
        assertEquals(FtpaDecisionOutcomeType.FTPA_GRANTED, adminOfficerFtpaDecisionPersonalisation.getFtpaApplicationDecision(asylumCase));

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        assertEquals(null, adminOfficerFtpaDecisionPersonalisation.getFtpaApplicationDecision(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_ADMIN_OFFICER", adminOfficerFtpaDecisionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address() {
        assertEquals(true, adminOfficerFtpaDecisionPersonalisation.getRecipientsList(asylumCase).contains(adminOfficeEmailAddress));
    }

    @Test
    public void should_return_personalisation_of_all_information_given() {
        when(personalisationProvider.getFtpaDecisionPersonalisation(asylumCase)).thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation = adminOfficerFtpaDecisionPersonalisation.getPersonalisation(asylumCase);

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
