package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
class ApplyForCostsRespondentPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String applyForCostsNotificationForRespondentTemplateId = "applyForCostsNotificationForRespondentTemplateId";
    private String homeOfficeEmailAddress = "homeOfficeEmailAddress@gmail.com";
    private String legalRepEmailAddress = "legalRepEmailAddress@gmail.com";
    private String iaExUiFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String legalRepRefNumber = "someLegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private static String homeOffice = "Home office";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private ApplyForCostsRespondentPersonalisation applyForCostsRespondentPersonalisation;

    @BeforeEach
    void setup() {
        Map<String, String> appelantInfo = new HashMap<>();
        appelantInfo.put("appellantGivenNames", appellantGivenNames);
        appelantInfo.put("appellantFamilyName", appellantFamilyName);

        applyForCostsRespondentPersonalisation = new ApplyForCostsRespondentPersonalisation(
            applyForCostsNotificationForRespondentTemplateId,
            homeOfficeEmailAddress,
            emailAddressFinder,
            customerServicesProvider,
            iaExUiFrontendUrl,
            personalisationProvider
        );

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(personalisationProvider.getAppellantCredentials(asylumCase)).thenReturn(appelantInfo);
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(applyForCostsNotificationForRespondentTemplateId, applyForCostsRespondentPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_APPLY_FOR_COSTS_SUBMITTED",
            applyForCostsRespondentPersonalisation.getReferenceId(caseId));
    }


    @ParameterizedTest
    @MethodSource("respondentEmails")
    void should_return_given_email_address(List<IdValue<ApplyForCosts>> applyForCostsList) {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        if (applyForCostsList.get(0).getValue().getRespondentToCostsOrder().equals("Home office")) {
            assertTrue(applyForCostsRespondentPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
        } else {
            assertTrue(applyForCostsRespondentPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
        }
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> applyForCostsRespondentPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @MethodSource("respondentEmails")
    void should_return_personalisation_when_all_information_given(List<IdValue<ApplyForCosts>> applyForCostsList) {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));
        Map<String, String> personalisation = applyForCostsRespondentPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals("1", personalisation.get("applicationId"));

        if (applyForCostsList.get(0).getValue().getRespondentToCostsOrder().equals(homeOffice)) {
            assertEquals(homeOfficeRefNumber, personalisation.get("respondentReferenceNumber"));
            assertEquals(homeOffice, personalisation.get("respondent"));
            assertEquals("Wasted", personalisation.get("appliedCostsType"));
        } else {
            assertEquals(legalRepRefNumber, personalisation.get("respondentReferenceNumber"));
            assertEquals("Your", personalisation.get("respondent"));
            assertEquals("Unreasonable", personalisation.get("appliedCostsType"));
        }

    }

    @Test
    void should_throw_when_applies_for_costs_are_not_present() {
        assertThatThrownBy(() -> applyForCostsRespondentPersonalisation.getPersonalisation(asylumCase))
            .hasMessage("Applies for costs are not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    static Stream<Arguments> respondentEmails() {
        return Stream.of(
            Arguments.of(List.of(new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative")))),
            Arguments.of(List.of(new IdValue<>("1", new ApplyForCosts("Wasted costs", homeOffice))))
        );
    }

}