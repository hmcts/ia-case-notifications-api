package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantMakeAnApplicationPersonalisationEmailTest {

    private final String beforeListingEmailTemplateId = "beforeListingEmailTemplateId";
    private final String afterListingEmailTemplateId = "afterListingEmailtemplateId";
    private final String otherBeforeListingEmailTemplateId = "otherBeforeListingEmailTemplateId";
    private final String otherAfterListingEmailTemplateId = "otherAfterListingEmailtemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String mockedAppellantGivenNames = "someAppellantGivenNames";
    private final String mockedAppellantFamilyName = "someAppellantFamilyName";
    private final String homeOfficeUser = "caseworker-ia-homeofficelart";
    private final String citizenUser = "citizen";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    AppealService appealService;
    @Mock
    MakeAnApplicationService makeAnApplicationService;
    @Mock
    UserDetailsProvider userDetailsProvider;
    @Mock
    UserDetails userDetails;
    @Mock
    MakeAnApplication makeAnApplication;
    private AppellantMakeAnApplicationPersonalisationEmail appellantMakeAnApplicationPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        String ariaListingReference = "someReferenceNumber";
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, false)).thenReturn(Optional.of(makeAnApplication));
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        appellantMakeAnApplicationPersonalisationEmail = new AppellantMakeAnApplicationPersonalisationEmail(
            beforeListingEmailTemplateId,
            afterListingEmailTemplateId,
            otherBeforeListingEmailTemplateId,
            otherAfterListingEmailTemplateId,
            iaAipFrontendUrl,
            recipientsFinder, appealService, makeAnApplicationService, userDetailsProvider);
    }

    @Test
    public void should_return_given_template_id() {
        when(userDetails.getRoles()).thenReturn(List.of(citizenUser));

        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(beforeListingEmailTemplateId, appellantMakeAnApplicationPersonalisationEmail.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(afterListingEmailTemplateId, appellantMakeAnApplicationPersonalisationEmail.getTemplateId(asylumCase));

        when(userDetails.getRoles()).thenReturn(List.of(homeOfficeUser));

        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(otherBeforeListingEmailTemplateId, appellantMakeAnApplicationPersonalisationEmail.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(otherAfterListingEmailTemplateId, appellantMakeAnApplicationPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_MAKE_AN_APPLICATION_APPELLANT_AIP_EMAIL",
            appellantMakeAnApplicationPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {
        String mockedAppellantEmailAddress = "appelant@example.net";
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantMakeAnApplicationPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantMakeAnApplicationPersonalisationEmail.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {citizenUser, homeOfficeUser})
    public void should_return_personalisation_when_all_information_given(String user) {
        initializePrefixes(appellantMakeAnApplicationPersonalisationEmail);
        when(userDetails.getRoles()).thenReturn(List.of(user));
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, false))
            .thenReturn(Optional.of(makeAnApplication));
        String applicationType = "someApplicationType";
        when(makeAnApplication.getType()).thenReturn(applicationType);
        String applicationTypePhrase = "some application type";
        when(makeAnApplicationService.mapApplicationTypeToPhrase(makeAnApplication))
            .thenReturn(applicationTypePhrase);

        Map<String, String> personalisation =
            appellantMakeAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("HO Ref Number", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("Given names", mockedAppellantGivenNames)
            .containsEntry("Family name", mockedAppellantFamilyName)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);
        assertEquals(user.equals(citizenUser) ? applicationType : applicationTypePhrase,
            personalisation.get("applicationType"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, false);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(appellantMakeAnApplicationPersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> personalisation =
            appellantMakeAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("HO Ref Number", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("Given names", mockedAppellantGivenNames)
            .containsEntry("Family name", mockedAppellantFamilyName)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, false);
    }


    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_when_only_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(appellantMakeAnApplicationPersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        LocalDate.now().plusDays(28)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(makeAnApplication.getType()).thenReturn("");

        Map<String, String> personalisation =
            appellantMakeAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", "")
            .containsEntry("HO Ref Number", "")
            .containsEntry("Given names", "")
            .containsEntry("Family name", "")
            .containsEntry("applicationType", "")
            .containsEntry("ariaListingReference", "")
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, false);
    }
}
