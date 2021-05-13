package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LegalRepresentativeRequestFeeRemissionPersonalisationTest {

    @Mock private AsylumCase asylumCase;
    @Mock protected CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String templateId = "applyForLateRemissionTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "hmctsReference";
    private String legalRepReferenceNumber = "legalRepresentativeReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private LegalRepresentativeRequestFeeRemissionPersonalisation legalRepresentativeRequestFeeRemissionPersonalisation;

    @BeforeEach
    void setUp() {
        legalRepresentativeRequestFeeRemissionPersonalisation =
            new LegalRepresentativeRequestFeeRemissionPersonalisation(templateId, iaExUiFrontendUrl, customerServicesProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeRequestFeeRemissionPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {

        assertEquals(caseId + "_LEGAL_REPRESENTATIVE_APPLY_FOR_LATE_REMISSION",
            legalRepresentativeRequestFeeRemissionPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeRequestFeeRemissionPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        init();

        Map<String, String> personalisation =
            legalRepresentativeRequestFeeRemissionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {
        init();

        Map<String, String> personalisation =
            legalRepresentativeRequestFeeRemissionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    private void init() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
    }
}
