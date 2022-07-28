package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.END_APPEAL_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeAppealEndedAutomaticallyPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    EmailAddressFinder emailAddressFinder;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String emailAddress = "homeoffice@example.com";

    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String endAppealDate = "2019-08-27";
    private String expectedEndAppealDate = "27 Aug 2019";

    private HomeOfficeAppealEndedAutomaticallyPersonalisation homeOfficeAppealEndedAutomaticallyPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.of(endAppealDate));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(emailAddress);

        homeOfficeAppealEndedAutomaticallyPersonalisation = new HomeOfficeAppealEndedAutomaticallyPersonalisation(
            emailAddress,
            templateId,
            iaExUiFrontendUrl,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, homeOfficeAppealEndedAutomaticallyPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPEAL_ENDED_AUTOMATICALLY_HOME_OFFICE",
            homeOfficeAppealEndedAutomaticallyPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address() {
        assertTrue(homeOfficeAppealEndedAutomaticallyPersonalisation.getRecipientsList(asylumCase)
            .contains(emailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> homeOfficeAppealEndedAutomaticallyPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = homeOfficeAppealEndedAutomaticallyPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(expectedEndAppealDate, personalisation.get("endAppealDate"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = homeOfficeAppealEndedAutomaticallyPersonalisation
            .getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("endAppealDate"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
    }
}
