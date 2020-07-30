package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import java.util.Map;
import org.junit.Test;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

public class RespondentForceCaseProgressionPersonalisationTest {

    private RespondentForceCaseProgressionPersonalisation personalisation =
        new RespondentForceCaseProgressionPersonalisation(
            "templateId", "emailAddress");

    @Test
    public void getTemplateId() {
        assertEquals("templateId", personalisation.getTemplateId());
    }

    @Test
    public void getRecipientsList() {
        assertTrue(personalisation.getRecipientsList(new AsylumCase()).contains("emailAddress"));
    }

    @Test
    public void getReferenceId() {
        assertEquals("1234_RESPONDENT_FORCE_CASE_PROGRESSION", personalisation.getReferenceId(1234L));
    }

    @Test
    public void getPersonalisation() {
        AsylumCase asylumCase = writeTestAsylumCase();
        Map<String, String> actualPersonalisation = personalisation.getPersonalisation(asylumCase);
        assertEquals("RP/50001/2020", actualPersonalisation.get("appealReferenceNumber"));
        assertEquals("Lacy Dawson", actualPersonalisation.get("appellantGivenNames"));
        assertEquals("Venus Blevins", actualPersonalisation.get("appellantFamilyName"));
        assertEquals("CASE001", actualPersonalisation.get("legalRepReferenceNumber"));
        assertEquals("A1234567", actualPersonalisation.get("homeOfficeReferenceNumber"));
    }

    private AsylumCase writeTestAsylumCase() {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "RP/50001/2020");
        asylumCase.write(APPELLANT_GIVEN_NAMES, "Lacy Dawson");
        asylumCase.write(APPELLANT_FAMILY_NAME, "Venus Blevins");
        asylumCase.write(LEGAL_REP_REFERENCE_NUMBER, "CASE001");
        asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, "A1234567");
        return asylumCase;
    }
}