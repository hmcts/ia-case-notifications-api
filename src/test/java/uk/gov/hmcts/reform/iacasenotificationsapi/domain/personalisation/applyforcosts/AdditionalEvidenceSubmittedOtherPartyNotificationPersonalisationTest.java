package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdditionalEvidenceSubmittedOtherPartyNotificationPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;

    private final String additionalEvidenceForCostsHoId = "additionalEvidenceForCostsHoId";
    private final String homeOfficeEmailAddress = "homeOfficeEmailAddress@gmail.com";
    private final String legalRepEmailAddress = "legalRepEmailAddress@gmail.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private static final String homeOffice = "Home office";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";
    private AdditionalEvidenceSubmittedOtherPartyNotificationPersonalisation additionalEvidenceSubmittedOtherPartyNotificationPersonalisation;

    @BeforeEach
    void setup() {
        additionalEvidenceSubmittedOtherPartyNotificationPersonalisation = new AdditionalEvidenceSubmittedOtherPartyNotificationPersonalisation(
            additionalEvidenceForCostsHoId,
            homeOfficeEmailAddress,
            emailAddressFinder,
            customerServicesProvider,
            personalisationProvider
        );

        Map<String, String> additionalEvidenceSubmittedHoPersonalisationTemplate = new HashMap<>();

        additionalEvidenceSubmittedHoPersonalisationTemplate.put("appellantGivenNames", appellantGivenNames);
        additionalEvidenceSubmittedHoPersonalisationTemplate.put("appellantFamilyName", appellantFamilyName);
        additionalEvidenceSubmittedHoPersonalisationTemplate.put("appealReferenceNumber", appealReferenceNumber);

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(personalisationProvider.getApplyForCostsPersonalisation(asylumCase)).thenReturn(additionalEvidenceSubmittedHoPersonalisationTemplate);
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
        String homeOfficeReferenceNumber = "A1234567/001";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        String legalRepRefNumber = "someLegalRepRefNumber";
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        String ariaReferenceNumber = "ariaReferenceNumber";
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaReferenceNumber));
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(additionalEvidenceForCostsHoId, additionalEvidenceSubmittedOtherPartyNotificationPersonalisation.getTemplateId(asylumCase));
    }


    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_ADDITIONAL_EVIDENCE_OTHER_PARTY_EMAIL",
            additionalEvidenceSubmittedOtherPartyNotificationPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @MethodSource("appliesForCostsProviderWithJudge")
    void should_return_given_email_address(List<IdValue<ApplyForCosts>> applyForCostsList, DynamicList addEvidenceToCostsList) {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));
        when(asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(addEvidenceToCostsList));

        if (("Tribunal").equals(applyForCostsList.get(0).getValue().getApplyForCostsApplicantType())) {
            assertTrue(additionalEvidenceSubmittedOtherPartyNotificationPersonalisation.getRecipientsList(asylumCase).isEmpty());
        } else if (applyForCostsList.get(0).getValue().getLoggedUserRole().equals(homeOffice)) {
            assertTrue(additionalEvidenceSubmittedOtherPartyNotificationPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
        } else {
            assertTrue(additionalEvidenceSubmittedOtherPartyNotificationPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
        }
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
assertThrows(NullPointerException.class, () -> additionalEvidenceSubmittedOtherPartyNotificationPersonalisation.getPersonalisation((AsylumCase) null))
            ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("appliesForCostsProviderWithJudge")
    public void should_return_personalisation_when_all_information_given(List<IdValue<ApplyForCosts>> applyForCostsList, DynamicList addEvidenceToCostsList) {
        when(personalisationProvider.getTypeForSelectedApplyForCosts(any(), any())).thenReturn(Map.of("appliedCostsType", applyForCostsList.get(0).getValue().getAppliedCostsType().replaceAll("costs", "").trim()));
        when(personalisationProvider.retrieveSelectedApplicationId(any(), any())).thenReturn(Map.of("applicationId", applyForCostsList.get(0).getId()));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));
        when(asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(addEvidenceToCostsList));

        Map<String, String> personalisation = additionalEvidenceSubmittedOtherPartyNotificationPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("applicationId", applyForCostsList.get(0).getId());
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

        if (applyForCostsList.get(0).getValue().getLoggedUserRole().equals("Home office")) {
            String expectedAriaReferenceNumber = "\nListing reference: ariaReferenceNumber";
            String expectedLegalRepRefNumber = "\nYour reference: someLegalRepRefNumber";
            assertThat(personalisation)
                .containsEntry("appliedCostsType", "Wasted")
                .containsEntry("homeOfficeReferenceNumber", "")
                .containsEntry("legalRepReferenceNumber", expectedLegalRepRefNumber)
                .containsEntry("ariaListingReference", expectedAriaReferenceNumber);
        } else {
            String expectedHomeOfficeReferenceNumber = "\nHome Office reference: A1234567/001";
            assertThat(personalisation)
                .containsEntry("appliedCostsType", "Unreasonable")
                .containsEntry("homeOfficeReferenceNumber", expectedHomeOfficeReferenceNumber)
                .containsEntry("legalRepReferenceNumber", "")
                .containsEntry("ariaListingReference", "");
        }
    }

    static Stream<Arguments> appliesForCostsProviderWithJudge() {
        return Stream.of(
            Arguments.of(List.of(new IdValue<>("1", new ApplyForCosts("Legal representative", "Legal representative", homeOffice, "Unreasonable costs", "24 Nov 2023"))),
                new DynamicList(new Value("1", "Costs 1, Unreasonable costs, 24 Nov 2023"), List.of(new Value("1", "Costs 1, Unreasonable costs, 24 Nov 2023")))),
            Arguments.of(List.of(new IdValue<>("2", new ApplyForCosts(homeOffice, homeOffice, "Legal representative", "Wasted costs", "24 Nov 2023"))),
                new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")))),
            Arguments.of(List.of(new IdValue<>("3", new ApplyForCosts(homeOffice, "Tribunal", homeOffice, "Wasted costs", "24 Nov 2023"))),
                new DynamicList(new Value("3", "Costs 3, Wasted costs, 24 Nov 2023"), List.of(new Value("3", "Costs 3, Wasted costs, 24 Nov 2023"))))
        );
    }
}
