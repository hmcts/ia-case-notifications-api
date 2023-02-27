package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class RespondentNonStandardDirectionPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AppealService appealService;
    @Mock
    DirectionFinder directionFinder;
    @Mock
    Direction direction;

    private final Long caseId = 12345L;
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String apcHomeOfficeEmailAddress = "homeoffice-apc@example.com";
    private final String lartHomeOfficeEmailAddress = "homeoffice-respondent@example.com";
    private final String homeOfficeHearingCentreEmail = "hc-taylorhouse@example.com";
    private final String homeOfficeEmail = "ho-taylorhouse@example.com";
    private final String homeOfficeFtpaEmailAddress = "ho-ftpa-taylorhouse@example.com";

    private String directionDueDate = "2019-08-27";
    private String expectedDirectionDueDate = "27 Aug 2019";
    private String directionExplanation = "someExplanation";
    private String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String homeOfficeRefNumber = "homeOfficeReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";

    private RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation;

    @BeforeEach
    public void setUp() {

        when((emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)))
                .thenReturn(homeOfficeHearingCentreEmail);

        when(emailAddressFinder.getListCaseFtpaHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeFtpaEmailAddress);

        when((emailAddressFinder.getHomeOfficeEmailAddress(asylumCase))).thenReturn(homeOfficeEmail);

        when((direction.getDateDue())).thenReturn(directionDueDate);
        when((direction.getExplanation())).thenReturn(directionExplanation);
        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.of(direction));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);


        respondentNonStandardDirectionPersonalisation = new RespondentNonStandardDirectionPersonalisation(
                beforeListingTemplateId,
                afterListingTemplateId,
                iaExUiFrontendUrl,
                apcHomeOfficeEmailAddress,
                lartHomeOfficeEmailAddress,
                directionFinder,
                customerServicesProvider,
                appealService,
                emailAddressFinder
        );
    }

    @Test
    void should_return_the_given_before_listing_template_id() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                .thenReturn(Optional.of(State.FINAL_BUNDLING));

        assertEquals(beforeListingTemplateId, respondentNonStandardDirectionPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_the_given_after_listing_template_id() {
        when(appealService.isAppealListed(asylumCase))
                .thenReturn(true);

        assertEquals(afterListingTemplateId,
                respondentNonStandardDirectionPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_throw_exception_if_current_visible_state_to_home_office_all_is_not_present() {

        assertThatThrownBy(() -> respondentNonStandardDirectionPersonalisation.getRecipientsList(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("currentCaseStateVisibleToHomeOfficeAll flag is not present");
    }

    @Test
    void should_return_correct_email_address_for_home_office() {

        List<State> apcEmail = newArrayList(
                State.APPEAL_SUBMITTED,
                State.PENDING_PAYMENT,
                State.AWAITING_RESPONDENT_EVIDENCE

        );


        List<State> lartEmail = newArrayList(
                State.CASE_BUILDING,
                State.CASE_UNDER_REVIEW,
                State.RESPONDENT_REVIEW
        );

        List<State> ftpaEmail = newArrayList(
                State.FTPA_SUBMITTED,
                State.FTPA_DECIDED
        );

        List<State> pouNoListedEmail = newArrayList(
                State.LISTING,
                State.SUBMIT_HEARING_REQUIREMENTS,
                State.ENDED,
                State.APPEAL_TAKEN_OFFLINE
        );

        List<State> poulistedEmail = newArrayList(
                State.PREPARE_FOR_HEARING,
                State.FINAL_BUNDLING,
                State.PRE_HEARING,
                State.DECISION,
                State.ADJOURNED,
                State.DECIDED,
                State.ENDED,
                State.APPEAL_TAKEN_OFFLINE
        );

        Map<String, List<State>> states = new HashMap<>();

        states.put(apcHomeOfficeEmailAddress, apcEmail);
        states.put(lartHomeOfficeEmailAddress, lartEmail);
        states.put(homeOfficeFtpaEmailAddress, ftpaEmail);
        states.put(homeOfficeEmail, pouNoListedEmail);
        states.put(homeOfficeHearingCentreEmail, poulistedEmail);


        Set<String> emailAddresses = states.keySet();

        for (String emailAddress : emailAddresses) {
            List<State> statesList = states.get(emailAddress);
            for (State state : statesList) {
                when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                        .thenReturn(Optional.of(state));

                if (emailAddress != null && emailAddress.equals(homeOfficeHearingCentreEmail)) {
                    // test the same state when the case is listed
                    when(appealService.isAppealListed(asylumCase)).thenReturn(true);
                    when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.of(Optional.empty()));
                    when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

                    assertTrue(respondentNonStandardDirectionPersonalisation.getRecipientsList(asylumCase)
                            .contains(homeOfficeHearingCentreEmail));
                } else if (emailAddress != null && emailAddress.equals(homeOfficeEmail)) {
                    //case not listed yet
                    when(appealService.isAppealListed(asylumCase)).thenReturn(false);
                    when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
                    when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

                    assertTrue(respondentNonStandardDirectionPersonalisation.getRecipientsList(asylumCase)
                            .contains(homeOfficeEmail));
                } else {
                    assertTrue(respondentNonStandardDirectionPersonalisation.getRecipientsList(asylumCase)
                            .contains(emailAddress));
                }
            }
        }
    }

    @Test
    void should_throw_exception_when_home_office_is_missing_in_the_case_data() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> respondentNonStandardDirectionPersonalisation.getRecipientsList(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("currentCaseStateVisibleToHomeOfficeAll flag is not present");
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_RESPONDENT_NON_STANDARD_DIRECTION",
                respondentNonStandardDirectionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> respondentNonStandardDirectionPersonalisation.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(respondentNonStandardDirectionPersonalisation);
        Map<String, String> personalisation =
                respondentNonStandardDirectionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(directionExplanation, personalisation.get("explanation"));
        assertEquals(expectedDirectionDueDate, personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(respondentNonStandardDirectionPersonalisation);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
                respondentNonStandardDirectionPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(directionExplanation, personalisation.get("explanation"));
        assertEquals(expectedDirectionDueDate, personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {
        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respondentNonStandardDirectionPersonalisation.getPersonalisation(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("non-standard direction is not present");
    }

    @Test
    public void should_return_false_if_appeal_not_yet_listed() {
        assertFalse(appealService.isAppealListed(asylumCase));
    }
}
