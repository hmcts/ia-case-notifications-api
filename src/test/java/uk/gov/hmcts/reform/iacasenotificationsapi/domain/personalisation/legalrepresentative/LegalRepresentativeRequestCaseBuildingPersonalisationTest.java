package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeRequestCaseBuildingPersonalisationTest {

    private final String iaExUiFrontendUrl = "http://localhost";
    @Mock
    AsylumCase asylumCase;
    @Mock
    DirectionFinder directionFinder;
    @Mock
    Direction direction;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    private final Long caseId = 12345L;
    private final String templateId = "someTemplateId";
    private final String detentionTemplateId = "someDetentionTemplateId";
    private final String directionDueDate = "2019-09-10";
    private final String expectedDirectionDueDate = "10 Oct 2019";
    private final String directionExplanation = "someExplanation";
    private final String legalRepEmailAddress = "legalrep@example.com";

    private final String appealReferenceNumber = "someReferenceNumber";
    private final String legalRepRefNumber = "somelegalRepRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";

    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer.services@example.com";

    private LegalRepresentativeRequestCaseBuildingPersonalisation legalRepresentativeRequestCaseBuildingPersonalisation;

    @BeforeEach
    public void setUp() {

        when((direction.getDateDue())).thenReturn(directionDueDate);
        when((direction.getExplanation())).thenReturn(directionExplanation);
        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_CASE_BUILDING))
            .thenReturn(Optional.of(direction));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepresentativeRequestCaseBuildingPersonalisation =
            new LegalRepresentativeRequestCaseBuildingPersonalisation(
                templateId,
                detentionTemplateId,
                iaExUiFrontendUrl,
                directionFinder,
                customerServicesProvider
            );
    }

    @Test
    public void should_return_given_template_id_for_non_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertEquals(templateId, legalRepresentativeRequestCaseBuildingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_template_id_for_missing_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.empty());

        assertEquals(templateId, legalRepresentativeRequestCaseBuildingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_template_id_for_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertEquals(detentionTemplateId, legalRepresentativeRequestCaseBuildingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REPRESENTATIVE_REQUEST_CASE_BUILDING",
            legalRepresentativeRequestCaseBuildingPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeRequestCaseBuildingPersonalisation.getRecipientsList(asylumCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeRequestCaseBuildingPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeRequestCaseBuildingPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(legalRepresentativeRequestCaseBuildingPersonalisation);

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantGivenNames)
                .put("directionExplanation", directionExplanation)
                .put("expectedDirectionDueDate", expectedDirectionDueDate)
                .put("iaExUiFrontendUrl", iaExUiFrontendUrl)
                .put("legalRepRefNumber", legalRepRefNumber)
                .put("subjectPrefix", isAda.equals(YesOrNo.YES)
                    ? "Accelerated detained appeal"
                    : "Immigration and Asylum appeal")
                .build();

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> actualPersonalisation =
            legalRepresentativeRequestCaseBuildingPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", actualPersonalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(legalRepresentativeRequestCaseBuildingPersonalisation);

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", "")
                .put("appellantGivenNames", "")
                .put("appellantFamilyName", "")
                .put("directionExplanation", "")
                .put("expectedDirectionDueDate", "")
                .put("legalRepRefNumber", "")
                .put("subjectPrefix", isAda.equals(YesOrNo.YES)
                    ? "Accelerated detained appeal"
                    : "Immigration and Asylum appeal")
                .build();

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> actualPersonalisation =
            legalRepresentativeRequestCaseBuildingPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", actualPersonalisation.get("subjectPrefix"));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_CASE_BUILDING)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeRequestCaseBuildingPersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legal representative request case building is not present");
    }

}
