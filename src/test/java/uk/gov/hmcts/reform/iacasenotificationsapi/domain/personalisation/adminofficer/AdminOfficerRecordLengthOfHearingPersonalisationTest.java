package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AdminOfficerRecordLengthOfHearingPersonalisationTest {

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;

    private Long caseId = 12345L;
    private String templateId = "ftpaSubmittedTemplateId";
    private String adminOfficerEmailAddress = "adminOfficer@example.com";
    private String iaExUiFrontendUrl = "https://exuilinkrtofrontend";
    private String appealReferenceNumber = "appealReferenceNumber";
    private String ariaListingReference = "ariaListingReference";
    private String appellantGivenNames = "appellantGivenNames";
    private String appellantFamilyName = "appellantFamilyName";


    private AdminOfficerRecordLengthOfHearingPersonalisation adminOfficerRecordLengthOfHearingPersonalisation;

    @Before
    public void setUp() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        adminOfficerRecordLengthOfHearingPersonalisation = new AdminOfficerRecordLengthOfHearingPersonalisation(templateId, adminOfficerEmailAddress, iaExUiFrontendUrl);
    }

    @Test
    public void should_return_given_personalisation() {

        Map<String, String> expectedPersonalisation = adminOfficerRecordLengthOfHearingPersonalisation.getPersonalisation(callback);

        assertThat(expectedPersonalisation).isEqualToComparingOnlyGivenFields(getExpectedPersonalisation());
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        assertThatThrownBy(() -> adminOfficerRecordLengthOfHearingPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    public void should_return_given_reference_id() {

        assertThat(adminOfficerRecordLengthOfHearingPersonalisation.getReferenceId(caseId)).isEqualTo(caseId + "_RECORD_LENGTH_OF_HEARING_ADMIN_OFFICER");
    }

    @Test
    public void should_return_given_email_address() {

        assertThat(adminOfficerRecordLengthOfHearingPersonalisation.getRecipientsList(asylumCase)).isEqualTo(Collections.singleton(adminOfficerEmailAddress));
    }

    @Test
    public void should_return_given_template_id() {

        assertThat(adminOfficerRecordLengthOfHearingPersonalisation.getTemplateId()).isEqualTo(templateId);
    }

    private Map<String, String> getExpectedPersonalisation() {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("homeOfficeReference", iaExUiFrontendUrl)
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }
}
