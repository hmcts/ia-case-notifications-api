package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantListCasePersonalisationEmailTest {

    private final String templateId = "someTemplateId";
    private final String stf24wTemplateId = "some24WeeksTemplateId";
    private final String listAssistHearingTemplateId = "listAssistHearingTemplateId";
    private final String legallyReppedTemplateId = "legallyReppedTemplateId";
    private final String listAssistHearingLegallyReppedTemplateId = "listAssistHearingLegallyReppedTemplateId";
    private final String iaAipFrontendUrl = "http://somefrontendurl";
    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private final String hearingCentreAddress = "some hearing centre address";
    private final String hearingDate = "2019-08-27";
    private final String hearingTime = "14:25";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAppellantGivenNames = "someAppellantGivenNames";
    private final String mockedAppellantFamilyName = "someAppellantFamilyName";
    private final String mockedAppellantEmailAddress = "appelant@example.net";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";
    private final String ariaListingReference = "someAriaListingReference";
    @Mock
    AsylumCase asylumCase;
    @Mock
    StringProvider stringProvider;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;
    @Mock
    RecipientsFinder recipientsFinder;
    private AppellantListCasePersonalisationEmail appellantListCasePersonalisationEmail;

    @BeforeEach
    void setup() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        String hearingDateTime = "2019-08-27T14:25:15.000";
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn(hearingCentre.toString());
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(hearingCentreAddress);
        when(hearingDetailsFinder.getHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);
        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString()))
            .thenReturn(Optional.of(hearingCentreAddress));
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);

        appellantListCasePersonalisationEmail = new AppellantListCasePersonalisationEmail(
            templateId,
            stf24wTemplateId,
            listAssistHearingTemplateId,
            legallyReppedTemplateId,
            listAssistHearingLegallyReppedTemplateId,
            iaAipFrontendUrl,
            dateTimeExtractor,
            customerServicesProvider,
            hearingDetailsFinder,
            recipientsFinder
        );
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_CASE_LISTED_AIP_APPELLANT_EMAIL",
            appellantListCasePersonalisationEmail.getReferenceId(caseId));
    }

    @ParameterizedTest
    @CsvSource({
        "YES, YES, AIP, some24WeeksTemplateId",
        "YES, NO, AIP, listAssistHearingTemplateId",
        "NO, YES, AIP, some24WeeksTemplateId",
        "NO, NO, AIP, someTemplateId",
        "YES, YES, REP, listAssistHearingLegallyReppedTemplateId",
        "YES, NO, REP, listAssistHearingLegallyReppedTemplateId",
        "NO, YES, REP, legallyReppedTemplateId",
        "NO, NO, REP, legallyReppedTemplateId",
    })
    public void should_return_correct_template_id(YesOrNo isIntegrated, YesOrNo isStf24w, JourneyType journeyType, String expectedTemplateId) {
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(isIntegrated));
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(isStf24w));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));
        assertEquals(expectedTemplateId, appellantListCasePersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantListCasePersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
        verify(recipientsFinder, times(1)).findAll(asylumCase, NotificationType.EMAIL);
        verify(recipientsFinder, times(0)).findAll(asylumCase, NotificationType.SMS);
        verify(recipientsFinder, times(0)).findReppedAppellant(asylumCase, NotificationType.EMAIL);
        verify(recipientsFinder, times(0)).findReppedAppellant(asylumCase, NotificationType.SMS);
    }

    @Test
    public void should_return_given_email_address_list_from_email_in_asylum_case_if_repped() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(recipientsFinder.findReppedAppellant(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));
        assertTrue(appellantListCasePersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
        verify(recipientsFinder, times(0)).findAll(asylumCase, NotificationType.EMAIL);
        verify(recipientsFinder, times(0)).findAll(asylumCase, NotificationType.SMS);
        verify(recipientsFinder, times(1)).findReppedAppellant(asylumCase, NotificationType.EMAIL);
        verify(recipientsFinder, times(0)).findReppedAppellant(asylumCase, NotificationType.SMS);
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantListCasePersonalisationEmail.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }


    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(appellantListCasePersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> personalisation = appellantListCasePersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", mockedAppealReferenceNumber)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("appellantGivenNames", mockedAppellantGivenNames)
            .containsEntry("appellantFamilyName", mockedAppellantFamilyName)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("hearingDate", hearingDate)
            .containsEntry("hearingTime", hearingTime)
            .containsEntry("hearingCentreAddress", hearingCentreAddress)
            .containsEntry("tribunalCentre", hearingCentre.getValue());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_personalisation_when_co_records_hearing_response(YesOrNo isAda) {

        initializePrefixes(appellantListCasePersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));
        Map<String, String> personalisation = appellantListCasePersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", mockedAppealReferenceNumber)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("appellantGivenNames", mockedAppellantGivenNames)
            .containsEntry("appellantFamilyName", mockedAppellantFamilyName)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("hearingDate", hearingDate)
            .containsEntry("hearingTime", hearingTime)
            .containsEntry("hearingCentreAddress", hearingCentreAddress)
            .containsEntry("tribunalCentre", hearingCentre.getValue());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(appellantListCasePersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = appellantListCasePersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("ariaListingReference", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("hearingDate", hearingDate)
            .containsEntry("hearingTime", hearingTime)
            .containsEntry("hearingCentreAddress", hearingCentreAddress)
            .containsEntry("tribunalCentre", hearingCentre.getValue());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @Test
    void should_throw_personalisation_when_no_hearing_centre() {
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class,
                () -> appellantListCasePersonalisationEmail.getPersonalisation(asylumCase));
        assertEquals("No hearing centre present", exception.getMessage());
    }
}
