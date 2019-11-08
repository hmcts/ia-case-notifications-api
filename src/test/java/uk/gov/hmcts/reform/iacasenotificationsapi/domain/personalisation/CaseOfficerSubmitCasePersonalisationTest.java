package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


@RunWith(MockitoJUnitRunner.class)
public class CaseOfficerSubmitCasePersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock EmailAddressFinder emailAddressFinder;


    private Long caseId = 12345L;
    private String templateId = "someTemplateId";

    private String hearingCentreEmailAddress = "hearingCentre@example.com";

    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private CaseOfficerSubmitCasePersonalisation caseOfficerSubmitCasePersonalisation;

    @Before
    public void setup() {
        when(emailAddressFinder.getEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        caseOfficerSubmitCasePersonalisation = new CaseOfficerSubmitCasePersonalisation(
            templateId,
            emailAddressFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerSubmitCasePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_SUBMITTED_CASE_OFFICER", caseOfficerSubmitCasePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_lookup_map() {
        assertEquals(hearingCentreEmailAddress, caseOfficerSubmitCasePersonalisation.getEmailAddress(asylumCase));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> caseOfficerSubmitCasePersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = caseOfficerSubmitCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = caseOfficerSubmitCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
    }
}
