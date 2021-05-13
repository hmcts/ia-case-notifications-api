package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseOfficerEditListingPersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String hearingCentreEmailAddress = "hearingCentre@example.com";
    private String listCaseHearingCentreEmailAddress = "listCaseHearingCentre@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String appellantGivenNames = "appellantGivenNames";
    private String appellantFamilyName = "appellantFamilyName";
    private String homeOfficeRefNumber = "homeOfficeRefNumber";
    private String hearingCentreName = "The Hearing Centre";

    private CaseOfficerEditListingPersonalisation caseOfficerEditListingPersonalisation;

    @BeforeEach
    public void setup() {

        when(emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase)).thenReturn(listCaseHearingCentreEmailAddress);
        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);

        caseOfficerEditListingPersonalisation = new CaseOfficerEditListingPersonalisation(
            templateId,
            emailAddressFinder,
            personalisationProvider
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerEditListingPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_RE_LISTED_CASE_OFFICER",
            caseOfficerEditListingPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_lookup_map() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        assertTrue(caseOfficerEditListingPersonalisation.getRecipientsList(asylumCase).contains(listCaseHearingCentreEmailAddress));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));

        assertTrue(caseOfficerEditListingPersonalisation.getRecipientsList(asylumCase).contains(hearingCentreEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(() -> caseOfficerEditListingPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithBlankValues());

        Map<String, String> personalisation = caseOfficerEditListingPersonalisation.getPersonalisation(callback);

        assertEquals(getPersonalisationMapWithBlankValues().get("appealReferenceNumber"), personalisation.get("appealReferenceNumber"));
        assertEquals(getPersonalisationMapWithBlankValues().get("ariaListingReference"), personalisation.get("ariaListingReference"));
        assertEquals(getPersonalisationMapWithBlankValues().get("homeOfficeReference"), personalisation.get("homeOfficeReference"));
        assertEquals(getPersonalisationMapWithBlankValues().get("appellantGivenNames"), personalisation.get("appellantGivenNames"));
        assertEquals(getPersonalisationMapWithBlankValues().get("appellantFamilyName"), personalisation.get("appellantFamilyName"));
        assertEquals(getPersonalisationMapWithBlankValues().get("customerServicesTelephone"), personalisation.get("customerServicesTelephone"));
        assertEquals(getPersonalisationMapWithBlankValues().get("customerServicesEmail"), personalisation.get("customerServicesEmail"));
        assertEquals(getPersonalisationMapWithBlankValues().get("linkToOnlineService"), personalisation.get("linkToOnlineService"));
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation = caseOfficerEditListingPersonalisation.getPersonalisation(callback);

        assertEquals(getPersonalisationMapWithGivenValues().get("appealReferenceNumber"), personalisation.get("appealReferenceNumber"));
        assertEquals(getPersonalisationMapWithGivenValues().get("ariaListingReference"), personalisation.get("ariaListingReference"));
        assertEquals(getPersonalisationMapWithGivenValues().get("appellantGivenNames"), personalisation.get("appellantGivenNames"));
        assertEquals(getPersonalisationMapWithGivenValues().get("appellantFamilyName"), personalisation.get("appellantFamilyName"));
        assertEquals(getPersonalisationMapWithGivenValues().get("customerServicesTelephone"), personalisation.get("customerServicesTelephone"));
        assertEquals(getPersonalisationMapWithGivenValues().get("customerServicesEmail"), personalisation.get("customerServicesEmail"));
        assertEquals(getPersonalisationMapWithGivenValues().get("linkToOnlineService"), personalisation.get("linkToOnlineService"));
        assertEquals(getPersonalisationMapWithGivenValues().get("hearingCentreName"), personalisation.get("hearingCentreName"));
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("hearingCentreName", hearingCentreName)
            .build();
    }

    private Map<String, String> getPersonalisationMapWithBlankValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "")
            .put("ariaListingReference", "")
            .put("homeOfficeReferenceNumber", "")
            .put("appellantGivenNames", "")
            .put("appellantFamilyName", "")
            .put("linkToOnlineService", "")
            .put("hearingCentreName", "")
            .build();
    }
}
