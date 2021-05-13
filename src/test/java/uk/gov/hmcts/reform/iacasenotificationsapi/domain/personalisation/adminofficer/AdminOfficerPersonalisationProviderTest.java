package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
class AdminOfficerPersonalisationProviderTest {

    @Mock
    AsylumCase asylumCase;

    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String ariaListingReference = "someAriaListingReference";


    @BeforeEach
    void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        adminOfficerPersonalisationProvider = new AdminOfficerPersonalisationProvider(
            iaExUiFrontendUrl
        );
    }

    @Test
    void should_return_default_personalisation() {

        Map<String, String> personalisation = adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));

    }

    @Test
    void should_return_reviewed_hearing_requirements_personalisation() {

        Map<String, String> personalisation =
            adminOfficerPersonalisationProvider.getReviewedHearingRequirementsPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));

    }

    @Test
    void should_return_change_to_hearing_requirements_personalisation() {

        Map<String, String> personalisation =
            adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase);
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
    }
}
