package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_PERSON_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;


@RunWith(MockitoJUnitRunner.class)
public class AppellantInPersonSubmitAppealPersonalisationTest {

    @Mock
    AsylumCase asylumCase;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String mockedAppellantEmailAddress = "appelant@example.net";

    private AppellantInPersonSubmitAppealPersonalisation appellantInPersonSubmitAppealPersonalisation;

    @Before
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when(asylumCase.read(APPELLANT_IN_PERSON_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(mockedAppellantEmailAddress));

        appellantInPersonSubmitAppealPersonalisation = new AppellantInPersonSubmitAppealPersonalisation(templateId);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, appellantInPersonSubmitAppealPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPEAL_SUBMITTED_APPELLANT_IN_PERSON", appellantInPersonSubmitAppealPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertEquals(mockedAppellantEmailAddress, appellantInPersonSubmitAppealPersonalisation.getEmailAddress(asylumCase));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_appelant() {
        when(asylumCase.read(APPELLANT_IN_PERSON_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantInPersonSubmitAppealPersonalisation.getEmailAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantInPersonEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> appellantInPersonSubmitAppealPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = appellantInPersonSubmitAppealPersonalisation.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("HO Ref Number"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("Given names"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("Family name"));
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = appellantInPersonSubmitAppealPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals("", personalisation.get("HO Ref Number"));
        assertEquals("", personalisation.get("Given names"));
        assertEquals("", personalisation.get("Family name"));
    }
}
