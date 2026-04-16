package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantSubmitAppealPersonalisationSmsTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    AppealService appealService;

    private final Long caseId = 12345L;
    private final String smsTemplateId = "someSmsTemplateId";
    private final String nonAipSmsTemplateId = "nonAipSmsTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";

    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAppellantMobilePhone = "07123456789";
    private final String dateOfBirth = "2020-03-01";
    private final String expectedDateOfBirth = "1 Mar 2020";

    private final String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String mockedAppellantGivenNames = "someAppellantGivenNames";
    private final String mockedAppellantFamilyName = "someAppellantFamilyName";

    private AppellantSubmitAppealPersonalisationSms appellantSubmitAppealPersonalisationSms;

    @BeforeEach
    void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);


        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        String mockedLegalRepAppealReferenceNumber = "someReferenceNumber";
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedLegalRepAppealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));

        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppellantMobilePhone));

        appellantSubmitAppealPersonalisationSms = new AppellantSubmitAppealPersonalisationSms(
            smsTemplateId,
            nonAipSmsTemplateId,
            iaAipFrontendUrl,
            28,
            recipientsFinder,
            systemDateProvider,
            appealService);
    }

    @Test
    void should_return_given_template_id() {
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(true);
        assertEquals(smsTemplateId, appellantSubmitAppealPersonalisationSms.getTemplateId(asylumCase));
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(false);
        assertEquals(nonAipSmsTemplateId, appellantSubmitAppealPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_APPEAL_SUBMITTED_APPELLANT_AIP_SMS",
            appellantSubmitAppealPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
assertThrows(NullPointerException.class, () -> appellantSubmitAppealPersonalisationSms.getRecipientsList(null))
            ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_recipients_when_mobile_is_empty() {

        when(asylumCase.read(MOBILE_NUMBER,String.class))
            .thenReturn(Optional.empty());
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(false);

        IllegalStateException exception =
assertThrows(IllegalStateException.class, () -> appellantSubmitAppealPersonalisationSms.getRecipientsList(asylumCase))
            ;
assertEquals("appellantMobileNumber is not present", exception.getMessage());
    }


    @Test
    void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(true);
        assertTrue(
            appellantSubmitAppealPersonalisationSms.getRecipientsList(asylumCase).contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_return_given_mobile_number_from_asylum_case_in_non_aip_case() {

        when(asylumCase.read(MOBILE_NUMBER))
            .thenReturn(Optional.of(mockedAppellantMobilePhone));
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(false);

        assertTrue(
            appellantSubmitAppealPersonalisationSms.getRecipientsList(asylumCase).contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
assertThrows(NullPointerException.class, () -> appellantSubmitAppealPersonalisationSms.getPersonalisation((Callback<AsylumCase>) null))
            ;
assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_date_of_birth_is_null() {
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
assertThrows(IllegalStateException.class, () -> appellantSubmitAppealPersonalisationSms.getPersonalisation(callback))
            ;
assertEquals("Appellant's birth of date is not present", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        final String dueDate = LocalDate.now().plusDays(28)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(28)).thenReturn(dueDate);

        Map<String, String> personalisation = appellantSubmitAppealPersonalisationSms.getPersonalisation(callback);
        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("due date", dueDate)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);

        assertThat(personalisation)
            .containsEntry("Ref Number", "" + caseId)
            .containsEntry("Legal Rep Ref", mockedAppealReferenceNumber)
            .containsEntry("HO Ref Number", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("Given names", mockedAppellantGivenNames)
            .containsEntry("Family name", mockedAppellantFamilyName)
            .containsEntry("Date Of Birth", expectedDateOfBirth);



    }

    @Test
    void should_return_personalisation_when_only_mandatory_information_given() {
        final String dueDate = LocalDate.now().plusDays(28)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(systemDateProvider.dueDate(28)).thenReturn(dueDate);


        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));

        Map<String, String> personalisation = appellantSubmitAppealPersonalisationSms.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", "")
            .containsEntry("due date", dueDate)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);

        assertThat(personalisation)
            .containsEntry("Ref Number", "" + caseId)
            .containsEntry("Legal Rep Ref", "")
            .containsEntry("HO Ref Number", "")
            .containsEntry("Given names", "")
            .containsEntry("Family name", "")
            .containsEntry("Date Of Birth", expectedDateOfBirth);

    }
}
