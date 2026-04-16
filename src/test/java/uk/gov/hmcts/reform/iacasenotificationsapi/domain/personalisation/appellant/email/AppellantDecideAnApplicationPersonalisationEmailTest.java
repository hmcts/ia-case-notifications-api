package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantDecideAnApplicationPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    MakeAnApplicationService makeAnApplicationService;
    @Mock
    AppealService appealService;
    @Mock
    MakeAnApplication makeAnApplication;
    @Mock
    UserDetailsProvider userDetailsProvider;
    @Mock
    UserDetails userDetails;

    private final String emailTemplateIdRefusedBeforeListing = "emailTemplateIdRefusedBeforeListing";
    private final String emailTemplateIdRefusedAfterListing = "emailTemplateIdRefusedAfterListing";
    private final String emailTemplateIdGrantedBeforeListing = "emailTemplateIdGrantedBeforeListing";
    private final String emailTemplateIdGrantedAfterListing = "emailTemplateIdGrantedAfterListing";
    private final String emailTemplateIdOtherPartyBeforeListing = "emailTemplateIdOtherPartyBeforeListing";
    private final String emailTemplateIdOtherPartyAfterListing = "emailTemplateIdOtherPartyAfterListing";

    private final String iaAipFrontendUrl = "http://localhost";
    private final String applicationType = "someApplicationType";
    private final String applicationTypePhrase = "some application type";
    private final String decisionMaker = "someDecisionMaker";
    private final String citizenUser = "citizen";
    private final String homeOfficeUser = "caseworker-ia-homeofficelart";

    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String mockedListingReferenceNumber = "someListingReferenceNumber";
    private final String mockedAppellantGivenNames = "someAppellantGivenNames";
    private final String mockedAppellantFamilyName = "someAppellantFamilyName";

    private AppellantDecideAnApplicationPersonalisationEmail appellantDecideAnApplicationPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class))
            .thenReturn(Optional.of(mockedListingReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.ofNullable(makeAnApplication));
        when(makeAnApplication.getType()).thenReturn(applicationType);
        when(makeAnApplicationService.mapApplicationTypeToPhrase(makeAnApplication))
            .thenReturn(applicationTypePhrase);
        when(makeAnApplication.getDecisionMaker()).thenReturn(decisionMaker);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        appellantDecideAnApplicationPersonalisationEmail = new AppellantDecideAnApplicationPersonalisationEmail(
            emailTemplateIdRefusedBeforeListing,
            emailTemplateIdRefusedAfterListing,
            emailTemplateIdGrantedBeforeListing,
            emailTemplateIdGrantedAfterListing,
            emailTemplateIdOtherPartyBeforeListing,
            emailTemplateIdOtherPartyAfterListing,
            iaAipFrontendUrl,
            recipientsFinder,
            makeAnApplicationService,
            userDetailsProvider);
    }

    @Test
    public void should_return_refused_before_listing_template_id() {
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");
        when(makeAnApplication.getApplicantRole()).thenReturn(citizenUser);

        assertEquals(emailTemplateIdRefusedBeforeListing,
                appellantDecideAnApplicationPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_refused_after_listing_template_id() {
        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        when(makeAnApplication.getState()).thenReturn("preHearing");
        when(makeAnApplication.getApplicantRole()).thenReturn(citizenUser);
        when(makeAnApplicationService.isApplicationListed(State.PRE_HEARING)).thenReturn(true);

        assertEquals(emailTemplateIdRefusedAfterListing,
                appellantDecideAnApplicationPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_granted_after_listing_template_id() {
        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getState()).thenReturn("preHearing");
        when(makeAnApplication.getApplicantRole()).thenReturn(citizenUser);
        when(makeAnApplicationService.isApplicationListed(State.PRE_HEARING)).thenReturn(true);

        assertEquals(emailTemplateIdGrantedAfterListing,
                appellantDecideAnApplicationPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_granted_before_listing_template_id() {
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");
        when(makeAnApplication.getApplicantRole()).thenReturn(citizenUser);

        assertEquals(emailTemplateIdGrantedBeforeListing,
                appellantDecideAnApplicationPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_other_party_before_listing_template_id() {
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");
        when(makeAnApplication.getApplicantRole()).thenReturn(homeOfficeUser);

        assertEquals(emailTemplateIdOtherPartyBeforeListing,
                appellantDecideAnApplicationPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_other_party_after_listing_template_id() {
        when(makeAnApplication.getState()).thenReturn("preHearing");
        when(makeAnApplication.getApplicantRole()).thenReturn(homeOfficeUser);
        when(makeAnApplicationService.isApplicationListed(State.PRE_HEARING)).thenReturn(true);

        assertEquals(emailTemplateIdOtherPartyAfterListing,
                appellantDecideAnApplicationPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_DECIDE_AN_APPLICATION_APPELLANT_AIP_EMAIL",
            appellantDecideAnApplicationPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {
        String mockedAppellantEmailAddress = "appelant@example.net";
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantDecideAnApplicationPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
assertThrows(NullPointerException.class, () -> appellantDecideAnApplicationPersonalisationEmail.getRecipientsList(null))
            ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_only_mandatory_information_given(YesOrNo isAda) {

        when(userDetails.getRoles()).thenReturn(List.of(citizenUser));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(appellantDecideAnApplicationPersonalisationEmail);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(makeAnApplication.getType()).thenReturn("");
        when(makeAnApplication.getDecisionMaker()).thenReturn("");
        when(makeAnApplication.getDecision()).thenReturn("Refused");

        Map<String, String> personalisation =
            appellantDecideAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", "")
            .containsEntry("ariaListingReference", "")
            .containsEntry("HO Ref Number", "")
            .containsEntry("Given names", "")
            .containsEntry("Family name", "")
            .containsEntry("applicationType", "")
            .containsEntry("decision maker role", "")
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, true);
    }

    @ParameterizedTest
    @ValueSource(strings = { citizenUser, homeOfficeUser })
    public void should_return_personalisation_when_all_information_given_and_decision_granted(String user) {

        initializePrefixes(appellantDecideAnApplicationPersonalisationEmail);
        when(userDetails.getRoles()).thenReturn(List.of(user));

        String decision = "Granted";
        when(makeAnApplication.getDecision()).thenReturn(decision);

        Map<String, String> personalisation =
                appellantDecideAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("ariaListingReference", mockedListingReferenceNumber)
            .containsEntry("HO Ref Number", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("Given names", mockedAppellantGivenNames)
            .containsEntry("Family name", mockedAppellantFamilyName)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("decision", decision);
        assertEquals(user.equals(citizenUser) ? applicationType : applicationTypePhrase,
            personalisation.get("applicationType"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, true);
    }

    @ParameterizedTest
    @ValueSource(strings = { citizenUser, homeOfficeUser })
    public void should_return_personalisation_when_all_information_given_and_decision_refused(String user) {

        initializePrefixes(appellantDecideAnApplicationPersonalisationEmail);
        when(userDetails.getRoles()).thenReturn(List.of(user));

        String decision = "Refused";
        when(makeAnApplication.getDecision()).thenReturn(decision);

        Map<String, String> personalisation =
                appellantDecideAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("ariaListingReference", mockedListingReferenceNumber)
            .containsEntry("HO Ref Number", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("Given names", mockedAppellantGivenNames)
            .containsEntry("Family name", mockedAppellantFamilyName)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("decision", decision)
            .containsEntry("decision maker role", decisionMaker);
        assertEquals(user.equals(citizenUser) ? applicationType : applicationTypePhrase,
            personalisation.get("applicationType"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, true);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given_and_decision_granted_ada(YesOrNo isAda) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(appellantDecideAnApplicationPersonalisationEmail);

        String decision = "Granted";
        when(makeAnApplication.getDecision()).thenReturn(decision);

        Map<String, String> personalisation =
            appellantDecideAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("ariaListingReference", mockedListingReferenceNumber)
            .containsEntry("HO Ref Number", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("Given names", mockedAppellantGivenNames)
            .containsEntry("Family name", mockedAppellantFamilyName)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("decision", decision);
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, true);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given_and_decision_refused_ada(YesOrNo isAda) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(appellantDecideAnApplicationPersonalisationEmail);

        String decision = "Refused";
        when(makeAnApplication.getDecision()).thenReturn(decision);

        Map<String, String> personalisation =
            appellantDecideAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("ariaListingReference", mockedListingReferenceNumber)
            .containsEntry("HO Ref Number", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("Given names", mockedAppellantGivenNames)
            .containsEntry("Family name", mockedAppellantFamilyName)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("decision", decision)
            .containsEntry("decision maker role", decisionMaker);
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, true);
    }
}
