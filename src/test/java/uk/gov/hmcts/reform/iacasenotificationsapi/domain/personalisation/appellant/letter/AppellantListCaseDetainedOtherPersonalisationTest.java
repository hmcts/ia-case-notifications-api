package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantListCaseDetainedOtherPersonalisationTest {

    private static final String TEMPLATE_ID = "templateId123";
    private static final Long CASE_ID = 12345L;

    @Mock
    private HearingDetailsFinder hearingDetailsFinder;
    @Mock
    private DateTimeExtractor dateTimeExtractor;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private AppellantListCaseDetainedOtherPersonalisation personalisation;

    @BeforeEach
    void setUp() {
        personalisation = new AppellantListCaseDetainedOtherPersonalisation(
                TEMPLATE_ID,
                hearingDetailsFinder,
                dateTimeExtractor,
                customerServicesProvider
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_return_template_id() {
        assertThat(personalisation.getTemplateId()).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void should_return_reference_id() {
        assertThat(personalisation.getReferenceId(CASE_ID))
                .isEqualTo(CASE_ID + "_INTERNAL_SUBMIT_APPEAL_WITH_FEE_OUT_OF_TIME_APPELLANT_LETTER");
    }

    @Test
    void should_return_recipients_list() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class))
                .thenReturn(Optional.of(new AddressUk("10", "Main St", "", "Birmingham", "", "BM3 5TF", "UK")));

        Set<String> recipients = personalisation.getRecipientsList(asylumCase);

        assertThat(recipients).containsExactly("10_MainSt__Birmingham_BM35TF");
    }

    @Test
    void should_throw_exception_when_callback_is_null() {
        Callback<AsylumCase> callback = null;
        assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation(callback));
    }

    @Test
    void should_return_all_personalisation_values() {
        mockCustomerServices();
        mockCaseData();
        mockHearingDetails();
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class))
                .thenReturn(Optional.of(new AddressUk("10", "Main St", "", "Birmingham", "", "BM3 5TF", "UK")));

        Map<String, String> result = personalisation.getPersonalisation(callback);

        assertThat(result)
                .containsEntry("appealReferenceNumber", "REF123")
                .containsEntry("homeOfficeReferenceNumber", "HO123")
                .containsEntry("appellantGivenNames", "John")
                .containsEntry("appellantFamilyName", "Doe")
                .containsEntry("hearingDate", "01-01-2025")
                .containsEntry("hearingTime", "10:00 AM")
                .containsEntry("hearingChannel", "video")
                .containsEntry("hearingLocation", "Taylor House")
                .containsEntry("address_line_1", "10")
                .containsEntry("address_line_2", "Main St")
                .containsKey("customerServiceName"); // from mockCustomerServices
    }

    private void mockCustomerServices() {
        when(customerServicesProvider.getCustomerServicesPersonalisation())
                .thenReturn(ImmutableMap.of("customerServiceName", "HMCTS", "customerServiceEmail", "email@test.com"));
    }

    private void mockCaseData() {
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("REF123"));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("HO123"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("John"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Doe"));
    }

    private void mockHearingDetails() {
        LocalDateTime hearingDateTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn("01-01-2025T10:00 AM");
        when(dateTimeExtractor.extractHearingDate(anyString())).thenReturn("01-01-2025");
        when(dateTimeExtractor.extractHearingTime(anyString())).thenReturn("10:00 AM");
        when(hearingDetailsFinder.getHearingChannel(asylumCase)).thenReturn("video");
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn("Taylor House");
    }
}
