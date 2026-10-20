package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantUploadAdditionalEvidencePersonalisationSmsTest {
    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final String afterListing24WeeksTemplateId = "afterListing24WeeksTemplateId";
    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private final String iaAipFrontendUrl = "iaAipFrontendUrl";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    private AppellantUploadAdditionalEvidencePersonalisationSms appellantUploadAdditionalEvidencePersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantUploadAdditionalEvidencePersonalisationSms = new AppellantUploadAdditionalEvidencePersonalisationSms(
            beforeListingTemplateId,
            afterListingTemplateId,
            afterListing24WeeksTemplateId,
            iaAipFrontendUrl,
            recipientsFinder);
    }

    @Test
    void should_return_before_listing_template_id_when_not_listed() {
        assertEquals(beforeListingTemplateId,
            appellantUploadAdditionalEvidencePersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    void should_return_after_listing_template_id_when_listed_and_not_24_weeks() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        assertEquals(afterListingTemplateId,
            appellantUploadAdditionalEvidencePersonalisationSms.getTemplateId(asylumCase));
    }

    @ParameterizedTest
    @CsvSource(value = {"HATTON_CROSS", "null"}, nullValues = "null")
    void should_return_after_listing_24_weeks_template_id_when_24_weeks(HearingCentre hearingCentre) {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.ofNullable(hearingCentre));
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertEquals(afterListing24WeeksTemplateId,
            appellantUploadAdditionalEvidencePersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(12345L + "_UPLOADED_ADDITIONAL_EVIDENCE_AIP_APPELLANT_SMS",
            appellantUploadAdditionalEvidencePersonalisationSms.getReferenceId(12345L));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantUploadAdditionalEvidencePersonalisationSms.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        String mockedAppellantMobilePhone = "07123456789";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantUploadAdditionalEvidencePersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantUploadAdditionalEvidencePersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
            appellantUploadAdditionalEvidencePersonalisationSms.getPersonalisation(asylumCase);
        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("hyperlink to service", iaAipFrontendUrl);

    }
}
