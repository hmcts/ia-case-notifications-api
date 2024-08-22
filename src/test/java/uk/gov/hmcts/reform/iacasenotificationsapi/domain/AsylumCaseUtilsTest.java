package uk.gov.hmcts.reform.iacasenotificationsapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.AsylumCaseUtils.isIntegrated;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepEmailInternalOrLegalRepJourney;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepEmailInternalOrLegalRepJourneyNonMandatory;

@ExtendWith(MockitoExtension.class)
public class AsylumCaseUtilsTest {

    @Mock
    private AsylumCase asylumCase;

    @Test
    void should_read_isIntegrated_field() {
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.empty());
        assertFalse(isIntegrated(asylumCase));

        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(isIntegrated(asylumCase));

        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(isIntegrated(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES","NO"})
    void should_get_repEmail_for_internal_and_non_internal_cases_and_appellant_representation(YesOrNo appellantsRepresentation) {
        String legalRepEmail = "legal@rep.email";
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(appellantsRepresentation));
        if (appellantsRepresentation.equals(YES)) {
            when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmail));
        } else {
            when(asylumCase.read(LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.of(legalRepEmail));
        }
        assertEquals(legalRepEmail, getLegalRepEmailInternalOrLegalRepJourney(asylumCase));
        assertEquals(legalRepEmail, getLegalRepEmailInternalOrLegalRepJourneyNonMandatory(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = { "YES", "NO" })
    void should_throw_an_exception_when_getting_repEmail_for_internal_and_non_internal_cases_and_appellant_representation(YesOrNo appellantsRepresentation) {
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(appellantsRepresentation));
        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> getLegalRepEmailInternalOrLegalRepJourney(asylumCase)
        );
        if (appellantsRepresentation.equals(YES)) {
            assertEquals("legalRepresentativeEmailAddress is not present", thrown.getMessage());
        } else {
            assertEquals("legalRepEmail is not present", thrown.getMessage());
        }
    }
}
