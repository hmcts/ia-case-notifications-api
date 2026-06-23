package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AdminOfficerPersonalisationProviderTest {

    private final String iaExUiFrontendUrl = "http://somefrontendurl";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    @Mock
    AsylumCase asylumCase;
    private AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.GLASGOW));
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
            .thenReturn(Optional.of(AppealDecision.ALLOWED));

        adminOfficerPersonalisationProvider = new AdminOfficerPersonalisationProvider(
            iaExUiFrontendUrl
        );
    }

    @Test
    public void should_return_default_personalisation() {

        Map<String, String> personalisation = adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
    }

    @Test
    public void should_return_admin_personalisation() {

        Map<String, String> personalisation = adminOfficerPersonalisationProvider.getAdminPersonalisation(asylumCase);

        String applicationDecision = "ALLOWED";
        String hearingCentre = "GLASGOW";
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("hearingCentre", hearingCentre)
            .containsEntry("applicationDecision", applicationDecision);
    }

    @Test
    public void should_return_reviewed_hearing_requirements_personalisation() {
        Map<String, String> personalisation =
            adminOfficerPersonalisationProvider.getReviewedHearingRequirementsPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
    }

    @Test
    public void should_return_change_to_hearing_requirements_personalisation() {

        Map<String, String> personalisation =
            adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("ariaListingReference", ariaListingReference);
    }
}
