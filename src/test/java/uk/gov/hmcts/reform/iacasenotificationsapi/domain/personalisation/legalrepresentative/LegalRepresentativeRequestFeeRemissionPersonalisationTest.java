package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
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
    private String legalRepEmailAddress = "legalrep@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

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
        final Map<String, String> expectedPersonalisation =
                ImmutableMap
                        .<String, String>builder()
                        .put("appealReferenceNumber", appealReferenceNumber)
                        .put("appellantGivenNames", appellantGivenNames)
                        .put("appellantFamilyName", appellantFamilyName)
                        .put("legalRepReferenceNumber", legalRepRefNumber)
                        .put("linkToOnlineService", iaExUiFrontendUrl)
                        .build();
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        Map<String, String> personalisation =
            legalRepresentativeRequestFeeRemissionPersonalisation.getPersonalisation(asylumCase);

        assertThat(expectedPersonalisation).usingRecursiveComparison().isEqualTo(personalisation);

    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {
        final Map<String, String> expectedPersonalisation =
                ImmutableMap
                        .<String, String>builder()
                        .put("appealReferenceNumber", "")
                        .put("appellantGivenNames", "")
                        .put("appellantFamilyName", "")
                        .put("legalRepReferenceNumber", "")
                        .put("linkToOnlineService", iaExUiFrontendUrl)
                        .build();
        Map<String, String> personalisation =
            legalRepresentativeRequestFeeRemissionPersonalisation.getPersonalisation(asylumCase);

        assertThat(expectedPersonalisation).usingRecursiveComparison().isEqualTo(personalisation);
    }
}
