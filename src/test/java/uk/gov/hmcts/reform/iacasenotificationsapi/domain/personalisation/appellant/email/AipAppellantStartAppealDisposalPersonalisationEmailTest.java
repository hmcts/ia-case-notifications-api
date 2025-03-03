package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipAppellantStartAppealDisposalPersonalisationEmailTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    AppealService appealService;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private final Long caseId = 12345L;
    private final String emailTemplateId = "someEmailTemplateId";
    private final String mockedAppellantEmailAddress = "appelant@example.net";

    private AipAppellantStartAppealDisposalPersonalisationEmail aipAppellantStartAppealDisposalPersonalisationEmail;

    @BeforeEach
    void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        String mockedAppealReferenceNumber = "someReferenceNumber";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        String mockedLegalRepAppealReferenceNumber = "someReferenceNumber";
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedLegalRepAppealReferenceNumber));

        String mockedAppellantGivenNames = "someAppellantGivenNames";
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        String mockedAppellantFamilyName = "someAppellantFamilyName";
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));

        String dateOfBirth = "2020-03-01";
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));
        when(asylumCase.read(EMAIL, String.class)).thenReturn(Optional.of(mockedAppellantEmailAddress));
        String customerServicesTelephone = "555 555 555";
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        String customerServicesEmail = "cust.services@example.com";
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        String iaAipFrontendUrl = "http://localhost";
        aipAppellantStartAppealDisposalPersonalisationEmail = new AipAppellantStartAppealDisposalPersonalisationEmail(
            emailTemplateId,
            iaAipFrontendUrl,
            recipientsFinder,
            appealService,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(emailTemplateId, aipAppellantStartAppealDisposalPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_APPEAL_STARTED_APPELLANT_AIP_DISPOSAL",
            aipAppellantStartAppealDisposalPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_list_from_subscribers_in_asylum_case() {
        // given
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(true);
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(false);

        // when
        // then
        assertTrue(aipAppellantStartAppealDisposalPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    void should_return_given_email_address_in_asylum_case_in_non_aip_case() {
        // given
        when(asylumCase.read(EMAIL))
            .thenReturn(Optional.of(mockedAppellantEmailAddress));

        // when
        // then
        assertTrue(aipAppellantStartAppealDisposalPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    void should_throw_an_exception_email_address_empty() {
        // given
        when(asylumCase.read(EMAIL,String.class))
            .thenReturn(Optional.empty());
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(false);

        // when
        // then
        assertThatThrownBy(() -> aipAppellantStartAppealDisposalPersonalisationEmail.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantEmailAddress is not present");

    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        // given
        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        // when
        // then
        assertThatThrownBy(() -> aipAppellantStartAppealDisposalPersonalisationEmail.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_throw_exception_on_personalisation_when_date_of_birth_is_null() {
        // given
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> aipAppellantStartAppealDisposalPersonalisationEmail.getPersonalisation(callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appellant's birth of date is not present");
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {
        // given
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        // when
        Map<String, String> personalisation =
                aipAppellantStartAppealDisposalPersonalisationEmail.getPersonalisation(callback);

        // then
        assertThat(personalisation).isNotEmpty();
        assertThat(personalisation).isEqualToComparingOnlyGivenFields(asylumCase);
    }
}
