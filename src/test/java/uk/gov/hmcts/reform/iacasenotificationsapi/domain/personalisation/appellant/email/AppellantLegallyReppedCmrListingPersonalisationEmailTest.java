package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantLegallyReppedCmrListingPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;
    @Mock
    PersonalisationProvider personalisationProvider;

    private final Long caseId = 12345L;
    private final String inPersonTemplateId = "inPersonTemplateId";
    private final String remoteTemplateId = "remoteTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String adaPrefix = "ADA - URGENT";
    private final String nonAdaPrefix = "Immigration Appeal";

    private final String appealReferenceNumber = "PA/00001/2024";
    private final String ariaListingReference = "AA/12345";
    private final String legalRepReferenceNumber = "LegalRef001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String hearingDate = "2024-06-01";
    private final String hearingTime = "10:00";
    private final String hearingCentreAddress = "Taylor House, 88 Rosebery Avenue, London";
    private final String hearingDateTime = "2024-06-01T10:00:00";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";
    private final String mockedAppellantEmail = "appellant@example.com";

    private AppellantLegallyReppedCmrListingPersonalisationEmail appellantLegallyReppedCmrListingPersonalisationEmail;

    @BeforeEach
    public void setUp() {
        appellantLegallyReppedCmrListingPersonalisationEmail = new AppellantLegallyReppedCmrListingPersonalisationEmail(
                inPersonTemplateId,
                remoteTemplateId,
                iaExUiFrontendUrl,
                personalisationProvider,
                customerServicesProvider,
                recipientsFinder,
                dateTimeExtractor,
                hearingDetailsFinder
        );

        org.springframework.test.util.ReflectionTestUtils.setField(appellantLegallyReppedCmrListingPersonalisationEmail, "adaPrefix", adaPrefix);
        org.springframework.test.util.ReflectionTestUtils.setField(appellantLegallyReppedCmrListingPersonalisationEmail, "nonAdaPrefix", nonAdaPrefix);
    }

    @Test
    public void should_return_in_person_template_id_when_not_remote_hearing() {
        when(asylumCase.read(CMR_IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(inPersonTemplateId, appellantLegallyReppedCmrListingPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_remote_template_id_when_remote_hearing() {
        when(asylumCase.read(CMR_IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(remoteTemplateId, appellantLegallyReppedCmrListingPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_in_person_template_id_when_remote_hearing_field_absent() {
        when(asylumCase.read(CMR_IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.empty());
        assertEquals(inPersonTemplateId, appellantLegallyReppedCmrListingPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_correct_reference_id() {
        assertEquals(caseId + "_CMR_LISTED_OR_REMOTE_APPELLANT_EMAIL",
                appellantLegallyReppedCmrListingPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_recipients_for_aip_journey() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL)).thenReturn(Set.of(mockedAppellantEmail));

        Set<String> recipients = appellantLegallyReppedCmrListingPersonalisationEmail.getRecipientsList(asylumCase);

        assertFalse(recipients.isEmpty());
        assertTrue(recipients.contains(mockedAppellantEmail));
    }

    @Test
    public void should_return_recipients_for_repped_journey() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(recipientsFinder.findReppedAppellant(asylumCase, NotificationType.EMAIL)).thenReturn(Set.of(mockedAppellantEmail));

        Set<String> recipients = appellantLegallyReppedCmrListingPersonalisationEmail.getRecipientsList(asylumCase);

        assertFalse(recipients.isEmpty());
        assertTrue(recipients.contains(mockedAppellantEmail));
    }

    @Test
    public void should_throw_exception_when_asylum_case_is_null_for_recipients() {
        assertThrows(NullPointerException.class,
                () -> appellantLegallyReppedCmrListingPersonalisationEmail.getRecipientsList(null));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);
        when(hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(Map.of(
                "customerServicesTelephone", customerServicesTelephone,
                "customerServicesEmail", customerServicesEmail
        ));

        Map<String, String> personalisation =
                appellantLegallyReppedCmrListingPersonalisationEmail.getPersonalisation(asylumCase);

        assertFalse(personalisation.isEmpty());
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(nonAdaPrefix, personalisation.get("subjectPrefix"));
        assertEquals(customerServicesTelephone, personalisation.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, personalisation.get("customerServicesEmail"));
    }

    @Test
    public void should_return_ada_prefix_when_accelerated_detained_appeal() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);
        when(hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(Map.of());

        Map<String, String> personalisation =
                appellantLegallyReppedCmrListingPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(adaPrefix, personalisation.get("subjectPrefix"));
    }

    @Test
    public void should_return_empty_strings_when_optional_fields_absent() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.empty());
        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);
        when(hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(Map.of());

        Map<String, String> personalisation =
                appellantLegallyReppedCmrListingPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
    }

    @Test
    public void should_throw_exception_when_asylum_case_is_null_for_personalisation() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> appellantLegallyReppedCmrListingPersonalisationEmail.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}