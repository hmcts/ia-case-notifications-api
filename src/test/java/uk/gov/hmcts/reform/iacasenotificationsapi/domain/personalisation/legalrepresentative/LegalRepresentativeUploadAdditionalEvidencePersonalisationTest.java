package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CHANGE_ORGANISATION_REQUEST_FIELD;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;

import com.google.common.collect.ImmutableMap;

import java.time.LocalDateTime;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class LegalRepresentativeUploadAdditionalEvidencePersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;

    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private String beforeListingTemplateId = "beforeListingTemplateId";
    private String afterListingTemplateId = "afterListingTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String legalRepEmailAddress = "legalRep@example.com";
    private String appealReferenceNumber = "hmctsReference";
    private String ariaListingReference = "someAriaListingReference";
    private String legalRepReference = "legalRepresentativeReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private LegalRepresentativeUploadAdditionalEvidencePersonalisation
        legalRepresentativeUploadAdditionalEvidencePersonalisation;

    @BeforeEach
    public void setUp() {

        legalRepresentativeUploadAdditionalEvidencePersonalisation =
            new LegalRepresentativeUploadAdditionalEvidencePersonalisation(
                beforeListingTemplateId,
                afterListingTemplateId,
                iaExUiFrontendUrl,
                personalisationProvider,
                customerServicesProvider
            );
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
                .thenReturn(Optional.of(legalRepEmailAddress));

        assertTrue(legalRepresentativeUploadAdditionalEvidencePersonalisation.getRecipientsList(asylumCase)
                .contains(legalRepEmailAddress));
    }

    @Test
    public void should_return_empty_recipients_from_asylum_case_for_aip_journey() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(AIP));

        assert(legalRepresentativeUploadAdditionalEvidencePersonalisation.getRecipientsList(asylumCase)).isEmpty();
    }

    @Test
    public void should_return_given_email_address_for_change_org_request_field_and_field() {
        Value caseRole =
                new Value("[LEGALREPRESENTATIVE]", "Legal Representative");
        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class))
                .thenReturn(Optional.of(
                        new ChangeOrganisationRequest(
                                new DynamicList(caseRole, newArrayList(caseRole)),
                                LocalDateTime.now().toString(),
                                "1"
                        )
                ));

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
                .thenReturn(Optional.of(legalRepEmailAddress));

        assertTrue(legalRepresentativeUploadAdditionalEvidencePersonalisation.getRecipientsList(asylumCase)
                .contains(legalRepEmailAddress));
    }

    @Test
    public void should_return_given_email_address_for_empty_change_org_request_field_and_field() {
        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class))
                .thenReturn(Optional.of(new ChangeOrganisationRequest(null, null, null)));

        assertTrue(legalRepresentativeUploadAdditionalEvidencePersonalisation.getRecipientsList(asylumCase).isEmpty());
    }

    @Test
    public void should_return_the_given_template_id() {
        assertEquals(beforeListingTemplateId,
            legalRepresentativeUploadAdditionalEvidencePersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        assertEquals(afterListingTemplateId,
            legalRepresentativeUploadAdditionalEvidencePersonalisation.getTemplateId(asylumCase));

    }

    @Test
    public void should_return_given_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForLegalRep());
        Map<String, String> personalisation =
            legalRepresentativeUploadAdditionalEvidencePersonalisation.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_throw_exception_on_personalistaion_when_case_is_null() {
        assertThatThrownBy(() -> legalRepresentativeUploadAdditionalEvidencePersonalisation
            .getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    private Map<String, String> getPersonalisationForLegalRep() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("legalRepReference", legalRepReference)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }

    @Test
    public void should_return_false_if_appeal_not_yet_listed() {
        assertFalse(legalRepresentativeUploadAdditionalEvidencePersonalisation.isAppealListed(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        assertTrue(legalRepresentativeUploadAdditionalEvidencePersonalisation.isAppealListed(asylumCase));
    }
}
