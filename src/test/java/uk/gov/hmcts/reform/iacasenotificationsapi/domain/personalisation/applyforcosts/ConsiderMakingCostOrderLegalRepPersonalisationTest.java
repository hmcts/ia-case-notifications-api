package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import java.util.HashMap;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsiderMakingCostOrderLegalRepPersonalisationTest {
    private static final String applyForCostsCreationDate = "2023-11-24";
    private final String templateId = "testTemplateId";
    private final String legalRepEmailAddress = "legalRepEmailAddress@gmail.com";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String legalRepRefNumber = "someLegalRepRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";
    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;
    private ConsiderMakingCostOrderLegalRepPersonalisation considerMakingCostOrderLegalRepPersonalisation;

    @BeforeEach
    void setup() {
        considerMakingCostOrderLegalRepPersonalisation = new ConsiderMakingCostOrderLegalRepPersonalisation(
            templateId,
            emailAddressFinder,
            customerServicesProvider,
            personalisationProvider
        );

        Map<String, String> applyForCostsApplicantPersonalisationTemplate = new HashMap<>();

        applyForCostsApplicantPersonalisationTemplate.put("appellantGivenNames", appellantGivenNames);
        applyForCostsApplicantPersonalisationTemplate.put("appellantFamilyName", appellantFamilyName);
        applyForCostsApplicantPersonalisationTemplate.put("appealReferenceNumber", appealReferenceNumber);
        applyForCostsApplicantPersonalisationTemplate.put("linkToOnlineService", iaExUiFrontendUrl);

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(personalisationProvider.getApplyForCostsPersonalisation(asylumCase)).thenReturn(applyForCostsApplicantPersonalisationTemplate);
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, considerMakingCostOrderLegalRepPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_CONSIDER_MAKING_A_COST_ORDER_LEGAL_REP_EMAIL",
            considerMakingCostOrderLegalRepPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address() {
        assertTrue(considerMakingCostOrderLegalRepPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> considerMakingCostOrderLegalRepPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = considerMakingCostOrderLegalRepPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(legalRepRefNumber, personalisation.get("legalRepReferenceNumber"));
    }

}