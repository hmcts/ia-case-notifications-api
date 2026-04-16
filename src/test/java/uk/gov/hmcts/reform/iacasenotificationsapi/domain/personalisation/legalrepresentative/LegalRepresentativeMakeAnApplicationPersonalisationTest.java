package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.List;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeMakeAnApplicationPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AppealService appealService;
    @Mock
    MakeAnApplicationService makeAnApplicationService;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    UserDetailsProvider userDetailsProvider;
    @Mock
    UserDetails userDetails;
    @Mock
    MakeAnApplication makeAnApplication;


    private final String makeAnApplicationLegalRepBeforeListingTemplateId = "beforeListTemplateId";
    private final String makeAnApplicationLegalRepAfterListingTemplateId = "afterListTemplateId";
    private final String makeAnApplicationLegalRepOtherPartyBeforeListingTemplateId = "otherPartyBeforeListTemplateId";
    private final String makeAnApplicationLegalRepOtherPartyAfterListingTemplateId = "otherPartyAfterListTemplateId";

    private final String iaExUiFrontendUrl = "http://localhost";
    private final String legalRepEmailAddress = "legalRep@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someReferenceNumber";
    private final String legalRepRefNumber = "somelegalRepRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String applicationType = "someApplicationType";

    private LegalRepresentativeMakeAnApplicationPersonalisation legalRepresentativeMakeAnApplicationPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));
        String customerServicesTelephone = "555 555 555";
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        String customerServicesEmail = "cust.services@example.com";
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, false)).thenReturn(Optional.of(makeAnApplication));
        when(makeAnApplication.getType()).thenReturn(applicationType);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        legalRepresentativeMakeAnApplicationPersonalisation = new LegalRepresentativeMakeAnApplicationPersonalisation(
            makeAnApplicationLegalRepBeforeListingTemplateId,
            makeAnApplicationLegalRepAfterListingTemplateId,
            makeAnApplicationLegalRepOtherPartyBeforeListingTemplateId,
            makeAnApplicationLegalRepOtherPartyAfterListingTemplateId,
            iaExUiFrontendUrl,
            customerServicesProvider,
            appealService,
            userDetailsProvider,
            makeAnApplicationService
        );
    }

    @Test
    public void should_return_empty_recipients_list_if_internal_case() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(legalRepresentativeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase).isEmpty());
    }

    @Test
    public void should_return_appropriate_recipients_list_if_not_internal_case() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.empty());
        assertTrue(legalRepresentativeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_return_given_template_id() {
        String legalRepUser = "caseworker-ia-legalrep-solicitor";
        when(userDetails.getRoles()).thenReturn(
            List.of(legalRepUser)
        );
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(makeAnApplicationLegalRepBeforeListingTemplateId,
            legalRepresentativeMakeAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(makeAnApplicationLegalRepAfterListingTemplateId,
            legalRepresentativeMakeAnApplicationPersonalisation.getTemplateId(asylumCase));

        String homeOfficeUser = "caseworker-ia-homeofficelart";
        when(userDetails.getRoles()).thenReturn(
            List.of(homeOfficeUser)
        );

        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(makeAnApplicationLegalRepOtherPartyBeforeListingTemplateId,
            legalRepresentativeMakeAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(makeAnApplicationLegalRepOtherPartyAfterListingTemplateId,
            legalRepresentativeMakeAnApplicationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_MAKE_AN_APPLICATION_LEGAL_REPRESENTATIVE",
            legalRepresentativeMakeAnApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> legalRepresentativeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase));
        assertEquals("legalRepresentativeEmailAddress is not present", exception.getMessage());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> legalRepresentativeMakeAnApplicationPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeMakeAnApplicationPersonalisation);
        Map<String, String> personalisation =
            legalRepresentativeMakeAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertFalse(personalisation.isEmpty());
        assertThat(personalisation)
            .containsAllEntriesOf(customerServicesProvider.getCustomerServicesPersonalisation())
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("applicationType", applicationType)
            .containsEntry("legalRepReferenceNumber", legalRepRefNumber)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? "Accelerated detained appeal"
                : "Immigration and Asylum appeal")
            .containsEntry("appellantFamilyName", appellantFamilyName);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeMakeAnApplicationPersonalisation);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(makeAnApplication.getType()).thenReturn("");

        Map<String, String> personalisation =
            legalRepresentativeMakeAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertFalse(personalisation.isEmpty());
        assertThat(personalisation)
            .containsAllEntriesOf(customerServicesProvider.getCustomerServicesPersonalisation())
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? "Accelerated detained appeal"
                : "Immigration and Asylum appeal");
        assertThat(personalisation).allSatisfy((key, value) -> {
            if (!List.of(
                "linkToOnlineService",
                "subjectPrefix"
            ).contains(key)) {
                assertThat(value).isEmpty();
            }
        });
    }
}
