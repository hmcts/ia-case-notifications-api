package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativePaymentPaidPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String beforeListingTemplateId = "beforeListingTemplateId";
    private String afterListingTemplateId = "afterListingTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String legalRepEmailAddress = "legalRep@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private LegalRepresentativePaymentPaidPersonalisation legalRepresentativePaymentPaidPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepresentativePaymentPaidPersonalisation = new LegalRepresentativePaymentPaidPersonalisation(
            beforeListingTemplateId,
            afterListingTemplateId,
            iaExUiFrontendUrl,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_template_id() {

        assertEquals(beforeListingTemplateId, legalRepresentativePaymentPaidPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        assertEquals(afterListingTemplateId, legalRepresentativePaymentPaidPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_PAYMENT_PAID_LEGAL_REPRESENTATIVE",
            legalRepresentativePaymentPaidPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(
            legalRepresentativePaymentPaidPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativePaymentPaidPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> legalRepresentativePaymentPaidPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            legalRepresentativePaymentPaidPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(legalRepRefNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));

        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            legalRepresentativePaymentPaidPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("legalRepReferenceNumber"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));

        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_false_if_appeal_not_yet_listed() {
        assertFalse(legalRepresentativePaymentPaidPersonalisation.isAppealListed(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        assertTrue(legalRepresentativePaymentPaidPersonalisation.isAppealListed(asylumCase));
    }
}
