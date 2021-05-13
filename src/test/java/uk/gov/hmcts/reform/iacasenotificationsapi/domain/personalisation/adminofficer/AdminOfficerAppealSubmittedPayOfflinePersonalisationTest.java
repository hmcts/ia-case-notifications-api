package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType;

@ExtendWith(MockitoExtension.class)
class AdminOfficerAppealSubmittedPayOfflinePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String remissionTemplateId = "someRemissionTemplateId";
    private String changeToHearingRequirementsAdminOfficerEmailAddress = "fees-ao@example.com";
    private String paymentExceptionsAdminOfficerEmailAddress = "payment-exceptions-ao@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String ariaListingReference = "someAriaListingReference";
    private String linkToOnlineService = "someLinkToOnlineService";
    private AdminOfficerAppealSubmittedPayOfflinePersonalisation adminOfficerAppealSubmittedPayOfflinePersonalisation;

    @BeforeEach
    void setup() {

        adminOfficerAppealSubmittedPayOfflinePersonalisation = new AdminOfficerAppealSubmittedPayOfflinePersonalisation(
            templateId,
            remissionTemplateId,
            changeToHearingRequirementsAdminOfficerEmailAddress,
            paymentExceptionsAdminOfficerEmailAddress,
            adminOfficerPersonalisationProvider
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerAppealSubmittedPayOfflinePersonalisation.getTemplateId(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(
        value = RemissionType.class,
        names = {"HO_WAIVER_REMISSION", "HELP_WITH_FEES", "EXCEPTIONAL_CIRCUMSTANCES_REMISSION"})
    void should_return_given_template_id_with_remission(RemissionType remissionType) {
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        assertEquals(
            remissionTemplateId, adminOfficerAppealSubmittedPayOfflinePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {

        assertEquals(caseId + "_APPEAL_SUBMITTED_PAY_OFFLINE_ADMIN_OFFICER",
            adminOfficerAppealSubmittedPayOfflinePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerAppealSubmittedPayOfflinePersonalisation.getRecipientsList(asylumCase)
            .contains(changeToHearingRequirementsAdminOfficerEmailAddress));
    }

    @ParameterizedTest
    @EnumSource(
        value = RemissionType.class,
        names = {"HO_WAIVER_REMISSION", "HELP_WITH_FEES", "EXCEPTIONAL_CIRCUMSTANCES_REMISSION"})
    void should_return_payment_email_address_with_remission(RemissionType remissionType) {
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        assertTrue(adminOfficerAppealSubmittedPayOfflinePersonalisation.getRecipientsList(asylumCase)
            .contains(paymentExceptionsAdminOfficerEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> adminOfficerAppealSubmittedPayOfflinePersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase)).thenReturn((ImmutableMap<String, String>) getPersonalisation());

        Map<String, String> personalisation =
            adminOfficerAppealSubmittedPayOfflinePersonalisation.getPersonalisation(asylumCase);
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(linkToOnlineService, personalisation.get("linkToOnlineService"));
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {
        when(adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase)).thenReturn((ImmutableMap<String, String>) getEmptyPersonalisation());

        Map<String, String> personalisation =
            adminOfficerAppealSubmittedPayOfflinePersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(linkToOnlineService, personalisation.get("linkToOnlineService"));
    }

    private Map<String, String> getPersonalisation() {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("linkToOnlineService", linkToOnlineService)
            .build();
    }

    private Map<String, String> getEmptyPersonalisation() {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "")
            .put("ariaListingReference", "")
            .put("appellantGivenNames", "")
            .put("appellantFamilyName", "")
            .put("linkToOnlineService", linkToOnlineService)
            .build();
    }
}
