package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantCmrHearingCancelledPersonalisationEmailTest {

    private final Long caseId = 12345L;
    private final String appellantCmrCancelledEmailTemplateId = "appellantCmrCancelledEmailTemplateId";
    private final String legallyReppedAppellantCmrCancelledEmailTemplateId = "legallyReppedAppellantCmrCancelledEmailTemplateId";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String oldHearingCentreAddress = "someHearingCentreAddress";
    private final String oldHearingDate = "1 January 2026";
    private final String oldHearingTime = "2:00pm";
    private final String someHearingDateTime = "2026-01-01T14:00";

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;

    private AppellantCmrHearingCancelledPersonalisationEmail appellantCmrHearingCancelledPersonalisationEmail;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)).thenReturn(someHearingDateTime);
        when(hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase)).thenReturn(oldHearingCentreAddress);
        when(dateTimeExtractor.extractHearingDate(someHearingDateTime)).thenReturn(oldHearingDate);
        when(dateTimeExtractor.extractHearingTime(someHearingDateTime)).thenReturn(oldHearingTime);

        appellantCmrHearingCancelledPersonalisationEmail = new AppellantCmrHearingCancelledPersonalisationEmail(
                appellantCmrCancelledEmailTemplateId,
                legallyReppedAppellantCmrCancelledEmailTemplateId,
                recipientsFinder,
                dateTimeExtractor,
                hearingDetailsFinder
        );
    }

    @Test
    void should_return_appellant_template_id_when_aip_journey() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        assertEquals(appellantCmrCancelledEmailTemplateId,
                appellantCmrHearingCancelledPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_legally_repped_template_id_when_not_aip_journey() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));

        assertEquals(legallyReppedAppellantCmrCancelledEmailTemplateId,
                appellantCmrHearingCancelledPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_CMR_HEARING_CANCELLED_APPELLANT_EMAIL",
                appellantCmrHearingCancelledPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_list_from_subscribers_in_asylum_case_when_aip_journey() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        String mockedAppellantEmail = "fake@faketest.com";
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
                .thenReturn(Collections.singleton(mockedAppellantEmail));

        assertTrue(appellantCmrHearingCancelledPersonalisationEmail.getRecipientsList(asylumCase)
                .contains(mockedAppellantEmail));
    }

    @Test
    void should_return_given_email_address_list_from_legal_rep_when_not_aip_journey() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));

        String mockedLegalRepEmail = "legalrep@faketest.com";
        when(recipientsFinder.findReppedAppellant(asylumCase, NotificationType.EMAIL))
                .thenReturn(Collections.singleton(mockedLegalRepEmail));

        assertTrue(appellantCmrHearingCancelledPersonalisationEmail.getRecipientsList(asylumCase)
                .contains(mockedLegalRepEmail));
    }

    @Test
    void should_return_empty_recipients_list_when_internal_case() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(AsylumCaseDefinition.IS_ADMIN, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        assertThat(appellantCmrHearingCancelledPersonalisationEmail.getRecipientsList(asylumCase))
                .isEmpty();
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> appellantCmrHearingCancelledPersonalisationEmail.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation = appellantCmrHearingCancelledPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
                .containsEntry("appealReferenceNumber", appealReferenceNumber)
                .containsEntry("appellantGivenNames", appellantGivenNames)
                .containsEntry("appellantFamilyName", appellantFamilyName)
                .containsEntry("oldHearingDate", oldHearingDate)
                .containsEntry("oldHearingTime", oldHearingTime)
                .containsEntry("oldHearingCentreAddress", oldHearingCentreAddress);
    }
}