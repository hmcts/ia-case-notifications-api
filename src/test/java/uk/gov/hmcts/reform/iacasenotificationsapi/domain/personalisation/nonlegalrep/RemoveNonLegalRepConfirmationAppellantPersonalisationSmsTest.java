package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NLR_DETAILS;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RemoveNonLegalRepConfirmationAppellantPersonalisationSmsTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private final Long caseId = 12345L;
    private final String templateId = "removeNonLegalRepConfirmationAppellantSmsTemplateId";
    private final NonLegalRepDetails nlrDetails = NonLegalRepDetails.builder()
        .emailAddress("nlr@example.com")
        .givenNames("someGivenNames")
        .familyName("someFamilyName")
        .idamId("someIdamId")
        .build();
    private final String appealReferenceNumber = "hmctsReference";
    private final String appellantPhone = "07123456789";

    private RemoveNonLegalRepConfirmationAppellantPersonalisationSms removeNonLegalRepConfirmationAppellantPersonalisationSms;

    @BeforeEach
    void setUp() {
        removeNonLegalRepConfirmationAppellantPersonalisationSms = new RemoveNonLegalRepConfirmationAppellantPersonalisationSms(
            templateId,
            recipientsFinder
        );
    }

    @Test
    void should_return_appellant_phone_from_recipients_finder() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(appellantPhone));
        assertEquals(Collections.singleton(appellantPhone),
            removeNonLegalRepConfirmationAppellantPersonalisationSms.getRecipientsList(asylumCase));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {
        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> removeNonLegalRepConfirmationAppellantPersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId,
            removeNonLegalRepConfirmationAppellantPersonalisationSms.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_REMOVE_NON_LEGAL_REP_CONFIRMATION_APPELLANT_SMS",
            removeNonLegalRepConfirmationAppellantPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(nlrDetails));

        Map<String, String> personalisation =
            removeNonLegalRepConfirmationAppellantPersonalisationSms.getPersonalisation(callback);

        assertFalse(personalisation.isEmpty());
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(nlrDetails.getGivenNames(), personalisation.get("nlrGivenNames"));
        assertEquals(nlrDetails.getFamilyName(), personalisation.get("nlrFamilyName"));
    }

    @Test
    void should_return_personalisation_when_nlr_names_empty() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            removeNonLegalRepConfirmationAppellantPersonalisationSms.getPersonalisation(callback);

        assertFalse(personalisation.isEmpty());
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals("Sir /", personalisation.get("nlrGivenNames"));
        assertEquals("Madam", personalisation.get("nlrFamilyName"));
    }

    @Test
    void should_throw_exception_when_callback_is_null() {
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> removeNonLegalRepConfirmationAppellantPersonalisationSms.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }
}
