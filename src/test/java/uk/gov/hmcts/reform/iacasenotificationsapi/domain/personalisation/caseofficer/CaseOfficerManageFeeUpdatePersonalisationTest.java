package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseOfficerManageFeeUpdatePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;
    private final String ctscManageFeeUpdateBeforeListingTemplateId = "ctscBeforeListTemplateId";
    private final String ctscManageFeeUpdateAfterListingTemplateId = "ctscAfterListTemplateId";
    private final String nbcManageFeeUpdateBeforeListingTemplateId = "nbcBeforeListTemplateId";
    private final String nbcManageFeeUpdateAfterListingTemplateId = "nbcAfterListTemplateId";

    private final String iaExUiFrontendUrl = "http://somefrontendurl";
    private final String nbcEmailAddress = "nbc-review@example.com";
    private final String ctscEmailAddress = "ctsc-review@example.com";

    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";

    private CaseOfficerManageFeeUpdatePersonalisation caseOfficerManageFeeUpdatePersonalisation;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        caseOfficerManageFeeUpdatePersonalisation = new CaseOfficerManageFeeUpdatePersonalisation(
            ctscManageFeeUpdateBeforeListingTemplateId,
            ctscManageFeeUpdateAfterListingTemplateId,
            nbcManageFeeUpdateBeforeListingTemplateId,
            nbcManageFeeUpdateAfterListingTemplateId,
            nbcEmailAddress,
            ctscEmailAddress,
            iaExUiFrontendUrl,

                featureToggler);
    }

    @Test
    void should_return_ctsc_template_id_for_PBa_PA_before_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        assertEquals(ctscManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_ctsc_template_id_for_PBa_EA_before_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));

        assertEquals(ctscManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_ctsc_template_id_for_PBa_HU_before_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));

        assertEquals(ctscManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_ctsc_template_id_for_PBa_EU_before_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EU));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));

        assertEquals(ctscManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_ctsc_template_id_for_PBa_PA_after_listing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        assertEquals(ctscManageFeeUpdateAfterListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_ctsc_template_id_for_PBa_HU_after_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));

        assertEquals(ctscManageFeeUpdateAfterListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_ctsc_template_id_for_PBa_EU_after_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EU));

        assertEquals(ctscManageFeeUpdateAfterListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_nbc_template_id_for_PBa_PA_before_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        assertEquals(nbcManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_nbc_template_id_for_PBa_EA_before_listing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        assertEquals(nbcManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_nbc_template_id_for_PBa_HU_before_listing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        assertEquals(nbcManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_nbc_template_id_for_PBa_EU_before_listing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EU));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        assertEquals(nbcManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_nbc_template_id_for_PBa_PA_after_listing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        assertEquals(nbcManageFeeUpdateAfterListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }


    @Test
    void should_return_nbc_template_id_for_PBa_HU_after_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));

        assertEquals(nbcManageFeeUpdateAfterListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_nbc_email_address_hu_pay_by_card_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(nbcEmailAddress));
    }

    @Test
    void should_return_given_nbc_email_address_ea_pay_by_card_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(nbcEmailAddress));
    }

    @Test
    void should_return_given_nbc_email_address_eu_pay_by_card_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EU));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(nbcEmailAddress));
    }

    @Test
    void should_return_given_nbc_email_address_pa_pay_by_card_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(nbcEmailAddress));
    }

    @Test
    void should_return_given_ctsc_email_address_hu_pay_by_PBa_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(ctscEmailAddress));
    }

    @Test
    void should_return_given_ctsc_email_address_ea_pay_by_PBa_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(ctscEmailAddress));
    }

    @Test
    void should_return_given_ctsc_email_address_eu_pay_by_PBa_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EU));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(ctscEmailAddress));
    }

    @Test
    void should_return_given_ctsc_email_address_pa_pay_by_PBa_now_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(ctscEmailAddress));
    }

    @Test
    void should_return_given_ctsc_email_address_pa_pay_by_PBa_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payLater"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(ctscEmailAddress));
    }

    @Test
    void should_return_given_nbc_email_address_when_feature_flag_is_Off() {
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
                .isEmpty());
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_MANAGE_FEE_UPDATE_CASE_OFFICER",
            caseOfficerManageFeeUpdatePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
assertThrows(NullPointerException.class, () -> caseOfficerManageFeeUpdatePersonalisation.getPersonalisation((AsylumCase) null))
            ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_email_when_case_is_null() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());
        IllegalStateException exception =
assertThrows(IllegalStateException.class, () -> caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase))
            ;
assertEquals("Email Address cannot be found", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_template_when_case_is_null() {
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());
        IllegalStateException exception =
assertThrows(IllegalStateException.class, () -> caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase))
            ;
assertEquals("Template cannot be found", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(caseOfficerManageFeeUpdatePersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> personalisation =
            caseOfficerManageFeeUpdatePersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(caseOfficerManageFeeUpdatePersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            caseOfficerManageFeeUpdatePersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("ariaListingReference", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }
}
