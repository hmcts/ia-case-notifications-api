package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OtherDetentionEngagementTeamFtpaSubmittedPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    private DetEmailService detEmailService;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private final String templateId = "someTemplateId";
    private final String adaPrefix = "Accelerated detained appeal";
    private final String detEmailAddress = "legalrep@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private OtherDetentionEngagementTeamFtpaSubmittedPersonalisation otherDetentionEngagementTeamFtpaSubmittedPersonalisation;

    OtherDetentionEngagementTeamFtpaSubmittedPersonalisationTest() {
    }

    @BeforeEach
    void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        String listingReference = "listingReference";
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(listingReference));

        String customerServicesTelephone = "555 555 555";
        when(customerServicesProvider.getCustomerServicesTelephone()).thenReturn(customerServicesTelephone);
        String customerServicesEmail = "customer.services@example.com";
        when(customerServicesProvider.getCustomerServicesEmail()).thenReturn(customerServicesEmail);
        when(detEmailService.getAdaDetEmailAddress()).thenReturn(detEmailAddress);

        otherDetentionEngagementTeamFtpaSubmittedPersonalisation = new OtherDetentionEngagementTeamFtpaSubmittedPersonalisation(
            templateId,
            adaPrefix,
            customerServicesProvider,
            detEmailService
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, otherDetentionEngagementTeamFtpaSubmittedPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_FTPA_SUBMITTED_DETENTION_ENGAGEMENT_TEAM_OTHER",
            otherDetentionEngagementTeamFtpaSubmittedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(otherDetentionEngagementTeamFtpaSubmittedPersonalisation.getRecipientsList(asylumCase)
            .contains(detEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> otherDetentionEngagementTeamFtpaSubmittedPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_maintain() {

        String listingReferenceIfPresent = "Listing reference: listingReference";
        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", adaPrefix)
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("ariaListingReferenceIfPresent", listingReferenceIfPresent)
                .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .build();

        Map<String, String> actualPersonalisation =
            otherDetentionEngagementTeamFtpaSubmittedPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
    }
}

