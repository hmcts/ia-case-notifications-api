package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeMarkAppealAsDetainedPersonalisationTest {

    private final String markAppealAsDetainedTemplateId = "removeDetentionStatusTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private final String hearingEmailAddress = "hearinge@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";
    private final String homeOfficeApcEmailAddress = "homeOfficeAPC@example.com";
    private final String homeOfficeLartEmailAddress = "homeOfficeLART@example.com";
    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    private HomeOfficeMarkAppealAsDetainedPersonalisation homeOfficeMarkAppealAsDetainedPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        homeOfficeMarkAppealAsDetainedPersonalisation = new HomeOfficeMarkAppealAsDetainedPersonalisation(
            markAppealAsDetainedTemplateId,
            homeOfficeApcEmailAddress,
            homeOfficeLartEmailAddress,
            iaExUiFrontendUrl,
            emailAddressFinder,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(markAppealAsDetainedTemplateId, homeOfficeMarkAppealAsDetainedPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_MARK_APPEAL_AS_DETAINED_HOME_OFFICE",
            homeOfficeMarkAppealAsDetainedPersonalisation.getReferenceId(caseId));
    }


    @Test
    public void should_return_the_ho_apc_email_address_until_awaiting_reasons_for_appeal() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.AWAITING_REASONS_FOR_APPEAL));

        assertEquals(Collections.singleton(homeOfficeApcEmailAddress), homeOfficeMarkAppealAsDetainedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_apc_email_address_until_awaiting_clarifying_questions_answers() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS));

        assertEquals(Collections.singleton(homeOfficeApcEmailAddress), homeOfficeMarkAppealAsDetainedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_apc_email_address_until_reasons_for_appeal_submitted() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.REASONS_FOR_APPEAL_SUBMITTED));

        assertEquals(Collections.singleton(homeOfficeApcEmailAddress), homeOfficeMarkAppealAsDetainedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_apc_email_address_until_clarifying_questions_answers_submitted() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED));

        assertEquals(Collections.singleton(homeOfficeApcEmailAddress), homeOfficeMarkAppealAsDetainedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_apc_email_address_until_case_under_review() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.CASE_UNDER_REVIEW));

        assertEquals(Collections.singleton(homeOfficeApcEmailAddress), homeOfficeMarkAppealAsDetainedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_lart_email_address_until_listing() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.LISTING));

        assertEquals(Collections.singleton(homeOfficeLartEmailAddress), homeOfficeMarkAppealAsDetainedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_hearing_centre_email_address_after_listing() {
        String homeOfficeBhamEmailAddress = "ho-birmingham@example.com";
        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeBhamEmailAddress);
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.PRE_HEARING));

        assertEquals(Collections.singleton(homeOfficeBhamEmailAddress), homeOfficeMarkAppealAsDetainedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> homeOfficeMarkAppealAsDetainedPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            homeOfficeMarkAppealAsDetainedPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("ariaListingReferenceIfPresent", "Listing reference: " + ariaListingReference)
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
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            homeOfficeMarkAppealAsDetainedPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("ariaListingReferenceIfPresent", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
