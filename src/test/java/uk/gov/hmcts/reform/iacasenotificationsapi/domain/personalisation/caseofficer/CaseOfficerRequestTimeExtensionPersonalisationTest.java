package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@RunWith(MockitoJUnitRunner.class)
public class CaseOfficerRequestTimeExtensionPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String hearingCentreEmailAddress = "hearingCentre@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenName = "Pablo";
    private String appellantFamilyName = "Jimenez";
    private String iaCcdFrontendUrl = "http://localhost";


    private CaseOfficerRequestTimeExtensionPersonalisation caseOfficerRequestTimeExtensionPersonalisation;

    @Before
    public void setUp() {
        when(emailAddressFinder.getEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenName));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        caseOfficerRequestTimeExtensionPersonalisation =
            new CaseOfficerRequestTimeExtensionPersonalisation(
                templateId,
                iaCcdFrontendUrl,
                emailAddressFinder
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerRequestTimeExtensionPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_REQUEST_TIME_EXTENSION_CASE_OFFICER", caseOfficerRequestTimeExtensionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(hearingCentreEmailAddress, caseOfficerRequestTimeExtensionPersonalisation.getRecipientsList(asylumCase).contains(hearingCentreEmailAddress));
    }


    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> caseOfficerRequestTimeExtensionPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase cannot be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", appealReferenceNumber)
                .put("Appellant Given names", appellantGivenName)
                .put("Appellant Family name", appellantFamilyName)
                .put("Hyperlink to service", iaCcdFrontendUrl)
                .build();

        Map<String, String> actualPersonalisation = caseOfficerRequestTimeExtensionPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualTo(expectedPersonalisation);
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", "")
                .put("Appellant Given names", "")
                .put("Appellant Family name", "")
                .put("Hyperlink to service", iaCcdFrontendUrl)
                .build();

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> actualPersonalisation = caseOfficerRequestTimeExtensionPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualTo(expectedPersonalisation);
    }
}
