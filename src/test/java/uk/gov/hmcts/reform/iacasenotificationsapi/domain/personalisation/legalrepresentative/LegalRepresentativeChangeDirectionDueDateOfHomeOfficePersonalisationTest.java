package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class LegalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String afterListingTemplateId = "afterListingTemplateId";
    private String beforeListingTemplateId = "beforeListingTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String legalRepEmailAddress = "legalRep@example.com";
    private String hmctsReference = "hmctsReference";
    private String ariaListingReference = "someAriaListingReference";
    private String legalRepReference = "legalRepresentativeReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private LegalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation
        legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation;

    @BeforeEach
    public void setUp() {
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);

        legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation =
            new LegalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation(
                afterListingTemplateId,
                beforeListingTemplateId,
                iaExUiFrontendUrl,
                personalisationProvider,
                customerServicesProvider
            );
    }

    @Test
    public void should_return_the_given_before_listing_template_id() {
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE, State.class))
            .thenReturn(Optional.of(State.CASE_BUILDING));
        assertEquals(beforeListingTemplateId,
            legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_the_given_after_listing_template_id() {
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE, State.class))
            .thenReturn(Optional.of(State.FINAL_BUNDLING));
        assertEquals(afterListingTemplateId,
            legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_throw_exception_if_current_visible_state_to_legal_rep_is_not_present() {

        assertThatThrownBy(
            () -> legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("currentCaseStateVisibleToLegalRepresentative flag is not present");
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REP_CHANGE_DIRECTION_DUE_DATE_OF_HOME_OFFICE",
            legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_given_personalisation_when_all_information_given(YesOrNo isAda) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation);
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForLegalRep());

        Map<String, String> personalisation =
            legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation.getPersonalisation(callback);

        assertThat(personalisation).isNotEmpty();
        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_throw_exception_on_personalistaion_when_case_is_null() {
        assertThatThrownBy(() -> legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation
            .getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    private Map<String, String> getPersonalisationForLegalRep() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", hmctsReference)
            .put("ariaListingReference", ariaListingReference)
            .put("legalRepReference", legalRepReference)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }
}
