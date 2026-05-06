package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

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
class DecideCostsLegalRepPersonalisationTest {

    private final String decideCostsNotificationId = "decideCostsNotificationId";
    private final String homeOfficeEmailAddress = "homeOfficeEmailAddress@gmail.com";
    private final String legalRepEmailAddress = "legalRepEmailAddress@gmail.com";
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
    private DecideCostsLegalRepPersonalisation decideCostsLegalRepPersonalisation;

    static Stream<Arguments> appliesForCostsProvider() {
        String homeOffice = "Home office";
        return Stream.of(
            Arguments.of(List.of(new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", homeOffice))),
                new DynamicList(new Value("1", "Costs 1, Unreasonable costs, 24 Nov 2023"), List.of(new Value("1", "Costs 1, Unreasonable costs, 24 Nov 2023")))),
            Arguments.of(List.of(new IdValue<>("2", new ApplyForCosts("Wasted costs", homeOffice, "Legal representative"))),
                new DynamicList(new Value("2", "Costs 1, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 1, Wasted costs, 24 Nov 2023"))))
        );
    }

    @BeforeEach
    void setup() {
        decideCostsLegalRepPersonalisation = new DecideCostsLegalRepPersonalisation(
            decideCostsNotificationId,
            emailAddressFinder,
            customerServicesProvider,
            personalisationProvider
        );

        Map<String, String> decideCostsRespondentAndApplicantPersonalisationTemplate = new HashMap<>();

        decideCostsRespondentAndApplicantPersonalisationTemplate.put("appellantGivenNames", appellantGivenNames);
        decideCostsRespondentAndApplicantPersonalisationTemplate.put("appellantFamilyName", appellantFamilyName);
        decideCostsRespondentAndApplicantPersonalisationTemplate.put("appealReferenceNumber", appealReferenceNumber);

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(personalisationProvider.getApplyForCostsPersonalisation(asylumCase)).thenReturn(decideCostsRespondentAndApplicantPersonalisationTemplate);
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
        String homeOfficeReferenceNumber = "A1234567/001";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        String ariaRefNumber = "ariaRefNumber";
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaRefNumber));
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(decideCostsNotificationId, decideCostsLegalRepPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_DECIDE_A_COSTS_EMAIL_TO_LR",
            decideCostsLegalRepPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address() {
        Set<String> recipientsSet = decideCostsLegalRepPersonalisation.getRecipientsList(asylumCase);
        assertTrue(recipientsSet.contains(legalRepEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> decideCostsLegalRepPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("appliesForCostsProvider")
    public void should_return_personalisation_when_all_information_given(List<IdValue<ApplyForCosts>> applyForCostsList) {
        when(personalisationProvider.getTypeForSelectedApplyForCosts(any(), any())).thenReturn(Map.of("appliedCostsType", applyForCostsList.getFirst().getValue().getAppliedCostsType().replaceAll("costs", "").trim()));
        when(personalisationProvider.retrieveSelectedApplicationId(any(), any())).thenReturn(Map.of("applicationId", applyForCostsList.getFirst().getId()));
        Map<String, String> decideCostsResult = new HashMap<>();
        decideCostsResult.put("costsDecisionType", "someCostsDecisionType");
        when(personalisationProvider.getDecideCostsPersonalisation(asylumCase)).thenReturn(decideCostsResult);

        Map<String, String> personalisation = decideCostsLegalRepPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("applicationId", applyForCostsList.getFirst().getId());
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        String expectedAriaReferenceNumber = "\nListing reference: ariaRefNumber";
        assertThat(personalisation)
            .containsEntry("costsDecisionType", "someCostsDecisionType")
            .containsEntry("ariaListingReference", expectedAriaReferenceNumber)
            .containsEntry("legalRepReferenceNumber", legalRepRefNumber);
    }
}