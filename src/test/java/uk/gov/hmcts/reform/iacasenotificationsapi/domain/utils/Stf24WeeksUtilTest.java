package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class Stf24WeeksUtilTest {

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private CustomerServicesProvider customerServicesProvider;

    @Mock
    private DateTimeExtractor dateTimeExtractor;

    @Mock
    private HearingDetailsFinder hearingDetailsFinder;

    @Test
    void should_return_appellant_given_name_when_present() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("John"));

        assertEquals("John", Stf24WeeksUtil.getAppellantGivenName(asylumCase));
    }

    @Test
    void should_return_empty_string_when_appellant_given_name_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.empty());

        assertEquals("", Stf24WeeksUtil.getAppellantGivenName(asylumCase));
    }

    @Test
    void should_return_appellant_family_name_when_present() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Doe"));

        assertEquals("Doe", Stf24WeeksUtil.getAppellantFamilyName(asylumCase));
    }

    @Test
    void should_return_empty_string_when_appellant_family_name_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.empty());

        assertEquals("", Stf24WeeksUtil.getAppellantFamilyName(asylumCase));
    }

    @Test
    void should_return_legal_rep_reference_number_from_primary_field() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("REF123"));

        assertEquals("REF123", Stf24WeeksUtil.getLegalRepReferenceNo(asylumCase));
    }

    @Test
    void should_return_legal_rep_reference_number_from_fallback_field_when_primary_empty() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J, String.class))
                .thenReturn(Optional.of("PAPER456"));

        assertEquals("PAPER456", Stf24WeeksUtil.getLegalRepReferenceNo(asylumCase));
    }

    @Test
    void should_return_empty_string_when_both_legal_rep_reference_fields_empty() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J, String.class))
                .thenReturn(Optional.empty());

        assertEquals("", Stf24WeeksUtil.getLegalRepReferenceNo(asylumCase));
    }

    @Test
    void should_return_false_when_legal_rep_reference_number_present() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("REF123"));

        assertFalse(Stf24WeeksUtil.noLegalRepresentation(asylumCase));
    }

    @Test
    void should_return_true_when_legal_rep_reference_number_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J, String.class))
                .thenReturn(Optional.empty());

        assertTrue(Stf24WeeksUtil.noLegalRepresentation(asylumCase));
    }

    @Test
    void should_return_true_when_only_fallback_legal_rep_field_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J, String.class))
                .thenReturn(Optional.empty());

        assertTrue(Stf24WeeksUtil.noLegalRepresentation(asylumCase));
    }

    @Test
    void should_return_personalisation_for_appellant_hearing_requirements_letter() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Jane"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Doe"));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of("APP-123"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class))
            .thenReturn(Optional.of(new AddressUk("10", "Main St", "", "Sometown", "", "CM3 4DC", "UK")));
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_OTHER, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(Map.of(
            "customerServicesTelephone", "0123 456 789",
            "customerServicesEmail", "customer.services@example.com"
        ));
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn("2024-12-12T09:00:00");
        when(dateTimeExtractor.extractHearingDate("2024-12-12T09:00:00")).thenReturn("12 Dec 2024");
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn("Manchester Civil Justice Centre");

        Map<String, String> personalisation = Stf24WeeksUtil.buildHearingRequirementsLetterParameters(
            Stf24WeeksUtil.Stf24WeeksNotificationFor.APPELLANT,
            asylumCase,
            customerServicesProvider,
            dateTimeExtractor,
            hearingDetailsFinder
        );

        assertEquals("Jane", personalisation.get("appellantGivenNames"));
        assertEquals("Doe", personalisation.get("appellantFamilyName"));
        assertEquals("APP-123", personalisation.get("appealReferenceNumber"));
        assertEquals("12 Dec 2024", personalisation.get("hearingDate"));
        assertEquals("Manchester Civil Justice Centre", personalisation.get("hearingCentreAddress"));
        assertTrue(personalisation.get("legalSupportInfo").contains("#How to find legal support and more information"));
        assertEquals("Jane Doe", personalisation.get("address_line_1"));
        assertEquals("10", personalisation.get("address_line_2"));
    }

    @Test
    void should_return_personalisation_for_legal_rep_hearing_requirements_letter() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class))
            .thenReturn(Optional.of(new AddressUk("20", "High Street", "", "Leeds", "", "LS1 1AA", "UK")));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of("LR-123"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Jane"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Doe"));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of("APP-123"));
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_OTHER, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(Map.of(
            "customerServicesTelephone", "0123 456 789",
            "customerServicesEmail", "customer.services@example.com"
        ));
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn("2024-12-12T09:00:00");
        when(dateTimeExtractor.extractHearingDate("2024-12-12T09:00:00")).thenReturn("12 Dec 2024");
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn("Manchester Civil Justice Centre");

        Map<String, String> personalisation = Stf24WeeksUtil.buildHearingRequirementsLetterParameters(
            Stf24WeeksUtil.Stf24WeeksNotificationFor.LEGAL_REPRESENTATIVE,
            asylumCase,
            customerServicesProvider,
            dateTimeExtractor,
            hearingDetailsFinder
        );

        assertEquals("Your reference:LR-123", personalisation.get("lrReferenceWithText"));
        assertEquals("", personalisation.get("legalSupportInfo"));
        assertEquals("20", personalisation.get("address_line_1"));
        assertEquals("High Street", personalisation.get("address_line_2"));
    }

    @Test
    void should_return_true_for_review_hearing_requirements_when_24_week_status_is_yes() {
        when(asylumCase.read(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(Stf24WeeksUtil.isHearingRequirementsFor24WeeksCase(Event.REVIEW_HEARING_REQUIREMENTS, asylumCase));
    }

    @Test
    void should_return_false_for_non_review_hearing_requirements_event() {
        when(asylumCase.read(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertFalse(Stf24WeeksUtil.isHearingRequirementsFor24WeeksCase(Event.COMPLETE_CASE_REVIEW, asylumCase));
    }

    @Test
    void should_return_false_when_24_week_status_is_no() {
        when(asylumCase.read(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(Stf24WeeksUtil.isHearingRequirementsFor24WeeksCase(Event.REVIEW_HEARING_REQUIREMENTS, asylumCase));
    }

    @Test
    void should_return_true_when_hearing_requirements_can_run_for_24_week_case() {
        when(asylumCase.read(AsylumCaseDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(Stf24WeeksUtil.canRunHearingReq(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            Event.REVIEW_HEARING_REQUIREMENTS,
            asylumCase,
            true
        ));
    }

    @Test
    void should_return_false_when_hearing_requirements_run_for_internal_case() {
        when(asylumCase.read(AsylumCaseDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertFalse(Stf24WeeksUtil.canRunHearingReq(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            Event.REVIEW_HEARING_REQUIREMENTS,
            asylumCase,
            true
        ));
    }

    @Test
    void should_throw_exception_when_asylum_case_is_null() {
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
            Stf24WeeksUtil.buildHearingRequirementsLetterParameters(
                Stf24WeeksUtil.Stf24WeeksNotificationFor.APPELLANT,
                null,
                customerServicesProvider,
                dateTimeExtractor,
                hearingDetailsFinder
            )
        );

        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_use_empty_strings_when_optional_appellant_fields_are_missing() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class))
            .thenReturn(Optional.of(new AddressUk("10", "Main St", "", "Sometown", "", "CM3 4DC", "UK")));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_OTHER, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(Map.of(
            "customerServicesTelephone", "0123 456 789",
            "customerServicesEmail", "customer.services@example.com"
        ));
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn("2024-12-12T09:00:00");
        when(dateTimeExtractor.extractHearingDate("2024-12-12T09:00:00")).thenReturn("12 Dec 2024");
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn("Manchester Civil Justice Centre");

        Map<String, String> personalisation = Stf24WeeksUtil.buildHearingRequirementsLetterParameters(
            Stf24WeeksUtil.Stf24WeeksNotificationFor.APPELLANT,
            asylumCase,
            customerServicesProvider,
            dateTimeExtractor,
            hearingDetailsFinder
        );

        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("appealReferenceNumber"));
    }

    @Test
    void should_return_email_personalisation_for_appellant_hearing_requirements() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Jane"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Doe"));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of("APP-123"));
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_OTHER, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(Map.of(
            "customerServicesTelephone", "0123 456 789",
            "customerServicesEmail", "customer.services@example.com"
        ));
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn("2024-12-12T09:00:00");
        when(dateTimeExtractor.extractHearingDate("2024-12-12T09:00:00")).thenReturn("12 Dec 2024");
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn("Manchester Civil Justice Centre");

        Map<String, String> personalisation = Stf24WeeksUtil.buildHearingRequirementsEmailParams(
            Stf24WeeksUtil.Stf24WeeksNotificationFor.APPELLANT,
            asylumCase,
            "A",
            "https://example.test",
            customerServicesProvider,
            dateTimeExtractor,
            hearingDetailsFinder
        );

        assertEquals("A", personalisation.get("subjectPrefix"));
        assertEquals("https://example.test", personalisation.get("linkToOnlineService"));
        assertEquals("Jane", personalisation.get("appellantGivenNames"));
        assertEquals("Doe", personalisation.get("appellantFamilyName"));
        assertEquals("APP-123", personalisation.get("appealReferenceNumber"));
        assertEquals("12 Dec 2024", personalisation.get("hearingDate"));
        assertEquals("Manchester Civil Justice Centre", personalisation.get("hearingCentreAddress"));
        assertEquals("No special adjustments are being made to accommodate vulnerabilities",
            personalisation.get("hearingRequirementVulnerabilities"));
    }

    @Test
    void should_return_empty_strings_when_optional_email_fields_are_missing() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_OTHER, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(Map.of(
            "customerServicesTelephone", "0123 456 789",
            "customerServicesEmail", "customer.services@example.com"
        ));
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn("2024-12-12T09:00:00");
        when(dateTimeExtractor.extractHearingDate("2024-12-12T09:00:00")).thenReturn("12 Dec 2024");
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn("Manchester Civil Justice Centre");

        Map<String, String> personalisation = Stf24WeeksUtil.buildHearingRequirementsEmailParams(
            Stf24WeeksUtil.Stf24WeeksNotificationFor.APPELLANT,
            asylumCase,
            "",
            "https://example.test",
            customerServicesProvider,
            dateTimeExtractor,
            hearingDetailsFinder
        );

        assertEquals("", personalisation.get("subjectPrefix"));
        assertEquals("https://example.test", personalisation.get("linkToOnlineService"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("appealReferenceNumber"));
    }

    @Test
    void should_return_false_when_callback_stage_is_not_about_to_submit() {

        assertFalse(Stf24WeeksUtil.canRunHearingReq(
            PreSubmitCallbackStage.ABOUT_TO_START,
            Event.REVIEW_HEARING_REQUIREMENTS,
            asylumCase,
            true
        ));
    }

    @Test
    void should_return_false_when_24_week_flag_is_disabled() {
        when(asylumCase.read(AsylumCaseDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(Stf24WeeksUtil.canRunHearingReq(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            Event.REVIEW_HEARING_REQUIREMENTS,
            asylumCase,
            false
        ));
    }

    @Test
    void should_return_false_when_case_is_internal() {
        when(asylumCase.read(AsylumCaseDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertFalse(Stf24WeeksUtil.canRunHearingReq(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            Event.REVIEW_HEARING_REQUIREMENTS,
            asylumCase,
            true
        ));
    }

    @Test
    void should_return_false_when_event_is_not_review_hearing_requirements() {
        when(asylumCase.read(AsylumCaseDefinition.IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertFalse(Stf24WeeksUtil.canRunHearingReq(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            Event.COMPLETE_CASE_REVIEW,
            asylumCase,
            true
        ));
    }

    @Test
    void should_return_true_for_review_hearing_requirements_internal_case_when_24_week_status_is_yes() {
        when(asylumCase.read(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(Stf24WeeksUtil.canRunHearingReqReviewInternalCase(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            Event.REVIEW_HEARING_REQUIREMENTS,
            asylumCase,
            true
        ));
    }

    @Test
    void should_return_false_for_review_hearing_requirements_internal_case_when_callback_stage_is_not_about_to_submit() {
        assertFalse(Stf24WeeksUtil.canRunHearingReqReviewInternalCase(
            PreSubmitCallbackStage.ABOUT_TO_START,
            Event.REVIEW_HEARING_REQUIREMENTS,
            asylumCase,
            true
        ));
    }

    @Test
    void should_return_false_for_review_hearing_requirements_internal_case_when_24_week_flag_is_disabled() {
        assertFalse(Stf24WeeksUtil.canRunHearingReqReviewInternalCase(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            Event.REVIEW_HEARING_REQUIREMENTS,
            asylumCase,
            false
        ));
    }

    @Test
    void should_return_false_for_review_hearing_requirements_internal_case_when_event_is_not_review_hearing_requirements() {
        when(asylumCase.read(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertFalse(Stf24WeeksUtil.canRunHearingReqReviewInternalCase(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            Event.COMPLETE_CASE_REVIEW,
            asylumCase,
            true
        ));
    }

    @Test
    void should_return_false_for_review_hearing_requirements_internal_case_when_24_week_status_is_no() {
        when(asylumCase.read(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(Stf24WeeksUtil.canRunHearingReqReviewInternalCase(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            Event.REVIEW_HEARING_REQUIREMENTS,
            asylumCase,
            true
        ));
    }
}

