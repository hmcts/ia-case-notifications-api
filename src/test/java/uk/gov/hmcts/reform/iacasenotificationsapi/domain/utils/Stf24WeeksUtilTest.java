package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class Stf24WeeksUtilTest {

    @Mock
    private AsylumCase asylumCase;

    @Test
    void should_return_appellant_given_name_when_present() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("John"));

        assertEquals("John", Stf24WeeksUtil.getAppellantGivenName(asylumCase));
    }

    @Test
    void should_return_empty_string_when_appellant_given_name_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.empty());

        assertEquals("", Stf24WeeksUtil.getAppellantGivenName(asylumCase));
    }

    @Test
    void should_return_appellant_family_name_when_present() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Doe"));

        assertEquals("Doe", Stf24WeeksUtil.getAppellantFamilyName(asylumCase));
    }

    @Test
    void should_return_empty_string_when_appellant_family_name_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.empty());

        assertEquals("", Stf24WeeksUtil.getAppellantFamilyName(asylumCase));
    }

    @Test
    void should_return_legal_rep_reference_number_from_primary_field() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("REF123"));

        assertEquals("REF123", Stf24WeeksUtil.getLegalRepReferenceNo(asylumCase));
    }

    @Test
    void should_return_legal_rep_reference_number_from_fallback_field_when_primary_empty() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J, String.class))
                .thenReturn(Optional.of("PAPER456"));

        assertEquals("PAPER456", Stf24WeeksUtil.getLegalRepReferenceNo(asylumCase));
    }

    @Test
    void should_return_empty_string_when_both_legal_rep_reference_fields_empty() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J, String.class))
                .thenReturn(Optional.empty());

        assertEquals("", Stf24WeeksUtil.getLegalRepReferenceNo(asylumCase));
    }

    @Test
    void should_return_false_when_legal_rep_reference_number_present() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("REF123"));

        assertFalse(Stf24WeeksUtil.noLegalRepresentation(asylumCase));
    }

    @Test
    void should_return_true_when_legal_rep_reference_number_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J, String.class))
                .thenReturn(Optional.empty());

        assertTrue(Stf24WeeksUtil.noLegalRepresentation(asylumCase));
    }

    @Test
    void should_return_true_when_only_fallback_legal_rep_field_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J, String.class))
                .thenReturn(Optional.empty());

        assertTrue(Stf24WeeksUtil.noLegalRepresentation(asylumCase));
    }
}