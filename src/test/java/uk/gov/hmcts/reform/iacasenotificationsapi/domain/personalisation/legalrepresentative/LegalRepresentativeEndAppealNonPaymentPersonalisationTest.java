package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@RunWith(MockitoJUnitRunner.class)
public class LegalRepresentativeEndAppealNonPaymentPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String legalRepEmailAddress = "legalrep@example.com";

    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String endAppealOutcome = "someEndAppealOutcome";
    private String endAppealOutcomeReason = "someEndAppealOutcomeReason";
    private String endAppealApproverType = "someEndAppealApproverType";
    private String endAppealDate = "2019-08-27";
    private String expectedEndAppealDate = "27 Aug 2019";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private LegalRepresentativeEndAppealNonPaymentPersonalisation legalRepresentativeEndAppealNonPaymentPersonalisation;

    @Before
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.of(endAppealOutcome));
        when(asylumCase.read(END_APPEAL_OUTCOME_REASON, String.class)).thenReturn(Optional.of(endAppealOutcomeReason));
        when(asylumCase.read(END_APPEAL_APPROVER_TYPE, String.class)).thenReturn(Optional.of(endAppealApproverType));
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.of(endAppealDate));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmailAddress));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepresentativeEndAppealNonPaymentPersonalisation = new LegalRepresentativeEndAppealNonPaymentPersonalisation(
            templateId,
            iaExUiFrontendUrl,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeEndAppealNonPaymentPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPEAL_ENDED_NON_PAYMENT_LEGAL_REPRESENTATIVE", legalRepresentativeEndAppealNonPaymentPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeEndAppealNonPaymentPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeEndAppealNonPaymentPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> legalRepresentativeEndAppealNonPaymentPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = legalRepresentativeEndAppealNonPaymentPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(legalRepRefNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(endAppealOutcome, personalisation.get("outcomeOfAppeal"));
        assertEquals(endAppealOutcomeReason, personalisation.get("reasonsOfOutcome"));
        assertEquals(endAppealApproverType, personalisation.get("endAppealApprover"));
        assertEquals(expectedEndAppealDate, personalisation.get("endAppealDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_OUTCOME_REASON, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_APPROVER_TYPE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = legalRepresentativeEndAppealNonPaymentPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("legalRepReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("outcomeOfAppeal"));
        assertEquals("No reason", personalisation.get("reasonsOfOutcome"));
        assertEquals("", personalisation.get("endAppealApprover"));
        assertEquals("", personalisation.get("endAppealDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
