package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeEditListingNoChangePersonalisationTest {

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String templateIdRemoteHearing = "remoteTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String legalRepEmailAddress = "legalRep@example.com";
    private String hearingCentreAddress = "some hearing centre address";

    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String appellantGivenNames = "appellantGivenNames";
    private String appellantFamilyName = "appellantFamilyName";
    private String homeOfficeRefNumber = "homeOfficeRefNumber";

    private String hearingCentreNameBefore = HearingCentre.MANCHESTER.toString();
    private String requirementsVulnerabilities = "someRequirementsVulnerabilities";
    private String requirementsMultimedia = "someRequirementsMultimedia";
    private String requirementsInCamera = "someRequirementsInCamera";
    private String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
    private String requirementsOther = "someRequirementsOther";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private LegalRepresentativeEditListingNoChangePersonalisation legalRepresentativeEditListingPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));

        legalRepresentativeEditListingPersonalisation = new LegalRepresentativeEditListingNoChangePersonalisation(
            templateId,
            templateIdRemoteHearing,
            personalisationProvider,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_template_id() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        assertEquals(templateId, legalRepresentativeEditListingPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));

        assertEquals(templateIdRemoteHearing, legalRepresentativeEditListingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_RE_LISTED_NO_CHANGE_LEGAL_REPRESENTATIVE",
            legalRepresentativeEditListingPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(
            legalRepresentativeEditListingPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeEditListingPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeEditListingPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation =
            legalRepresentativeEditListingPersonalisation.getPersonalisation(callback);

        assertEquals(getPersonalisationMapWithGivenValues().get("appealReferenceNumber"), personalisation.get("appealReferenceNumber"));
        assertEquals(getPersonalisationMapWithGivenValues().get("ariaListingReference"), personalisation.get("ariaListingReference"));
        assertEquals(getPersonalisationMapWithGivenValues().get("appellantGivenNames"), personalisation.get("appellantGivenNames"));
        assertEquals(getPersonalisationMapWithGivenValues().get("appellantFamilyName"), personalisation.get("appellantFamilyName"));
        assertEquals(getPersonalisationMapWithGivenValues().get("linkToOnlineService"), personalisation.get("linkToOnlineService"));
        assertEquals(getPersonalisationMapWithGivenValues().get("Hearing Requirement Vulnerabilities"), personalisation.get("Hearing Requirement Vulnerabilities"));
        assertEquals(getPersonalisationMapWithGivenValues().get("Hearing Requirement Multimedia"), personalisation.get("Hearing Requirement Multimedia"));
        assertEquals(getPersonalisationMapWithGivenValues().get("Hearing Requirement Single Sex Court"), personalisation.get("Hearing Requirement Single Sex Court"));
        assertEquals(getPersonalisationMapWithGivenValues().get("Hearing Requirement In Camera Court"), personalisation.get("Hearing Requirement In Camera Court"));
        assertEquals(getPersonalisationMapWithGivenValues().get("Hearing Requirement Other"), personalisation.get("Hearing Requirement Other"));
        assertEquals(getPersonalisationMapWithGivenValues().get("oldHearingCentre"), personalisation.get("oldHearingCentre"));
        assertEquals(getPersonalisationMapWithGivenValues().get(HEARING_CENTRE_ADDRESS), personalisation.get(HEARING_CENTRE_ADDRESS));
        assertEquals(getPersonalisationMapWithGivenValues().get("customerServicesTelephone"), personalisation.get("customerServicesTelephone"));
        assertEquals(getPersonalisationMapWithGivenValues().get("customerServicesEmail"), personalisation.get("customerServicesEmail"));


    }

    @Test
    public void should_return_personalisation_when_optional_fields_are_blank() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithBlankValues());

        Map<String, String> personalisation =
            legalRepresentativeEditListingPersonalisation.getPersonalisation(callback);

        assertEquals(getPersonalisationMapWithBlankValues().get("appealReferenceNumber"), personalisation.get("appealReferenceNumber"));
        assertEquals(getPersonalisationMapWithBlankValues().get("ariaListingReference"), personalisation.get("ariaListingReference"));
        assertEquals(getPersonalisationMapWithBlankValues().get("appellantGivenNames"), personalisation.get("appellantGivenNames"));
        assertEquals(getPersonalisationMapWithBlankValues().get("appellantFamilyName"), personalisation.get("appellantFamilyName"));
        assertEquals(getPersonalisationMapWithBlankValues().get("linkToOnlineService"), personalisation.get("linkToOnlineService"));
        assertEquals(getPersonalisationMapWithBlankValues().get("Hearing Requirement Vulnerabilities"), personalisation.get("Hearing Requirement Vulnerabilities"));
        assertEquals(getPersonalisationMapWithBlankValues().get("Hearing Requirement Multimedia"), personalisation.get("Hearing Requirement Multimedia"));
        assertEquals(getPersonalisationMapWithBlankValues().get("Hearing Requirement Single Sex Court"), personalisation.get("Hearing Requirement Single Sex Court"));
        assertEquals(getPersonalisationMapWithBlankValues().get("Hearing Requirement In Camera Court"), personalisation.get("Hearing Requirement In Camera Court"));
        assertEquals(getPersonalisationMapWithBlankValues().get("Hearing Requirement Other"), personalisation.get("Hearing Requirement Other"));
        assertEquals(getPersonalisationMapWithBlankValues().get("oldHearingCentre"), personalisation.get("oldHearingCentre"));
        assertEquals(getPersonalisationMapWithBlankValues().get(HEARING_CENTRE_ADDRESS), personalisation.get(HEARING_CENTRE_ADDRESS));
        assertEquals(getPersonalisationMapWithBlankValues().get("customerServicesTelephone"), personalisation.get("customerServicesTelephone"));
        assertEquals(getPersonalisationMapWithBlankValues().get("customerServicesEmail"), personalisation.get("customerServicesEmail"));

    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            //.put("Hyperlink to user’s case list", iaExUiFrontendUrl)
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("Hearing Requirement Vulnerabilities", requirementsVulnerabilities)
            .put("Hearing Requirement Multimedia", requirementsMultimedia)
            .put("Hearing Requirement Single Sex Court", requirementsSingleSexCourt)
            .put("Hearing Requirement In Camera Court", requirementsInCamera)
            .put("Hearing Requirement Other", requirementsOther)
            .put("oldHearingCentre", hearingCentreNameBefore)
            .put(HEARING_CENTRE_ADDRESS, hearingCentreAddress)
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }

    private Map<String, String> getPersonalisationMapWithBlankValues() {
        return ImmutableMap
            .<String, String>builder()
            //.put("Hyperlink to user’s case list", iaExUiFrontendUrl)
            .put("appealReferenceNumber", "")
            .put("ariaListingReference", "")
            .put("homeOfficeReferenceNumber", "")
            .put("appellantGivenNames", "")
            .put("appellantFamilyName", "")
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("Hearing Requirement Vulnerabilities", "")
            .put("Hearing Requirement Multimedia", "")
            .put("Hearing Requirement Single Sex Court", "")
            .put("Hearing Requirement In Camera Court", "")
            .put("Hearing Requirement Other", "")
            .put("oldHearingCentre", "")
            .put(HEARING_CENTRE_ADDRESS, "")
            .put("customerServicesTelephone", "")
            .put("customerServicesEmail", "")
            .build();
    }
}
