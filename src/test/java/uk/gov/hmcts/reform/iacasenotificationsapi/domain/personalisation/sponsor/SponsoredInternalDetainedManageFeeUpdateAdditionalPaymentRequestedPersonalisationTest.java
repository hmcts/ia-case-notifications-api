package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.sponsor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SponsoredInternalDetainedManageFeeUpdateAdditionalPaymentRequestedPersonalisationTest {

    @Mock private AsylumCase asylumCase;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private SystemDateProvider systemDateProvider;
    @Mock private FeatureToggler featureToggler;

    private final String templateId = "manageFeeUpdateAdditionalPaymentTemplateId";
    private final String iaExUiFrontendUrl = "https://ia-frontend";
    private final int daysToWaitAfterManageFeeUpdate = 14;

    private final Long ccdCaseId = 12345L;

    private SponsoredInternalDetainedManageFeeUpdateAdditionalPaymentRequestedPersonalisation personalisation;

    @BeforeEach
    void setup() {
        personalisation =
                new SponsoredInternalDetainedManageFeeUpdateAdditionalPaymentRequestedPersonalisation(
                        templateId,
                        iaExUiFrontendUrl,
                        customerServicesProvider,
                        systemDateProvider,
                        daysToWaitAfterManageFeeUpdate,
                        featureToggler
                );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, personalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(
                ccdCaseId + "_DETAINED_SPONSORED_ADDITIONAL_PAYMENT_REQUESTED",
                personalisation.getReferenceId(ccdCaseId)
        );
    }

    @Test
    void should_return_recipients_list_from_utility() {
        Set<String> expected = Set.of("some_recipient_key");

        try (MockedStatic<AsylumCaseUtils> utils = mockStatic(AsylumCaseUtils.class)) {
            utils.when(() -> AsylumCaseUtils.getSponsorAddressInCountryOrOoc(asylumCase))
                    .thenReturn(expected);

            assertEquals(expected, personalisation.getRecipientsList(asylumCase));
        }
    }

    @Test
    void should_throw_exception_when_asylum_case_is_null() {
        assertThatThrownBy(() -> personalisation.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(customerServicesProvider.getCustomerServicesPersonalisation())
                .thenReturn(Map.of(
                        "customerServicesTelephone", "555 555 555",
                        "customerServicesEmail", "example@example.com"
                ));

        when(systemDateProvider.dueDate(daysToWaitAfterManageFeeUpdate)).thenReturn("01 Jan 2026");

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("appealRef"));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("legalRepRef"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("given"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("family"));
        when(asylumCase.read(PREVIOUS_FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("14000"));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("16000"));
        when(asylumCase.read(MANAGE_FEE_REQUESTED_AMOUNT, String.class)).thenReturn(Optional.of("2000"));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of("1234-1234"));

        FeeUpdateReason feeUpdateReason = mock(FeeUpdateReason.class);
        when(feeUpdateReason.getNormalizedValue()).thenReturn("Some reason");
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(feeUpdateReason));

        List<String> sponsorAddressLines = List.of("line1", "line2", "line3", "town", "postcode");

        try (MockedStatic<AsylumCaseUtils> utils = mockStatic(AsylumCaseUtils.class);
             MockedStatic<CommonUtils> common = mockStatic(CommonUtils.class)) {

            utils.when(() -> AsylumCaseUtils.getSponserAddressAsList(asylumCase))
                    .thenReturn(sponsorAddressLines);

            common.when(() -> CommonUtils.convertAsylumCaseFeeValue("14000")).thenReturn("140.00");
            common.when(() -> CommonUtils.convertAsylumCaseFeeValue("16000")).thenReturn("160.00");
            common.when(() -> CommonUtils.convertAsylumCaseFeeValue("2000")).thenReturn("20.00");

            Map<String, String> result = personalisation.getPersonalisation(asylumCase);

            assertEquals("555 555 555", result.get("customerServicesTelephone"));
            assertEquals("example@example.com", result.get("customerServicesEmail"));

            assertEquals("appealRef", result.get("appealReferenceNumber"));
            assertEquals("legalRepRef", result.get("legalRepReferenceNumber"));
            assertEquals("given", result.get("appellantGivenNames"));
            assertEquals("family", result.get("appellantFamilyName"));

            assertEquals(iaExUiFrontendUrl, result.get("linkToOnlineService"));

            assertEquals("140.00", result.get("originalFee"));
            assertEquals("160.00", result.get("newFee"));
            assertEquals("20.00", result.get("additionalFee"));

            assertEquals("Some reason", result.get("feeUpdateReason"));
            assertEquals("1234-1234", result.get("onlineCaseReferenceNumber"));

            assertEquals("01 Jan 2026", result.get("dueDate"));

            assertEquals("line1", result.get("address_line_1"));
            assertEquals("line2", result.get("address_line_2"));
            assertEquals("line3", result.get("address_line_3"));
            assertEquals("town", result.get("address_line_4"));
            assertEquals("postcode", result.get("address_line_5"));
        }
    }

    @Test
    void should_use_empty_strings_when_optional_fields_missing() {
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(Map.of());
        when(systemDateProvider.dueDate(daysToWaitAfterManageFeeUpdate)).thenReturn("02 Jan 2026");

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PREVIOUS_FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(MANAGE_FEE_REQUESTED_AMOUNT, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.empty());

        try (MockedStatic<AsylumCaseUtils> utils = mockStatic(AsylumCaseUtils.class);
             MockedStatic<CommonUtils> common = mockStatic(CommonUtils.class)) {

            utils.when(() -> AsylumCaseUtils.getSponserAddressAsList(asylumCase))
                    .thenReturn(List.of("addr1"));

            common.when(() -> CommonUtils.convertAsylumCaseFeeValue("")).thenReturn("");

            Map<String, String> result = personalisation.getPersonalisation(asylumCase);

            assertEquals("", result.get("appealReferenceNumber"));
            assertEquals("", result.get("legalRepReferenceNumber"));
            assertEquals("", result.get("appellantGivenNames"));
            assertEquals("", result.get("appellantFamilyName"));
            assertEquals("", result.get("originalFee"));
            assertEquals("", result.get("newFee"));
            assertEquals("", result.get("additionalFee"));
            assertEquals("", result.get("feeUpdateReason"));
            assertEquals("", result.get("onlineCaseReferenceNumber"));
            assertEquals("02 Jan 2026", result.get("dueDate"));
            assertEquals("addr1", result.get("address_line_1"));
        }
    }

    @Test
    void should_generate_address_lines_for_variable_length_address_list() {
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(Map.of());
        when(systemDateProvider.dueDate(daysToWaitAfterManageFeeUpdate)).thenReturn("03 Jan 2026");

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("appealRef"));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("legalRef"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("given"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("family"));
        when(asylumCase.read(PREVIOUS_FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("100"));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("200"));
        when(asylumCase.read(MANAGE_FEE_REQUESTED_AMOUNT, String.class)).thenReturn(Optional.of("300"));
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of("ref"));

        try (MockedStatic<AsylumCaseUtils> utils = mockStatic(AsylumCaseUtils.class);
             MockedStatic<CommonUtils> common = mockStatic(CommonUtils.class)) {

            utils.when(() -> AsylumCaseUtils.getSponserAddressAsList(asylumCase))
                    .thenReturn(List.of("only-line"));

            common.when(() -> CommonUtils.convertAsylumCaseFeeValue("100")).thenReturn("1.00");
            common.when(() -> CommonUtils.convertAsylumCaseFeeValue("200")).thenReturn("2.00");
            common.when(() -> CommonUtils.convertAsylumCaseFeeValue("300")).thenReturn("3.00");

            Map<String, String> result = personalisation.getPersonalisation(asylumCase);

            assertEquals("only-line", result.get("address_line_1"));
            assertNull(result.get("address_line_2"));
            assertNull(result.get("address_line_3"));
        }
    }
}
