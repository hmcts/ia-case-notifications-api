package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseOfficerAipCmrHearingCancelledPersonalisationEmailTest {

    private final Long caseId = 12345L;
    private final String cmrCancelledCaseOfficerEmailTemplateId = "cmrCancelledCaseOfficerEmailTemplateId";
    private final String iaExUiFrontendUrl = "http://somexuiurl";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String caseOfficerEmail = "caseofficer@example.com";

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;

    private CaseOfficerAipCmrHearingCancelledPersonalisationEmail caseOfficerAipCmrHearingCancelledPersonalisationEmail;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        caseOfficerAipCmrHearingCancelledPersonalisationEmail = new CaseOfficerAipCmrHearingCancelledPersonalisationEmail(
            cmrCancelledCaseOfficerEmailTemplateId,
            iaExUiFrontendUrl,
            emailAddressFinder
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(cmrCancelledCaseOfficerEmailTemplateId,
            caseOfficerAipCmrHearingCancelledPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_CMR_CANCELLED_AIP_CASE_OFFICER_EMAIL",
            caseOfficerAipCmrHearingCancelledPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_list() {
        when(emailAddressFinder.getCmrListingCaseOfficerHearingCentreEmailAddress(asylumCase))
            .thenReturn(caseOfficerEmail);

        assertThat(caseOfficerAipCmrHearingCancelledPersonalisationEmail.getRecipientsList(asylumCase))
            .containsExactly(caseOfficerEmail);
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> caseOfficerAipCmrHearingCancelledPersonalisationEmail.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
            caseOfficerAipCmrHearingCancelledPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
    }
}