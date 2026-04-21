package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantEditListingPersonalisationEmailTest {

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";
    private final String templateId = "someTemplateId";
    private final String listAssistHearingTemplateId = "listAssistHearingTemplateId";
    private final String lrAppellantTemplateId = "lrAppellantTemplateId";
    private final String lrAppellantListAssistHearingTemplateId = "lrAppellantListAssistHearingTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String mockedAppellantEmailAddress = "legalRep@example.com";
    private final String hearingCentreNameBefore = HearingCentre.MANCHESTER.toString();
    private final String hearingCentreName = HearingCentre.TAYLOR_HOUSE.toString();
    private final String iaAipFrontendUrl = "http://localhost";
    private final HearingCentre tribunalCentre = HearingCentre.HATTON_CROSS;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;
    private AppellantEditListingPersonalisationEmail appellantEditListingPersonalisationEmail;

    @BeforeEach
    public void setup() {
        appellantEditListingPersonalisationEmail = new AppellantEditListingPersonalisationEmail(
            templateId,
            listAssistHearingTemplateId,
            lrAppellantTemplateId,
            lrAppellantListAssistHearingTemplateId,
            iaAipFrontendUrl,
            personalisationProvider,
            customerServicesProvider,
            recipientsFinder,
            hearingDetailsFinder
        );
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(tribunalCentre));
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn(tribunalCentre.getValue());
    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertEquals(templateId, appellantEditListingPersonalisationEmail.getTemplateId(asylumCase));

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertEquals(lrAppellantTemplateId, appellantEditListingPersonalisationEmail.getTemplateId(asylumCase));

        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertEquals(listAssistHearingTemplateId, appellantEditListingPersonalisationEmail.getTemplateId(asylumCase));

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertEquals(lrAppellantListAssistHearingTemplateId, appellantEditListingPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_CASE_RE_LISTED_APPELLANT_EMAIL",
            appellantEditListingPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_aip() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));
        Set<String> response = appellantEditListingPersonalisationEmail.getRecipientsList(asylumCase);
        verify(recipientsFinder, times(1)).findAll(asylumCase, NotificationType.EMAIL);
        verify(recipientsFinder, times(0)).findReppedAppellant(asylumCase, NotificationType.EMAIL);
        assertTrue(response.contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_legally_repped_appellant() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(recipientsFinder.findReppedAppellant(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));
        Set<String> response = appellantEditListingPersonalisationEmail.getRecipientsList(asylumCase);
        verify(recipientsFinder, times(0)).findAll(asylumCase, NotificationType.EMAIL);
        verify(recipientsFinder, times(1)).findReppedAppellant(asylumCase, NotificationType.EMAIL);
        assertTrue(response.contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantEditListingPersonalisationEmail.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_hearing_centre_is_null() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class,
                () -> appellantEditListingPersonalisationEmail.getPersonalisation(callback));
        assertEquals("No hearing centre present", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {
        initializePrefixes(appellantEditListingPersonalisationEmail);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation =
            appellantEditListingPersonalisationEmail.getPersonalisation(callback);
        assertFalse(personalisation.isEmpty());
        assertThat(personalisation)
            .containsAllEntriesOf(customerServicesProvider.getCustomerServicesPersonalisation())
            .containsAllEntriesOf(getPersonalisationMapWithGivenValues())
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? "Accelerated detained appeal" : "Immigration and Asylum appeal")
            .containsEntry("tribunalCentre", tribunalCentre.getValue())
            .containsEntry("hyperlink to service", iaAipFrontendUrl);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_when_optional_fields_are_blank(YesOrNo isAda) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(appellantEditListingPersonalisationEmail);
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithBlankValues());

        Map<String, String> personalisation =
            appellantEditListingPersonalisationEmail.getPersonalisation(callback);

        assertFalse(personalisation.isEmpty());
        assertThat(personalisation)
            .containsAllEntriesOf(customerServicesProvider.getCustomerServicesPersonalisation())
            .containsAllEntriesOf(getPersonalisationMapWithBlankValues())
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? "Accelerated detained appeal" : "Immigration and Asylum appeal")
            .containsEntry("tribunalCentre", tribunalCentre.getValue())
            .containsEntry("hyperlink to service", iaAipFrontendUrl);
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        String customerServicesEmail = "cust.services@example.com";
        String customerServicesTelephone = "555 555 555";
        String requirementsOther = "someRequirementsOther";
        String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
        String requirementsInCamera = "someRequirementsInCamera";
        String requirementsMultimedia = "someRequirementsMultimedia";
        String requirementsVulnerabilities = "someRequirementsVulnerabilities";
        String remoteVideoCallTribunalResponse = "some tribunal response";
        String homeOfficeRefNumber = "homeOfficeRefNumber";
        String appellantFamilyName = "appellantFamilyName";
        String appellantGivenNames = "appellantGivenNames";
        String ariaListingReference = "someAriaListingReference";
        String appealReferenceNumber = "someReferenceNumber";
        String hearingCentreAddress = "some hearing centre address";
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("hearingCentreName", hearingCentreName)
            .put("remoteVideoCallTribunalResponse", remoteVideoCallTribunalResponse)
            .put("hearingRequirementVulnerabilities", requirementsVulnerabilities)
            .put("hearingRequirementMultimedia", requirementsMultimedia)
            .put("hearingRequirementSingleSexCourt", requirementsSingleSexCourt)
            .put("hearingRequirementInCameraCourt", requirementsInCamera)
            .put("hearingRequirementOther", requirementsOther)
            .put("oldHearingCentre", hearingCentreNameBefore)
            .put(HEARING_CENTRE_ADDRESS, hearingCentreAddress)
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }

    private Map<String, String> getPersonalisationMapWithBlankValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "")
            .put("ariaListingReference", "")
            .put("homeOfficeReferenceNumber", "")
            .put("appellantGivenNames", "")
            .put("appellantFamilyName", "")
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("hearingCentreName", "")
            .put("remoteVideoCallTribunalResponse", "")
            .put("hearingRequirementVulnerabilities", "")
            .put("hearingRequirementMultimedia", "")
            .put("hearingRequirementSingleSexCourt", "")
            .put("hearingRequirementInCameraCourt", "")
            .put("hearingRequirementOther", "")
            .put("oldHearingCentre", "")
            .put(HEARING_CENTRE_ADDRESS, "")
            .put("customerServicesTelephone", "")
            .put("customerServicesEmail", "")
            .build();
    }
}

