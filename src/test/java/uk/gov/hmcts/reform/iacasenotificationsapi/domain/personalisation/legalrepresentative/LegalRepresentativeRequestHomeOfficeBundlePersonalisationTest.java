package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeRequestHomeOfficeBundlePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    DirectionFinder directionFinder;
    @Mock
    Direction direction;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String directionDueDate = "2019-09-10";
    private String expectedDirectionDueDate = "10 Oct 2019";

    private String legalRepEmailAddress = "legalrep@example.com";

    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private LegalRepresentativeRequestHomeOfficeBundlePersonalisation
        legalRepresentativeRequestHomeOfficeBundlePersonalisation;

    @BeforeEach
    public void setUp() {

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
                templateId,
                directionFinder
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeRequestHomeOfficeBundlePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
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

        assertThatThrownBy(
            () -> legalRepresentativeRequestHomeOfficeBundlePersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeRequestHomeOfficeBundlePersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("legalRepReferenceNumber", legalRepRefNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("insertDate", "10 Sep 2019")
                .build();

        Map<String, String> actualPersonalisation =
            legalRepresentativeRequestHomeOfficeBundlePersonalisation.getPersonalisation(asylumCase);

        assertEquals(expectedPersonalisation.get("appealReferenceNumber"), actualPersonalisation.get("appealReferenceNumber"));
        assertEquals(expectedPersonalisation.get("legalRepReferenceNumber"), actualPersonalisation.get("legalRepReferenceNumber"));
        assertEquals(expectedPersonalisation.get("appellantGivenNames"), actualPersonalisation.get("appellantGivenNames"));
        assertEquals(expectedPersonalisation.get("appellantFamilyName"), actualPersonalisation.get("appellantFamilyName"));
        assertEquals(expectedPersonalisation.get("insertDate"), actualPersonalisation.get("insertDate"));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> legalRepresentativeRequestHomeOfficeBundlePersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("direction 'respondentEvidence' is not present");
    }
}
