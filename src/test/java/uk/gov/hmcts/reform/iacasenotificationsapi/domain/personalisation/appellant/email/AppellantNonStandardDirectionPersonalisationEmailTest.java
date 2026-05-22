package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantNonStandardDirectionPersonalisationEmailTest {

    private final String templateId = "templateId";
    private final String iaAipFrontendUrl = "http://localhost/";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String mockedAriaListingReference = "someAriaListingReference";
    private final String mockedAppellantGivenNames = "someAppellantGivenNames";
    private final String mockedAppellantFamilyName = "someAppellantFamilyName";
    private final String iaServicesPhone = "0100000000";
    private final String iaServicesEmail = "services@email.com";
    private final Map<String, String> customerServices = Map.of("customerServicesTelephone", iaServicesPhone,
        "customerServicesEmail", iaServicesEmail);
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    private AppellantNonStandardDirectionPersonalisationEmail appellantNonStandardDirectionPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(mockedAriaListingReference));

        appellantNonStandardDirectionPersonalisationEmail =
            new AppellantNonStandardDirectionPersonalisationEmail(
                templateId,
                iaAipFrontendUrl,
                recipientsFinder,
                customerServicesProvider
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, appellantNonStandardDirectionPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_APPELLANT_NON_STANDARD_DIRECTION_EMAIL",
            appellantNonStandardDirectionPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        String mockedAppellantEmailAddress = "appelant@example.net";
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantNonStandardDirectionPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantNonStandardDirectionPersonalisationEmail.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(customerServices);

        Map<String, String> personalisation =
            appellantNonStandardDirectionPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", mockedAppealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("listingReferenceLine", "\nListing reference: " + mockedAriaListingReference)
            .containsEntry("appellantGivenNames", mockedAppellantGivenNames)
            .containsEntry("appellantFamilyName", mockedAppellantFamilyName)
            .containsEntry("linkToTimelinePage", iaAipFrontendUrl)
            .containsEntry("customerServicesTelephone", iaServicesPhone)
            .containsEntry("customerServicesEmail", iaServicesEmail);
    }
}
