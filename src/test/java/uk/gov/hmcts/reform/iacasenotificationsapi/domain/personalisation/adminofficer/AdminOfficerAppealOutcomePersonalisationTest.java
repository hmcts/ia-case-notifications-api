package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_DECISION_ALLOWED;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminOfficerAppealOutcomePersonalisationTest {

    @Mock
    AsylumCase asylumCase;

    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    @Mock
    AdminOfficerAppealOutcomePersonalisation adminOfficerAppealOutcomePersonalisation;


    private Long caseId = 12345L;

    private EmailAddressFinder emailAddressFinder;
    private String decisionAndReasonUploadedTemplateId = "someTemplateId";

    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String hearingCentre = "someHearingCentre";

    private String applicationDecision = "someDecision";

    private String iaExUiFrontendUrl;


    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HEARING_CENTRE, String.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
                .thenReturn(Optional.of(AppealDecision.ALLOWED));

        adminOfficerPersonalisationProvider = new AdminOfficerPersonalisationProvider(iaExUiFrontendUrl);

        adminOfficerAppealOutcomePersonalisation = new AdminOfficerAppealOutcomePersonalisation(
                decisionAndReasonUploadedTemplateId,
                adminOfficerPersonalisationProvider,
                emailAddressFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(decisionAndReasonUploadedTemplateId, adminOfficerAppealOutcomePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPEAL_OUTCOME_ADMIN",
                adminOfficerAppealOutcomePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> adminOfficerAppealOutcomePersonalisation.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = adminOfficerPersonalisationProvider.getAdminPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("someReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("someAriaListingReference"));
        assertEquals(appellantGivenNames, personalisation.get("someAppellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("someAppellantFamilyName"));
//        assertEquals(hearingCentre, personalisation.get("someHearingCentre"));
//        assertEquals(applicationDecision, personalisation.get("someDecision"));

    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HEARING_CENTRE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_DECISION_ALLOWED, String.class)).thenReturn(Optional.empty());


        Map<String, String> personalisation = adminOfficerPersonalisationProvider.getAdminPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("hearingCentre"));
        assertEquals("", personalisation.get("applicationDecision"));

    }

//    private Map<String, String> getPersonalisationMapWithGivenValues() {
//        return ImmutableMap
//                .<String, String>builder()
//                .put("appealReferenceNumber", appealReferenceNumber)
//                .put("ariaListingReference", ariaListingReference)
//                .put("appellantGivenNames", appellantGivenNames)
//                .put("appellantFamilyName", appellantFamilyName)
//                .put("hearingCentre", hearingCentre)
//                .put("applicationDecision", applicationDecision)
//                .build();
//    }

}
