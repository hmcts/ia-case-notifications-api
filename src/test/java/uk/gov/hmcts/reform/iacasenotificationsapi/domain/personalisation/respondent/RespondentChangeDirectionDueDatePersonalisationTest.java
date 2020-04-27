package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class RespondentChangeDirectionDueDatePersonalisationTest {

    @Mock Callback<AsylumCase> callback;
    @Mock AsylumCase asylumCase;
    @Mock EmailAddressFinder emailAddressFinder;
    @Mock PersonalisationProvider personalisationProvider;
    @Mock CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String beforeListingTemplateId = "beforeListingTemplateId";
    private String afterListingTemplateId = "afterListingTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String homeOfficeApcEmailAddress = "homeOfficeAPC@example.com";
    private String homeOfficeLartEmailAddress = "homeOfficeLART@example.com";
    private String homeOfficeBhamEmailAddress = "ho-birmingham@example.com";
    private String hmctsReference = "hmctsReference";
    private String ariaListingReference = "someAriaListingReference";
    private String homeOfficeReference = "homeOfficeReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation;

    @Before
    public void setUp() {

        respondentChangeDirectionDueDatePersonalisation = new RespondentChangeDirectionDueDatePersonalisation(
            beforeListingTemplateId,
            afterListingTemplateId,
            iaExUiFrontendUrl,
            personalisationProvider,
            homeOfficeApcEmailAddress,
            homeOfficeLartEmailAddress,
            emailAddressFinder,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_the_given_template_id() {
        assertEquals(beforeListingTemplateId, respondentChangeDirectionDueDatePersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        assertEquals(afterListingTemplateId, respondentChangeDirectionDueDatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_the_ho_apc_email_address_until_respondent_review() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.of(State.CASE_BUILDING));

        assertEquals(Collections.singleton(homeOfficeApcEmailAddress), respondentChangeDirectionDueDatePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_lart_email_address_at_respondent_review() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.of(State.RESPONDENT_REVIEW));

        assertEquals(Collections.singleton(homeOfficeLartEmailAddress), respondentChangeDirectionDueDatePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_hearing_centre_email_address_after_respondent_review() {
        when(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeBhamEmailAddress);
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.of(State.FINAL_BUNDLING));

        assertEquals(Collections.singleton(homeOfficeBhamEmailAddress), respondentChangeDirectionDueDatePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_exception_when_home_office_is_missing_in_the_case_data() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respondentChangeDirectionDueDatePersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("currentCaseStateVisibleToHomeOfficeAll flag is not present");
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_RESPONDENT_CHANGE_DIRECTION_DUE_DATE", respondentChangeDirectionDueDatePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisation());

        Map<String, String> personalisation = respondentChangeDirectionDueDatePersonalisation.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(() -> respondentChangeDirectionDueDatePersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    private Map<String, String> getPersonalisation() {
        return ImmutableMap
            .<String, String>builder()
            .put("hmctsReference", hmctsReference)
            .put("ariaListingReference", ariaListingReference)
            .put("homeOfficeReference", homeOfficeReference)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }

    @Test
    public void should_return_false_if_appeal_not_yet_listed() {
        assertFalse(respondentChangeDirectionDueDatePersonalisation.isAppealListed(asylumCase));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        assertTrue(respondentChangeDirectionDueDatePersonalisation.isAppealListed(asylumCase));
    }
}
