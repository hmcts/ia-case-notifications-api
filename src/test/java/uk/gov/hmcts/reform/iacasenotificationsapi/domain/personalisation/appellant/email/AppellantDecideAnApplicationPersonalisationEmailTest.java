package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

    private Long caseId = 12345L;
    private String emailTemplateIdRefusedBeforeListing = "emailTemplateIdRefusedBeforeListing";
    private String emailTemplateIdRefusedAfterListing = "emailTemplateIdRefusedAfterListing";
    private String emailTemplateIdGrantedBeforeListing = "emailTemplateIdGrantedBeforeListing";
    private String emailTemplateIdGrantedAfterListing = "emailTemplateIdGrantedAfterListing";
    private String emailTemplateIdOtherPartyBeforeListing = "emailTemplateIdOtherPartyBeforeListing";
    private String emailTemplateIdOtherPartyAfterListing = "emailTemplateIdOtherPartyAfterListing";

    private String iaAipFrontendUrl = "http://localhost";
    private String applicationType = "someApplicationType";
    private String applicationTypePhrase = "some application type";
    private String decisionMaker = "someDecisionMaker";
    private final String citizenUser = "citizen";
    private final String homeOfficeUser = "caseworker-ia-homeofficelart";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String mockedListingReferenceNumber = "someListingReferenceNumber";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String mockedAppellantEmailAddress = "appelant@example.net";

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
        assertEquals(caseId + "_DECIDE_AN_APPLICATION_APPELLANT_AIP_EMAIL",
            appellantDecideAnApplicationPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantDecideAnApplicationPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantDecideAnApplicationPersonalisationEmail.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
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

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("HO Ref Number"));
        assertEquals("", personalisation.get("Given names"));
        assertEquals("", personalisation.get("Family name"));
        assertEquals("", personalisation.get("applicationType"));
        assertEquals("", personalisation.get("decision maker role"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, true);
    }

    @ParameterizedTest
    @ValueSource(strings = { citizenUser, homeOfficeUser })
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given_and_decision_granted(YesOrNo isAda, String user) {
        when(userDetails.getRoles()).thenReturn(List.of(user));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(appellantDecideAnApplicationPersonalisationEmail);

        String decision = "Granted";
        when(makeAnApplication.getDecision()).thenReturn(decision);

        Map<String, String> personalisation =
                appellantDecideAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(mockedListingReferenceNumber, personalisation.get("ariaListingReference"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("HO Ref Number"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("Given names"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("Family name"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(decision, personalisation.get("decision"));
        assertEquals(user.equals(citizenUser) ? applicationType : applicationTypePhrase,
            personalisation.get("applicationType"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, true);
    }

    @ParameterizedTest
    @ValueSource(strings = { citizenUser, homeOfficeUser })
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given_and_decision_refused(YesOrNo isAda, String user) {
        when(userDetails.getRoles()).thenReturn(List.of(user));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(appellantDecideAnApplicationPersonalisationEmail);

        String decision = "Refused";
        when(makeAnApplication.getDecision()).thenReturn(decision);

        Map<String, String> personalisation =
                appellantDecideAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(mockedListingReferenceNumber, personalisation.get("ariaListingReference"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("HO Ref Number"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("Given names"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("Family name"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(decision, personalisation.get("decision"));
        assertEquals(decisionMaker, personalisation.get("decision maker role"));
        assertEquals(user.equals(citizenUser) ? applicationType : applicationTypePhrase,
            personalisation.get("applicationType"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, true);
    }
}
