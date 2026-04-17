package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplyForCostsApplicantPersonalisationTest {
    private static final String applyForCostsCreationDate = "2023-11-24";
    private static final String homeOffice = "Home office";
    private final String applyForCostsNotificationForApplicantTemplateId = "applyForCostsNotificationForRespondentTemplateId";
    private final String homeOfficeEmailAddress = "homeOfficeEmailAddress@gmail.com";
    private final String legalRepEmailAddress = "legalRepEmailAddress@gmail.com";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String legalRepRefNumber = "someLegalRepRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;
    private ApplyForCostsApplicantPersonalisation applyForCostsApplicantPersonalisation;

    static Stream<Arguments> appliesForCostsProvider() {
        String unreasonableCostsType = "Unreasonable costs";
        String newestApplicationCreatedNumber = "1";
        return Stream.of(
            Arguments.of(List.of(new IdValue<>(newestApplicationCreatedNumber, new ApplyForCosts(unreasonableCostsType, "Legal representative", homeOffice, applyForCostsCreationDate)))),
            Arguments.of(List.of(new IdValue<>(newestApplicationCreatedNumber, new ApplyForCosts("Wasted costs", homeOffice, "Legal representative", applyForCostsCreationDate))))
        );
    }

    @BeforeEach
    void setup() {
        applyForCostsApplicantPersonalisation = new ApplyForCostsApplicantPersonalisation(
            applyForCostsNotificationForApplicantTemplateId,
            homeOfficeEmailAddress,
            emailAddressFinder,
            customerServicesProvider,
            personalisationProvider
        );

        Map<String, String> applyForCostsApplicantPersonalisationTemplate = new HashMap<>();

        applyForCostsApplicantPersonalisationTemplate.put("appellantGivenNames", appellantGivenNames);
        applyForCostsApplicantPersonalisationTemplate.put("appellantFamilyName", appellantFamilyName);
        applyForCostsApplicantPersonalisationTemplate.put("appealReferenceNumber", appealReferenceNumber);
        applyForCostsApplicantPersonalisationTemplate.put("linkToOnlineService", iaExUiFrontendUrl);
        applyForCostsApplicantPersonalisationTemplate.put("appliedCostsType", "Wasted");

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(personalisationProvider.getApplyForCostsPersonalisation(asylumCase)).thenReturn(applyForCostsApplicantPersonalisationTemplate);
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));

        Map<String, String> homeOfficeRecipientHeader = new HashMap<>();
        homeOfficeRecipientHeader.put("recipient", homeOffice);
        homeOfficeRecipientHeader.put("recipientReferenceNumber", homeOfficeReferenceNumber);

        Map<String, String> legalRepRecipientHeader = new HashMap<>();
        legalRepRecipientHeader.put("recipient", "Your");
        legalRepRecipientHeader.put("recipientReferenceNumber", legalRepRefNumber);

        when(personalisationProvider.getHomeOfficeRecipientHeader(asylumCase)).thenReturn(homeOfficeRecipientHeader);
        when(personalisationProvider.getLegalRepRecipientHeader(asylumCase)).thenReturn(legalRepRecipientHeader);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(applyForCostsNotificationForApplicantTemplateId, applyForCostsApplicantPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_APPLY_FOR_COSTS_APPLICANT_EMAIL",
            applyForCostsApplicantPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @MethodSource("appliesForCostsProvider")
    void should_return_given_email_address(List<IdValue<ApplyForCosts>> applyForCostsList) {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        if (applyForCostsList.getFirst().getValue().getApplyForCostsApplicantType().equals(homeOffice)) {
            assertTrue(applyForCostsApplicantPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
        } else {
            assertTrue(applyForCostsApplicantPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
        }
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> applyForCostsApplicantPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("appliesForCostsProvider")
    void should_return_personalisation_when_all_information_given(List<IdValue<ApplyForCosts>> applyForCostsList) {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        Map<String, String> applyForCostsCreatedDateMap = new HashMap<>();
        applyForCostsCreatedDateMap.put("creationDate", "24 Nov 2023");
        when(personalisationProvider.getApplyToCostsCreationDate(asylumCase)).thenReturn(applyForCostsCreatedDateMap);

        Map<String, String> personalisation = applyForCostsApplicantPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertThat(personalisation)
            .containsEntry("appliedCostsType", "Wasted")
            .containsEntry("creationDate", "24 Nov 2023");

        if (applyForCostsList.getFirst().getValue().getApplyForCostsApplicantType().equals("Home office")) {
            assertThat(personalisation)
                .containsEntry("recipient", "Home office")
                .containsEntry("recipientReferenceNumber", homeOfficeReferenceNumber);
        } else {
            assertThat(personalisation)
                .containsEntry("recipient", "Your")
                .containsEntry("recipientReferenceNumber", legalRepRefNumber);
        }
    }
}