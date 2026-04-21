package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeRequestHomeOfficeBundlePersonalisationTest {

    private final String adaTemplateId = "adaTemplateId";
    private final String nonAdaTemplateId = "nonAdaTemplateId";
    private final String legalRepEmailAddress = "legalrep@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String legalRepRefNumber = "somelegalRepRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    @Mock
    AsylumCase asylumCase;
    @Mock
    DirectionFinder directionFinder;
    @Mock
    Direction direction;
    private LegalRepresentativeRequestHomeOfficeBundlePersonalisation
        legalRepresentativeRequestHomeOfficeBundlePersonalisation;

    @BeforeEach
    public void setUp() {

        String directionDueDate = "2019-09-10";
        when((direction.getDateDue())).thenReturn(directionDueDate);
        when(directionFinder.findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE))
            .thenReturn(Optional.of(direction));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));

        legalRepresentativeRequestHomeOfficeBundlePersonalisation =
            new LegalRepresentativeRequestHomeOfficeBundlePersonalisation(
                nonAdaTemplateId,
                adaTemplateId,
                directionFinder
            );
    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(nonAdaTemplateId, legalRepresentativeRequestHomeOfficeBundlePersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(adaTemplateId, legalRepresentativeRequestHomeOfficeBundlePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_EVIDENCE_DIRECTION_LEGAL_REPRESENTATIVE",
            legalRepresentativeRequestHomeOfficeBundlePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeRequestHomeOfficeBundlePersonalisation.getRecipientsList(asylumCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class,
                () -> legalRepresentativeRequestHomeOfficeBundlePersonalisation.getRecipientsList(asylumCase));
        assertEquals("legalRepresentativeEmailAddress is not present", exception.getMessage());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> legalRepresentativeRequestHomeOfficeBundlePersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        String expectedDirectionDueDate = "10 Sep 2019";
        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("legalRepReferenceNumber", legalRepRefNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("insertDate", expectedDirectionDueDate)
                .build();

        Map<String, String> actualPersonalisation =
            legalRepresentativeRequestHomeOfficeBundlePersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).containsAllEntriesOf(expectedPersonalisation);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class,
                () -> legalRepresentativeRequestHomeOfficeBundlePersonalisation.getPersonalisation(asylumCase));
        assertEquals("direction 'respondentEvidence' is not present", exception.getMessage());
    }
}
