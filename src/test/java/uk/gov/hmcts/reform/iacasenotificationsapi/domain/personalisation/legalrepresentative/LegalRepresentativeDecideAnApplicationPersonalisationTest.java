package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeDecideAnApplicationPersonalisationTest {

    private final String legalRepresentativeDecideAnApplicationGrantedAfterListingTemplateId =
        "grantedAfterListTemplateId";
    private final String legalRepresentativeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId =
        "grantedOtherPartyAfterListTemplateId";
    private final String legalRepresentativeDecideAnApplicationRefusedBeforeListingTemplateId =
        "refusedBeforeListTemplateId";
    private final String legalRepresentativeDecideAnApplicationRefusedAfterListingTemplateId =
        "refusedBeforeListTemplateId";
    private final String legalRepresentativeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId =
        "refusedBeforeListTemplateId";
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    MakeAnApplicationService makeAnApplicationService;
    @Mock
    MakeAnApplication makeAnApplication;
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String legalRepEmailAddress = "legalRep@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someReferenceNumber";
    private final String legalRepRefNumber = "somelegalRepRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";

    private LegalRepresentativeDecideAnApplicationPersonalisation legalRepresentativeDecideAnApplicationPersonalisation;

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
        when((makeAnApplicationService.getMakeAnApplication(asylumCase, true))).thenReturn(Optional.of(makeAnApplication));

        String legalRepresentativeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId = "refusedBeforeListTemplateId";
        String legalRepresentativeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId = "grantedOtherPartyBeforeListTemplateId";
        String legalRepresentativeDecideAnApplicationGrantedBeforeListingTemplateId = "grantedBeforeListTemplateId";
        legalRepresentativeDecideAnApplicationPersonalisation =
            new LegalRepresentativeDecideAnApplicationPersonalisation(
                legalRepresentativeDecideAnApplicationGrantedBeforeListingTemplateId,
                legalRepresentativeDecideAnApplicationGrantedAfterListingTemplateId,
                legalRepresentativeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId,
                legalRepresentativeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId,
                legalRepresentativeDecideAnApplicationRefusedBeforeListingTemplateId,
                legalRepresentativeDecideAnApplicationRefusedAfterListingTemplateId,
                legalRepresentativeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId,
                legalRepresentativeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId,
                iaExUiFrontendUrl,
                customerServicesProvider,
                makeAnApplicationService
            );
    }

    @Test
    public void should_return_given_template_id() {
        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(false);
        String legalRepUser = "caseworker-ia-legalrep-solicitor";
        when(makeAnApplication.getApplicantRole()).thenReturn(legalRepUser);
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");

        assertEquals(legalRepresentativeDecideAnApplicationGrantedAfterListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));
        when(makeAnApplication.getState()).thenReturn("listing");
        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(true);
        assertEquals(legalRepresentativeDecideAnApplicationGrantedAfterListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(false);
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        assertEquals(legalRepresentativeDecideAnApplicationRefusedBeforeListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(true);
        when(makeAnApplication.getState()).thenReturn("listing");
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        assertEquals(legalRepresentativeDecideAnApplicationRefusedAfterListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(false);
        String homeOfficeUser = "caseworker-ia-homeofficelart";
        when(makeAnApplication.getApplicantRole()).thenReturn(homeOfficeUser);
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");
        assertEquals(legalRepresentativeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(true);
        when(makeAnApplication.getState()).thenReturn("listing");
        assertEquals(legalRepresentativeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(false);
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");
        assertEquals(legalRepresentativeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(true);
        when(makeAnApplication.getState()).thenReturn("listing");
        assertEquals(legalRepresentativeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_DECIDE_AN_APPLICATION_LEGAL_REPRESENTATIVE",
            legalRepresentativeDecideAnApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeDecideAnApplicationPersonalisation.getRecipientsList(asylumCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> legalRepresentativeDecideAnApplicationPersonalisation.getRecipientsList(asylumCase));
        assertEquals("legalRepresentativeEmailAddress is not present", exception.getMessage());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> legalRepresentativeDecideAnApplicationPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeDecideAnApplicationPersonalisation);

        Map<String, String> personalisation =
            legalRepresentativeDecideAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertFalse(personalisation.isEmpty());
        assertThat(personalisation)
            .containsEntry("applicationType", "")
            .containsEntry("applicationDecisionReason", "No reason given")
            .containsEntry("decisionMaker", "")
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("legalRepReferenceNumber", legalRepRefNumber)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? "Accelerated detained appeal"
                : "Immigration and Asylum appeal");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeDecideAnApplicationPersonalisation);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        when(makeAnApplication.getDecisionReason()).thenReturn("No Reason Given");
        when(makeAnApplication.getDecisionMaker()).thenReturn("Judge");
        when(makeAnApplication.getType()).thenReturn("Other");

        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.of(makeAnApplication));

        Map<String, String> personalisation =
            legalRepresentativeDecideAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertFalse(personalisation.isEmpty());
        assertThat(personalisation)
            .containsEntry("applicationType", "Other")
            .containsEntry("applicationDecisionReason", "No Reason Given")
            .containsEntry("decisionMaker", "Judge")
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? "Accelerated detained appeal"
                : "Immigration and Asylum appeal");
        assertThat(personalisation).allSatisfy((key, value) -> {
            if (!List.of(
                "applicationType",
                "applicationDecisionReason",
                "decisionMaker",
                "linkToOnlineService",
                "subjectPrefix"
            ).contains(key)) {
                assertThat(value).isEmpty();
            }
        });
    }
}
