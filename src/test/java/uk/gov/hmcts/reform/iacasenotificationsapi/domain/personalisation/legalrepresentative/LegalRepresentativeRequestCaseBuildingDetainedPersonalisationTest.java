package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import java.util.List;
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

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeRequestCaseBuildingDetainedPersonalisationTest {

    private final String iaExUiFrontendUrl = "http://localhost";
    @Mock
    AsylumCase asylumCase;
    @Mock
    DirectionFinder directionFinder;
    @Mock
    Direction direction;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    private final String templateId = "someTemplateId";
    private final String legalRepEmailAddress = "legalrep@example.com";

    private final String appealReferenceNumber = "someReferenceNumber";
    private final String legalRepRefNumber = "somelegalRepRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";

    private LegalRepresentativeRequestCaseBuildingDetainedPersonalisation legalRepresentativeRequestCaseBuildingDetainedPersonalisation;

    @BeforeEach
    public void setUp() {

        String directionDueDate = "2019-09-10";
        when((direction.getDateDue())).thenReturn(directionDueDate);
        String directionExplanation = "someExplanation";
        when((direction.getExplanation())).thenReturn(directionExplanation);
        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_CASE_BUILDING))
            .thenReturn(Optional.of(direction));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));

        String customerServicesTelephone = "555 555 555";
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        String customerServicesEmail = "customer.services@example.com";
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepresentativeRequestCaseBuildingDetainedPersonalisation =
            new LegalRepresentativeRequestCaseBuildingDetainedPersonalisation(
                templateId,
                iaExUiFrontendUrl,
                directionFinder,
                customerServicesProvider
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeRequestCaseBuildingDetainedPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_LEGAL_REPRESENTATIVE_DETAINED_REQUEST_CASE_BUILDING",
            legalRepresentativeRequestCaseBuildingDetainedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeRequestCaseBuildingDetainedPersonalisation.getRecipientsList(asylumCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
assertThrows(IllegalStateException.class, () -> legalRepresentativeRequestCaseBuildingDetainedPersonalisation.getRecipientsList(asylumCase))
            ;
assertEquals("legalRepresentativeEmailAddress is not present", exception.getMessage());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
assertThrows(NullPointerException.class,
            () -> legalRepresentativeRequestCaseBuildingDetainedPersonalisation.getPersonalisation((AsylumCase) null))
            ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(legalRepresentativeRequestCaseBuildingDetainedPersonalisation);

        String expectedDirectionDueDate = "10 Sep 2019";
        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("dueDate", expectedDirectionDueDate)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("legalRepReferenceNumber", legalRepRefNumber)
                .put("subjectPrefix", isAda.equals(YesOrNo.YES)
                    ? "Accelerated detained appeal"
                    : "Immigration and Asylum appeal")
                .build();

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> actualPersonalisation =
            legalRepresentativeRequestCaseBuildingDetainedPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).containsAllEntriesOf(expectedPersonalisation);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(legalRepresentativeRequestCaseBuildingDetainedPersonalisation);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> personalisation =
            legalRepresentativeRequestCaseBuildingDetainedPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).allSatisfy((key, value) -> {
            if (!List.of(
                "linkToOnlineService",
                "subjectPrefix",
                "dueDate"
            ).contains(key)) {
                assertThat(value).isEmpty();
            }
        });

    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_CASE_BUILDING)).thenReturn(Optional.empty());

        IllegalStateException exception =
assertThrows(IllegalStateException.class, () -> legalRepresentativeRequestCaseBuildingDetainedPersonalisation.getPersonalisation(asylumCase))
            ;
assertEquals("legal representative request case building is not present", exception.getMessage());
    }

}
