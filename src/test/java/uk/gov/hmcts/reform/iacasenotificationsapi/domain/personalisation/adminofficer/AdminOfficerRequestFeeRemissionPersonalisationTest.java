package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
class AdminOfficerRequestFeeRemissionPersonalisationTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    protected CustomerServicesProvider customerServicesProvider;

    private final String templateId = "applyForLateRemissionTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String paymentExceptionsAdminOfficerEmailAddress = "payment-exceptions-ao@example.com";

    private final String nonAdaPrefix = "Immigration and Asylum appeal";

    private AdminOfficerRequestFeeRemissionPersonalisation adminOfficerRequestFeeRemissionPersonalisation;

    @BeforeEach
    void setUp() {

        adminOfficerRequestFeeRemissionPersonalisation =
            new AdminOfficerRequestFeeRemissionPersonalisation(
                templateId, iaExUiFrontendUrl,
                paymentExceptionsAdminOfficerEmailAddress, customerServicesProvider);

        initializePrefixes(adminOfficerRequestFeeRemissionPersonalisation);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerRequestFeeRemissionPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {

        Long caseId = 12345L;
        assertEquals(caseId + "_ADMIN_OFFICER_LATE_REMISSION_SUBMITTED",
            adminOfficerRequestFeeRemissionPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerRequestFeeRemissionPersonalisation.getRecipientsList(asylumCase)
            .contains(paymentExceptionsAdminOfficerEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> adminOfficerRequestFeeRemissionPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(Map.of());
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("someAppellantGivenNames"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("someAppellantFamilyName"));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("someReferenceNumber"));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("someLegalReferenceNumber"));

        Map<String, String> personalisation =
            adminOfficerRequestFeeRemissionPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsAllEntriesOf(customerServicesProvider.getCustomerServicesPersonalisation())
            .containsEntry("subjectPrefix", nonAdaPrefix)
            .containsEntry("appealReferenceNumber", "someReferenceNumber")
            .containsEntry("legalRepReferenceNumber", "someLegalReferenceNumber")
            .containsEntry("appellantGivenNames", "someAppellantGivenNames")
            .containsEntry("appellantFamilyName", "someAppellantFamilyName")
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);

    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> personalisation =
            adminOfficerRequestFeeRemissionPersonalisation.getPersonalisation(asylumCase);

        String adaPrefix = "Accelerated detained appeal";
        assertThat(personalisation)
            .containsAllEntriesOf(customerServicesProvider.getCustomerServicesPersonalisation())
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? adaPrefix : nonAdaPrefix)
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("legalRepReferenceNumber", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);

    }

}
