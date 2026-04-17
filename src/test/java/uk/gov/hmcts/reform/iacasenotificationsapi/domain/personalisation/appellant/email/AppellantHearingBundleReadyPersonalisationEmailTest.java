package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantHearingBundleReadyPersonalisationEmailTest {

    private final String templateId = "someEmailTemplateId";
    private final String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ccdReferenceNumber = "1234 5678 4321 8765";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    private AppellantHearingBundleReadyPersonalisationEmail
        appellantHearingBundleReadyPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class))
            .thenReturn(Optional.of(ccdReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        String customerServicesTelephone = "555 555 555";
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        String customerServicesEmail = "customer.services@example.com";
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);


        String iaAipFrontendUrl = "http://localhost";
        appellantHearingBundleReadyPersonalisationEmail =
            new AppellantHearingBundleReadyPersonalisationEmail(
                templateId,
                iaAipFrontendUrl,
                customerServicesProvider,
                recipientsFinder
            );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, appellantHearingBundleReadyPersonalisationEmail.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(12345L + "_HEARING_BUNDLE_IS_READY_APPELLANT_EMAIL",
            appellantHearingBundleReadyPersonalisationEmail.getReferenceId(12345L));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        String appellantEmailAddress = "appelant@example.net";
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(appellantEmailAddress));

        assertTrue(appellantHearingBundleReadyPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(appellantEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantHearingBundleReadyPersonalisationEmail.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_personalisation_when_all_information_given(YesOrNo isAda) {
        initializePrefixes(appellantHearingBundleReadyPersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> personalisation =
            appellantHearingBundleReadyPersonalisationEmail.getPersonalisation(asylumCase);
        assertThat(personalisation)
            .containsAllEntriesOf(customerServicesProvider.getCustomerServicesPersonalisation())
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("ccdReferenceNumber", ccdReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? "Accelerated detained appeal"
                : "Immigration and Asylum appeal");

    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {
        initializePrefixes(appellantHearingBundleReadyPersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantHearingBundleReadyPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsAllEntriesOf(customerServicesProvider.getCustomerServicesPersonalisation())
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("ccdReferenceNumber", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? "Accelerated detained appeal"
                : "Immigration and Asylum appeal");
    }
}