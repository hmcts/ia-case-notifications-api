package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
public class AdminOfficerChangeToHearingRequirementsPersonalisationTest {

    private final String templateId = "someTemplateId";
    private final String changeToHearingRequirementsAdminHearingCenterTemplateId = "anotherTemplateId";
    private final String changeToHearingRequirementsAdminOfficerEmailAddress = "adminofficer-change-to-hearing-requirements@example.com";

    @Mock
    AsylumCase asylumCase;
    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;
    @Mock
    private EmailAddressFinder emailAddressFinder;
    private AdminOfficerChangeToHearingRequirementsPersonalisation
        adminOfficerChangeToHearingRequirementsPersonalisation;

    @BeforeEach
    public void setup() {

        adminOfficerChangeToHearingRequirementsPersonalisation =
            new AdminOfficerChangeToHearingRequirementsPersonalisation(
                templateId,
                changeToHearingRequirementsAdminHearingCenterTemplateId,
                changeToHearingRequirementsAdminOfficerEmailAddress,
                adminOfficerPersonalisationProvider,
                emailAddressFinder
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerChangeToHearingRequirementsPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_template_id_when_stf24w() {
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertEquals(changeToHearingRequirementsAdminHearingCenterTemplateId, adminOfficerChangeToHearingRequirementsPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertEquals(adminOfficerChangeToHearingRequirementsPersonalisation.getRecipientsList(asylumCase), Collections.singleton(changeToHearingRequirementsAdminOfficerEmailAddress));
    }

    @Test
    void should_return_given_email_address_from_asylum_case_when_stf24w() {
        String adminHearingCenterEmailAddress = "admin-hearing-center@example.com";

        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(emailAddressFinder.getAdminHearingCenterEmailAddress(asylumCase)).thenReturn(adminHearingCenterEmailAddress);

        assertEquals(adminOfficerChangeToHearingRequirementsPersonalisation.getRecipientsList(asylumCase), Collections.singleton(adminHearingCenterEmailAddress));
    }

    @Test
    public void should_return_given_reference_id() {

        Long caseId = 12345L;
        assertEquals(caseId + "_CHANGE_TO_HEARING_REQUIREMENTS_ADMIN_OFFICER",
            adminOfficerChangeToHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> adminOfficerChangeToHearingRequirementsPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase))
            .thenReturn(ImmutableMap.<String, String>builder().build());
        initializePrefixes(adminOfficerChangeToHearingRequirementsPersonalisation);

        Map<String, String> personalisation =
            adminOfficerChangeToHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        verify(adminOfficerPersonalisationProvider).getChangeToHearingRequirementsPersonalisation(asylumCase);
        String nonAdaPrefix = "Immigration and Asylum appeal";
        String adaPrefix = "Accelerated detained appeal";
        assertThat(personalisation).containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? adaPrefix : nonAdaPrefix);
    }
}
