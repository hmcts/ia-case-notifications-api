package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeDecisionWithoutHearingPersonalisationTesting {

    private final String homeOfficeDecisionWithoutHearingTemplateId = "homeOfficeDecisionWithoutHearingTemplateId";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAriaListingReference = "someAriaListingReference";
    private final String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String mockedAppellantGivenNames = "someAppellantGivenNames";
    private final String mockedAppellantFamilyName = "someAppellantFamilyName";
    private final String iaServicesPhone = "0300 123 1711";
    private final String iaServicesEmail = "contactia@justice.gov.uk";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final Map<String, String> customerServices = Map.of("customerServicesTelephone", iaServicesPhone,
        "customerServicesEmail", iaServicesEmail);
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    EmailAddressFinder emailAddressFinder;
    private HomeOfficeDecisionWithoutHearingPersonalisation homeOfficeDecisionWithoutHearingPersonalisation;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(mockedAriaListingReference));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(NO));

        homeOfficeDecisionWithoutHearingPersonalisation = new HomeOfficeDecisionWithoutHearingPersonalisation(
            homeOfficeDecisionWithoutHearingTemplateId,
            emailAddressFinder,
            customerServicesProvider,
            iaExUiFrontendUrl
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(homeOfficeDecisionWithoutHearingTemplateId,
            homeOfficeDecisionWithoutHearingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_DECISION_WITHOUT_HEARING_HOME_OFFICE",
            homeOfficeDecisionWithoutHearingPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_home_office_email() {
        String mockedHomeOfficeEmail = "ho-taylorhouse@example.com";
        when(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase)).thenReturn(mockedHomeOfficeEmail);

        assertTrue(homeOfficeDecisionWithoutHearingPersonalisation.getRecipientsList(asylumCase)
            .contains(mockedHomeOfficeEmail));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> homeOfficeDecisionWithoutHearingPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        initializePrefixes(homeOfficeDecisionWithoutHearingPersonalisation);
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(customerServices);


        Map<String, String> personalisation =
            homeOfficeDecisionWithoutHearingPersonalisation.getPersonalisation(asylumCase);

        String subjectPrefix = "Immigration and Asylum appeal";
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", mockedAppealReferenceNumber)
            .containsEntry("ariaListingReference", mockedAriaListingReference)
            .containsEntry("homeOfficeReferenceNumber", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", mockedAppellantGivenNames)
            .containsEntry("appellantFamilyName", mockedAppellantFamilyName)
            .containsEntry("customerServicesTelephone", iaServicesPhone)
            .containsEntry("customerServicesEmail", iaServicesEmail)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("subjectPrefix", subjectPrefix);
    }
}
