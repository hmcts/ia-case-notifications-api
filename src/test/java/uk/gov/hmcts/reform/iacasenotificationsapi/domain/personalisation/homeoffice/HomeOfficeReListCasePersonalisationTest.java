package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeReListCasePersonalisationTest {

    private final String templateId = "someTemplateId";
    private final String homeOfficeEmailAddress = "homeoffice@example.com";
    private final String listCaseHomeOfficeEmailAddress = "listCaseHomeOffice@example.com";

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private HomeOfficeReListCasePersonalisation homeOfficeReListCasePersonalisation;

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(listCaseHomeOfficeEmailAddress);
        when(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeEmailAddress);

        homeOfficeReListCasePersonalisation = new HomeOfficeReListCasePersonalisation(
            templateId,
            personalisationProvider,
            emailAddressFinder,
            customerServicesProvider
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, homeOfficeReListCasePersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_RE_LIST_CASE_HOME_OFFICE",
            homeOfficeReListCasePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_list_case_home_office_email_for_non_remote_hearing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        assertTrue(homeOfficeReListCasePersonalisation.getRecipientsList(asylumCase)
            .contains(listCaseHomeOfficeEmailAddress));
    }

    @Test
    void should_return_home_office_email_for_remote_hearing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));

        assertTrue(homeOfficeReListCasePersonalisation.getRecipientsList(asylumCase)
            .contains(homeOfficeEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_callback_is_null() {
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> homeOfficeReListCasePersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMap());

        Map<String, String> personalisation = homeOfficeReListCasePersonalisation.getPersonalisation(callback);

        assertFalse(personalisation.isEmpty());
        assertThat(personalisation)
            .containsAllEntriesOf(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase))
            .containsAllEntriesOf(personalisationProvider.getPersonalisation(callback));
    }

    private Map<String, String> getPersonalisationMap() {
        return ImmutableMap.<String, String>builder()
            .put("appealReferenceNumber", "HU/12345/2024")
            .put("ariaListingReference", "someAriaListingReference")
            .put("homeOfficeReferenceNumber", "A1234567")
            .put("appellantGivenNames", "John")
            .put("appellantFamilyName", "Smith")
            .put("hearingCentreName", "Manchester")
            .put("hearingDate", "12 Jan 2026")
            .put("hearingTime", "10:00")
            .put("hearingCentreAddress", "Manchester Civil Justice Centre, 1 Bridge Street West, Manchester, M60 9DJ")
            .put("oldHearingCentre", "Taylor House")
            .put("oldHearingDate", "05 Jan 2026")
            .put("linkToOnlineService", "http://localhost")
            .put("customerServicesTelephone", "0300 123 1711")
            .put("customerServicesEmail", "customer.service@justice.gov.uk")
            .build();
    }
}
