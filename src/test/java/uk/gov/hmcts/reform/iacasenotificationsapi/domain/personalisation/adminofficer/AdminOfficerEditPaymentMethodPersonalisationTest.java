package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
class AdminOfficerEditPaymentMethodPersonalisationTest {

    private final String templateEaHuId = "eaHuTemplateId";
    private final String templatePaId = "paTemplateId";
    private final String adminOfficerEmailAddress = "adminOfficer@example.com";
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;
    private AdminOfficerEditPaymentMethodPersonalisation adminOfficerEditPaymentMethodPersonalisation;

    @BeforeEach
    void setUp() {

        adminOfficerEditPaymentMethodPersonalisation =
            new AdminOfficerEditPaymentMethodPersonalisation(
                templateEaHuId,
                templatePaId,
                adminOfficerEmailAddress,
                adminOfficerPersonalisationProvider
            );
    }

    @Test
    void should_return_given_template_id() {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        assertEquals(templateEaHuId, adminOfficerEditPaymentMethodPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        assertEquals(templateEaHuId, adminOfficerEditPaymentMethodPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        assertEquals(templatePaId, adminOfficerEditPaymentMethodPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_EDIT_PAYMENT_METHOD_PENDING_PAYMENT_ADMIN_OFFICER",
            adminOfficerEditPaymentMethodPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerEditPaymentMethodPersonalisation.getRecipientsList(asylumCase)
            .contains(adminOfficerEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> adminOfficerEditPaymentMethodPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(adminOfficerEditPaymentMethodPersonalisation);
        String iaExUiFrontendUrl = "http://localhost";
        String appellantFamilyName = "someAppellantFamilyName";
        String appellantGivenNames = "someAppellantGivenNames";
        String appealReferenceNumber = "someReferenceNumber";
        when(adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase))
            .thenReturn(ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build());

        Map<String, String> personalisation =
            adminOfficerEditPaymentMethodPersonalisation.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
    }
}
