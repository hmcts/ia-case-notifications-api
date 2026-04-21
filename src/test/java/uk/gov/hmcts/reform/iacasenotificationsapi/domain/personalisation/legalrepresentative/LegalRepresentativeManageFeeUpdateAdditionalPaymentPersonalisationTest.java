package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FeeUpdateReason.APPEAL_WITHDRAWN;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisationTest {

    private final String templateId = "legalRepAdditionalPaymentTemplateId";
    private final String legalRepEmail = "example@example.com";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String legalRepReferenceNumber = "legalRepReferenceNumber";
    private final String appellantGivenNames = "GivenNames";
    private final String appellantFamilyName = "FamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer@example.com";
    private final String iaExUiFrontendUrl = "http://somefrontendurl";
    private final int daysAfterManageFeeUpdate = 14;
    private final FeeUpdateReason feeUpdateReason = APPEAL_WITHDRAWN;
    private final String feeUpdateReasonString = APPEAL_WITHDRAWN.getNormalizedValue();

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    private FeatureToggler featureToggler;

    private LegalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation legalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation;

    @BeforeEach
    public void setup() {

        Mockito.when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        Mockito.when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        String onlineCaseReferenceNumber = "onlineCaseReferenceNumber";
        Mockito.when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
        String originalFee = "8000";
        Mockito.when(asylumCase.read(PREVIOUS_FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(originalFee));
        String newFee = "14000";
        Mockito.when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(newFee));
        String additionalAmount = "6000";
        Mockito.when(asylumCase.read(MANAGE_FEE_REQUESTED_AMOUNT, String.class)).thenReturn(Optional.of(additionalAmount));
        Mockito.when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(feeUpdateReason));
        Mockito.when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmail));
        Mockito.when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        Mockito.when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation = new LegalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation(
            templateId,
            iaExUiFrontendUrl,
            customerServicesProvider,
            systemDateProvider,
            daysAfterManageFeeUpdate,
            featureToggler
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void should_return_given_email_address_from_lookup_map_when_feature_flag_is_On_or_off(boolean value) {
        when(featureToggler.getValue("dlrm-telephony-feature-flag", false)).thenReturn(value);
        if (value) {
            assertTrue(
                legalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation.getRecipientsList(asylumCase).contains("example@example.com"));
        } else {
            assertTrue(legalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation.getRecipientsList(asylumCase).isEmpty());
        }
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_LEGAL_REPRESENTATIVE_ADDITIONAL_PAYMENT_REQUESTED",
            legalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        when(featureToggler.getValue("dlrm-telephony-feature-flag", false)).thenReturn(true);
        assertTrue(
            legalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmail));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        Mockito.when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());
        when(featureToggler.getValue("dlrm-telephony-feature-flag", false)).thenReturn(true);
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> legalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation.getRecipientsList(asylumCase));
        assertEquals("legalRepresentativeEmailAddress is not present", exception.getMessage());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> legalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        final String dueDate = LocalDate.now().plusDays(daysAfterManageFeeUpdate)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterManageFeeUpdate)).thenReturn(dueDate);

        Map<String, String> personalisation =
            legalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("legalRepReferenceNumber", legalRepReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("originalFee", "80.00")
            .containsEntry("newFee", "140.00")
            .containsEntry("additionalFee", "60.00")
            .containsEntry("dueDate", systemDateProvider.dueDate(daysAfterManageFeeUpdate))
            .containsEntry("feeUpdateReason", feeUpdateReasonString);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
