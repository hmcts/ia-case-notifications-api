package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
class AppellantUpdateFtpaDecisionPersonalisationEmailTest {

    private final Long caseId = 12345L;
    private final String templateId = "someTemplateId";
    private final String iaAipFrontendUrl = "http://localhost/aip";
    private final String appealReferenceNumber = "PA/12345/2026";
    private final String homeOfficeReferenceNumber = "A1234567";
    private final String appellantGivenNames = "Talha";
    private final String appellantFamilyName = "Awan";
    private final Map<String, String> customerServices = ImmutableMap.of(
        "customerServicesTelephone", "555 555 555",
        "customerServicesEmail", "cust.services@example.com"
    );

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    RecipientsFinder recipientsFinder;

    private AppellantUpdateFtpaDecisionPersonalisationEmail personalisation;

    @BeforeEach
    void setUp() {
        personalisation = new AppellantUpdateFtpaDecisionPersonalisationEmail(
            templateId, iaAipFrontendUrl, customerServicesProvider, recipientsFinder);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, personalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_UPDATE_FTPA_DECISION_APPELLANT_EMAIL", personalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_appellant_email_recipients() {
        Set<String> emails = Collections.singleton("appellant@example.com");
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL)).thenReturn(emails);

        assertEquals(emails, personalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(customerServices);

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertThat(result)
            .containsAllEntriesOf(customerServices)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToService", iaAipFrontendUrl)
            .hasSize(7);
    }

    @Test
    void should_return_empty_values_when_information_missing() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(customerServices);

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertThat(result)
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("linkToService", iaAipFrontendUrl);
    }

    @Test
    void should_throw_exception_when_asylum_case_is_null() {
        assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation((AsylumCase) null));
    }
}
