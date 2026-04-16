package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_EJP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MOBILE_NUMBER;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantNotificationsTurnedOnPersonalisationSmsTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PinInPostDetails pinInPostDetails;
    private final String representedTemplateId = "representedTemplateId";
    private final String unrepresentedTemplateId = "unrepresentedTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String appellantMobileNumberEjp = "07777777777";
    private final String ccdReferenceNumberForDisplay = "someRefNumber";
    private final String homeOfficeRef = "homeOfficeRef";
    private final String customerServicesTelephone = "customerServicesTelephone";
    private final String customerServicesEmail = "customerServicesEmail";
    private final String securityCode = "securityCode";


    private AppellantNotificationsTurnedOnPersonalisationSms appellantNotificationsTurnedOnPersonalisationSms;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        String legalRepReferenceEjp = "someLegalRepReferenceNumber";
        when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.of(legalRepReferenceEjp));
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(appellantMobileNumberEjp));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        String dateOfBirth = "2020-03-01";
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRef));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumberForDisplay));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class)).thenReturn(Optional.of(pinInPostDetails));
        when(pinInPostDetails.getAccessCode()).thenReturn(securityCode);
        String validDate = "2024-03-01";
        when(pinInPostDetails.getExpiryDate()).thenReturn(validDate);

        appellantNotificationsTurnedOnPersonalisationSms = new AppellantNotificationsTurnedOnPersonalisationSms(
            representedTemplateId,
            unrepresentedTemplateId,
            iaExUiFrontendUrl,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_represented_template_id() {
        assertEquals(representedTemplateId,
            appellantNotificationsTurnedOnPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_unrepresented_template_id() {
        when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.empty());

        assertEquals(unrepresentedTemplateId,
            appellantNotificationsTurnedOnPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {

        Long caseId = 12345L;
        assertEquals(caseId + "_APPELLANT_NOTIFICATIONS_TURNED_ON_SMS", appellantNotificationsTurnedOnPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_mobile_number() {
        assertTrue(
            appellantNotificationsTurnedOnPersonalisationSms.getRecipientsList(asylumCase).contains(
                appellantMobileNumberEjp));
    }

    @Test
    public void should_return_empty_set_when_mobile_number_is_null() {
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertEquals(Collections.emptySet(),
            appellantNotificationsTurnedOnPersonalisationSms.getRecipientsList(asylumCase));
    }



    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
assertThrows(NullPointerException.class,
            () -> appellantNotificationsTurnedOnPersonalisationSms.getPersonalisation((AsylumCase) null))
            ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation =
            appellantNotificationsTurnedOnPersonalisationSms.getPersonalisation(asylumCase);

        String expectedValidDate = "1 Mar 2024";
        String expectedDateOfBirth = "1 Mar 2020";
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("listingReferenceLine", "\nListing reference: " + ariaListingReference)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRef)
            .containsEntry("ccdReferenceNumberForDisplay", ccdReferenceNumberForDisplay)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("dateOfBirth", expectedDateOfBirth)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("customerServicesTelephone", customerServicesTelephone)
            .containsEntry("customerServicesEmail", customerServicesEmail)
            .containsEntry("securityCode", securityCode)
            .containsEntry("validDate", expectedValidDate);

    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }
}
