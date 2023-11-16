package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPLIES_FOR_COSTS;

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
class RespondToCostsApplicantPersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String templateId = "templateId";
    private String homeOfficeEmailAddress = "homeOfficeEmailAddress@gmail.com";
    private String legalRepEmailAddress = "legalRepEmailAddress@gmail.com";
    private String iaExUiFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepRefNumber = "someLegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private static String homeOffice = "Home office";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private static String newestApplicationCreatedNumber = "1";
    private static String unreasonableCostsType = "Unreasonable costs";
    private String homeOfficeReferenceNumber = "A1234567/001";
    private RespondToCostsApplicantPersonalisation respondToCostsApplicantPersonalisation;

    @BeforeEach
    void setup() {
        respondToCostsApplicantPersonalisation = new RespondToCostsApplicantPersonalisation(
            templateId,
            homeOfficeEmailAddress,
            emailAddressFinder,
            customerServicesProvider,
            personalisationProvider
        );

        Map<String, String> respondToCostsApplicantPersonalisationTemplate = new HashMap<>();

        respondToCostsApplicantPersonalisationTemplate.put("appellantGivenNames", appellantGivenNames);
        respondToCostsApplicantPersonalisationTemplate.put("appellantFamilyName", appellantFamilyName);
        respondToCostsApplicantPersonalisationTemplate.put("appealReferenceNumber", appealReferenceNumber);
        respondToCostsApplicantPersonalisationTemplate.put("linkToOnlineService", iaExUiFrontendUrl);
        respondToCostsApplicantPersonalisationTemplate.put("applicationId", newestApplicationCreatedNumber);
        respondToCostsApplicantPersonalisationTemplate.put("appliedCostsType", "Wasted");

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(personalisationProvider.getApplyForCostsPesonalisation(asylumCase)).thenReturn(respondToCostsApplicantPersonalisationTemplate);
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
        assertEquals(templateId, respondToCostsApplicantPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_RESPOND_TO_COSTS_APPLICANT_EMAIL",
            respondToCostsApplicantPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @MethodSource("appliesForCostsProvider")
    void should_return_given_email_address(List<IdValue<ApplyForCosts>> applyForCostsList) {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        if (applyForCostsList.get(0).getValue().getApplyForCostsApplicantType().equals(homeOffice)) {
            assertTrue(respondToCostsApplicantPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
        } else {
            assertTrue(respondToCostsApplicantPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
        }
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> respondToCostsApplicantPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @MethodSource("appliesForCostsProvider")
    void should_return_personalisation_when_all_information_given(List<IdValue<ApplyForCosts>> applyForCostsList) {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        Map<String, String> personalisation = respondToCostsApplicantPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals("1", personalisation.get("applicationId"));
        assertEquals("Wasted", personalisation.get("appliedCostsType"));

        if (applyForCostsList.get(0).getValue().getApplyForCostsApplicantType().equals("Home office")) {
            assertEquals("Home office", personalisation.get("recipient"));
            assertEquals(homeOfficeReferenceNumber, personalisation.get("recipientReferenceNumber"));
        } else {
            assertEquals("Your", personalisation.get("recipient"));
            assertEquals(legalRepRefNumber, personalisation.get("recipientReferenceNumber"));
        }
    }

    static Stream<Arguments> appliesForCostsProvider() {
        return Stream.of(
            Arguments.of(List.of(new IdValue<>(newestApplicationCreatedNumber, new ApplyForCosts(unreasonableCostsType, "Legal representative", homeOffice)))),
            Arguments.of(List.of(new IdValue<>(newestApplicationCreatedNumber, new ApplyForCosts("Wasted costs", homeOffice, "Legal representative"))))
        );
    }
}