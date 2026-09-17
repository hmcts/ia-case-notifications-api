package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipCmrRelistedAppellantSmsPersonalisationTest {

    private final String templateId = "appellantCaseEditedSmsTemplateId";
    private final String iaAipFrontendUrl = "http://somefrontendurl";
    private final String mockedAppellantMobileNumber = "+447123456789";
    private final String tribunalCentre = "Taylor House";

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;

    private AipCmrRelistedAppellantSmsPersonalisation aipCmrRelistedAppellantSmsPersonalisation;

    @BeforeEach
    void setup() {
        aipCmrRelistedAppellantSmsPersonalisation = new AipCmrRelistedAppellantSmsPersonalisation(
            templateId,
            iaAipFrontendUrl,
            personalisationProvider,
            recipientsFinder,
            hearingDetailsFinder
        );
        when(hearingDetailsFinder.getCmrHearingCentreName(asylumCase)).thenReturn(tribunalCentre);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, aipCmrRelistedAppellantSmsPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_CMR_RE_LISTING_AIP_APPELLANT_SMS",
            aipCmrRelistedAppellantSmsPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_mobile_number_from_subscribers() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobileNumber));
        Set<String> response = aipCmrRelistedAppellantSmsPersonalisation.getRecipientsList(asylumCase);
        verify(recipientsFinder, times(1)).findAll(asylumCase, NotificationType.SMS);
        assertTrue(response.contains(mockedAppellantMobileNumber));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> aipCmrRelistedAppellantSmsPersonalisation.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> aipCmrRelistedAppellantSmsPersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getCmrRelistingPersonalisationMap());

        Map<String, String> personalisation = aipCmrRelistedAppellantSmsPersonalisation.getPersonalisation(callback);

        assertFalse(personalisation.isEmpty());
        assertThat(personalisation)
            .containsAllEntriesOf(getCmrRelistingPersonalisationMap())
            .containsEntry("tribunalCentre", tribunalCentre)
            .containsEntry("hyperlink to service", iaAipFrontendUrl);
    }

    private Map<String, String> getCmrRelistingPersonalisationMap() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "someReferenceNumber")
            .put("hearingDate", "19 Sep 2023")
            .put("hearingTime", "10:00")
            .build();
    }
}
