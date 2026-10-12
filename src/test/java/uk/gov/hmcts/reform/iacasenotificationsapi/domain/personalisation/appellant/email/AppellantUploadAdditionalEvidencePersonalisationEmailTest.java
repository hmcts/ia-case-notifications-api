package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantUploadAdditionalEvidencePersonalisationEmailTest {

    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final String afterListing24WeeksTemplateId = "afterListing24WeeksTemplateId";
    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "homeOfficeReferenceNumber";
    private final String ariaListingReference = "ariaListingReference";
    private final String appellantGivenNames = "appellantGivenNames";
    private final String appellantFamilyName = "appellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer.services@example.com";
    private final String iaAipFrontendUrl = "iaAipFrontendUrl";

    @Mock
    AsylumCase asylumCase;

    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private AppellantUploadAdditionalEvidencePersonalisationEmail appellantUploadAdditionalEvidencePersonalisationEmail;

    @BeforeEach
    void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));

        appellantUploadAdditionalEvidencePersonalisationEmail =
            new AppellantUploadAdditionalEvidencePersonalisationEmail(
                beforeListingTemplateId,
                afterListingTemplateId,
                afterListing24WeeksTemplateId,
                iaAipFrontendUrl,
                recipientsFinder,
                customerServicesProvider
            );
    }

    @Test
    void should_return_null_for_getTemplateId_without_case() {
        assertNull(appellantUploadAdditionalEvidencePersonalisationEmail.getTemplateId());
    }

    @Test
    void should_return_before_listing_template_id_when_not_listed() {
        assertEquals(beforeListingTemplateId,
            appellantUploadAdditionalEvidencePersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_after_listing_template_id_for_non_24_week_listed_case() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.empty());

        assertEquals(afterListingTemplateId,
            appellantUploadAdditionalEvidencePersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_24_weeks_template_id_for_24_week_listed_case() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertEquals(afterListing24WeeksTemplateId,
            appellantUploadAdditionalEvidencePersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(12345L + "_UPLOADED_ADDITIONAL_EVIDENCE_AIP_APPELLANT_EMAIL",
            appellantUploadAdditionalEvidencePersonalisationEmail.getReferenceId(12345L));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantUploadAdditionalEvidencePersonalisationEmail.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            appellantUploadAdditionalEvidencePersonalisationEmail.getPersonalisation(asylumCase);
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("hyperlink to service", iaAipFrontendUrl);
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantUploadAdditionalEvidencePersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation).allSatisfy((key, value) -> {
            if (!Objects.equals("hyperlink to service", key)) {
                assertThat(value).isEmpty();
            }
        });
    }
}
