package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REINSTATED_DECISION_MAKER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REINSTATE_APPEAL_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REINSTATE_APPEAL_REASON;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeReinstateAppealPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AppealService appealService;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String legalRepReinstateAppealBeforeListingTemplateId = "SomeTemplate";
    private String legalRepReinstateAppealAfterListingTemplateId = "SomeTemplate";
    private String iaExUiFrontendUrl = "http://localhost";
    private String legalRepEmailAddress = "legalRep@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private String ariaListingReference = "someAriaListingReference";

    private String reinstateAppealDate = "2020-10-08";
    private String reinstateAppealReason = "someReason";
    private String reinstatedDecisionMaker = "someDecisionMaker";

    private LegalRepresentativeReinstateAppealPersonalisation legalRepresentativeReinstateAppealPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));
        when(asylumCase.read(REINSTATE_APPEAL_DATE, String.class)).thenReturn(Optional.of(reinstateAppealDate));
        when(asylumCase.read(REINSTATE_APPEAL_REASON, String.class)).thenReturn(Optional.of(reinstateAppealReason));
        when(asylumCase.read(REINSTATED_DECISION_MAKER, String.class)).thenReturn(Optional.of(reinstatedDecisionMaker));

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepresentativeReinstateAppealPersonalisation = new LegalRepresentativeReinstateAppealPersonalisation(
            legalRepReinstateAppealBeforeListingTemplateId,
            legalRepReinstateAppealAfterListingTemplateId,
            iaExUiFrontendUrl,
            customerServicesProvider,
            appealService
        );
    }


    @Test
    public void should_return_given_template_id() {
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(legalRepReinstateAppealBeforeListingTemplateId,
            legalRepresentativeReinstateAppealPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(legalRepReinstateAppealAfterListingTemplateId,
            legalRepresentativeReinstateAppealPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_REINSTATE_APPEAL_LEGAL_REP",
            legalRepresentativeReinstateAppealPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeReinstateAppealPersonalisation.getRecipientsList(asylumCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeReinstateAppealPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeReinstateAppealPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        Map<String, String> expPersonalisation = ImmutableMap
                .<String, String>builder()
                .put(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER.value(), appealReferenceNumber)
                .put(APPELLANT_GIVEN_NAMES.value(), appellantGivenNames)
                .put(APPELLANT_FAMILY_NAME.value(), appellantFamilyName)
                .put(LEGAL_REP_REFERENCE_NUMBER.value(), legalRepRefNumber)
                .put(ARIA_LISTING_REFERENCE.value(), ariaListingReference)
                .put(REINSTATE_APPEAL_DATE.value(), "8 Oct 2020")
                .put(REINSTATE_APPEAL_REASON.value(), reinstateAppealReason)
                .put(REINSTATED_DECISION_MAKER.value(), reinstatedDecisionMaker)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
        Map<String, String> personalisation =
            legalRepresentativeReinstateAppealPersonalisation.getPersonalisation(asylumCase);

        assertThat(expPersonalisation).usingRecursiveComparison().isEqualTo(personalisation);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REINSTATE_APPEAL_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REINSTATE_APPEAL_REASON, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REINSTATED_DECISION_MAKER, String.class)).thenReturn(Optional.empty());
        Map<String, String> expPersonalisation = ImmutableMap
                .<String, String>builder()
                .put(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER.value(), "")
                .put(APPELLANT_GIVEN_NAMES.value(), "")
                .put(APPELLANT_FAMILY_NAME.value(), "")
                .put(LEGAL_REP_REFERENCE_NUMBER.value(), "")
                .put(ARIA_LISTING_REFERENCE.value(), "")
                .put(REINSTATE_APPEAL_DATE.value(), "")
                .put(REINSTATE_APPEAL_REASON.value(), "No reason given")
                .put(REINSTATED_DECISION_MAKER.value(), "")
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
        Map<String, String> personalisation =
            legalRepresentativeReinstateAppealPersonalisation.getPersonalisation(asylumCase);


        assertThat(expPersonalisation).usingRecursiveComparison().isEqualTo(personalisation);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals("No reason given", personalisation.get("reinstateAppealReason"));
    }
}
