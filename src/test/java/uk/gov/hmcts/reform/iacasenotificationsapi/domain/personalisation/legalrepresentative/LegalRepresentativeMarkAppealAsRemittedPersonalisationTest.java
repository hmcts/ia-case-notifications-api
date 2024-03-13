package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SOURCE_OF_REMITTAL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SourceOfRemittal;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeMarkAppealAsRemittedPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private LegalRepresentativeMarkAppealAsRemittedPersonalisation
        legalRepresentativeMarkAppealAsRemittedPersonalisation;
    private Long caseId = 12345L;
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someReferenceNumber";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String legalRepresentativeMarkAppealAsRemittedTemplateId = "templateId";
    private String iaServicesPhone = "0100000000";
    private String iaServicesEmail = "services@email.com";
    private String legalRepEmailAddress = "legalRep@example.com";
    private SourceOfRemittal sourceOfRemittal = SourceOfRemittal.UPPER_TRIBUNAL;
    private Map<String, String> customerServices = Map.of("customerServicesTelephone", iaServicesPhone,
        "customerServicesEmail", iaServicesEmail);

    @BeforeEach
    public void setUp() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class)).thenReturn(Optional.of(sourceOfRemittal));
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(customerServices);
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));

        legalRepresentativeMarkAppealAsRemittedPersonalisation = new LegalRepresentativeMarkAppealAsRemittedPersonalisation(
            legalRepresentativeMarkAppealAsRemittedTemplateId,
            customerServicesProvider);
    }

    @Test
    public void should_return_given_email_address() {
        assertEquals(Collections.singleton(legalRepEmailAddress),
            legalRepresentativeMarkAppealAsRemittedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(legalRepresentativeMarkAppealAsRemittedTemplateId,
            legalRepresentativeMarkAppealAsRemittedPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REP_MARK_APPEAL_AS_REMITTED",
            legalRepresentativeMarkAppealAsRemittedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
            legalRepresentativeMarkAppealAsRemittedPersonalisation.getPersonalisation(asylumCase);

        assertEquals(iaServicesPhone, personalisation.get("customerServicesTelephone"));
        assertEquals(iaServicesEmail, personalisation.get("customerServicesEmail"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals("\nListing reference: " + ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(legalRepRefNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(sourceOfRemittal.getValue(), personalisation.get("remittalSource"));

    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeMarkAppealAsRemittedPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

}