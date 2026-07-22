package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeCmrHearingCancelledPersonalisationTest {

    private final Long caseId = 12345L;
    private final String legalRepCmrHearingCancelledTemplateId = "legalRepCmrHearingCancelledTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost/exui";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String legalRepReferenceNumber = "someLegalRepReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String oldHearingCentreAddress = "someHearingCentreAddress";
    private final String oldHearingDate = "1 January 2026";
    private final String oldHearingTime = "2:00pm";
    private final String someHearingDateTime = "2026-01-01T14:00";
    private final String legalRepEmailAddress = "legalrep@faketest.com";

    @Mock
    AsylumCase asylumCase;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;
    @Mock
    EmailAddressFinder emailAddressFinder;

    private LegalRepresentativeCmrHearingCancelledPersonalisation legalRepresentativeCmrHearingCancelledPersonalisation;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)).thenReturn(someHearingDateTime);
        when(hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase)).thenReturn(oldHearingCentreAddress);
        when(dateTimeExtractor.extractHearingDate(someHearingDateTime)).thenReturn(oldHearingDate);
        when(dateTimeExtractor.extractHearingTime(someHearingDateTime)).thenReturn(oldHearingTime);

        legalRepresentativeCmrHearingCancelledPersonalisation = new LegalRepresentativeCmrHearingCancelledPersonalisation(
                legalRepCmrHearingCancelledTemplateId,
                iaExUiFrontendUrl,
                dateTimeExtractor,
                hearingDetailsFinder,
                emailAddressFinder
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(legalRepCmrHearingCancelledTemplateId,
                legalRepresentativeCmrHearingCancelledPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_CMR_HEARING_CANCELLED_LEGAL_REPRESENTATIVE",
                legalRepresentativeCmrHearingCancelledPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_legal_rep_email_address_as_recipients_list() {
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);

        Set<String> recipients = legalRepresentativeCmrHearingCancelledPersonalisation.getRecipientsList(asylumCase);

        assertThat(recipients).hasSize(1);
        assertTrue(recipients.contains(legalRepEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> legalRepresentativeCmrHearingCancelledPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation = legalRepresentativeCmrHearingCancelledPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
                .containsEntry("appealReferenceNumber", appealReferenceNumber)
                .containsEntry("legalRepReferenceNumber", legalRepReferenceNumber)
                .containsEntry("appellantGivenNames", appellantGivenNames)
                .containsEntry("appellantFamilyName", appellantFamilyName)
                .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
                .containsEntry("oldHearingDate", oldHearingDate)
                .containsEntry("oldHearingTime", oldHearingTime)
                .containsEntry("oldHearingCentreAddress", oldHearingCentreAddress);
    }
}