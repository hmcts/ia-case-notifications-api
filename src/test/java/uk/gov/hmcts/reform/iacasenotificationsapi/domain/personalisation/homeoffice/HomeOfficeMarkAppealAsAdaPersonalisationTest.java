package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeMarkAppealAsAdaPersonalisationTest {

    private final String homeOfficeTemplateId = "homeOfficeTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private final String homeOfficeEmailAddress = "homeoffice@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
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
    private HomeOfficeMarkAppealAsAdaPersonalisation homeOfficeMarkAppealAsAdaPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        homeOfficeMarkAppealAsAdaPersonalisation = new HomeOfficeMarkAppealAsAdaPersonalisation(
            homeOfficeTemplateId,
            homeOfficeEmailAddress,
            iaExUiFrontendUrl,
            customerServicesProvider,
            emailAddressFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(homeOfficeTemplateId, homeOfficeMarkAppealAsAdaPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_MARK_APPEAL_AS_ADA_HOME_OFFICE",
            homeOfficeMarkAppealAsAdaPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address() {
        assertTrue(homeOfficeMarkAppealAsAdaPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> homeOfficeMarkAppealAsAdaPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            homeOfficeMarkAppealAsAdaPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            homeOfficeMarkAppealAsAdaPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
