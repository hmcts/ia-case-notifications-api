package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixesDetained;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RespondentForceCaseProgressionPersonalisationTest {

    @Mock
    CustomerServicesProvider customerServicesProvider;

    private RespondentForceCaseProgressionPersonalisation personalisation;

    @BeforeEach
    public void setUp() {

        personalisation = new RespondentForceCaseProgressionPersonalisation(
            "templateId",
            "emailAddress",
            "https://manage-case.platform.hmcts.net/",
            customerServicesProvider);
    }


    @Test
    public void getTemplateId() {
        assertEquals("templateId", personalisation.getTemplateId());
    }

    @Test
    public void getRecipientsList() {
        assertTrue(personalisation.getRecipientsList(new AsylumCase()).contains("emailAddress"));
    }

    @Test
    public void getReferenceId() {
        assertEquals("1234_RESPONDENT_FORCE_CASE_PROGRESSION", personalisation.getReferenceId(1234L));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void getPersonalisation(YesOrNo isAda) {
        final ImmutableMap<String, String> customerServicesValues = ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", "555 555 555")
            .put("customerServicesEmail", "cust.services@example.com").build();
        when(customerServicesProvider.getCustomerServicesPersonalisation(any(AsylumCase.class))).thenReturn(customerServicesValues);

        AsylumCase asylumCase = writeTestAsylumCase(isAda);

        initializePrefixesDetained(personalisation);

        Map<String, String> actualPersonalisation = personalisation.getPersonalisation(asylumCase);

        assertEquals("RP/50001/2020", actualPersonalisation.get("appealReferenceNumber"));
        assertEquals("Lacy Dawson", actualPersonalisation.get("appellantGivenNames"));
        assertEquals("Venus Blevins", actualPersonalisation.get("appellantFamilyName"));
        assertEquals("A1234567", actualPersonalisation.get("homeOfficeReferenceNumber"));
        assertEquals("555 555 555", actualPersonalisation.get("customerServicesTelephone"));
        assertEquals("cust.services@example.com", actualPersonalisation.get("customerServicesEmail"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Detained Appeal - Immigration and Asylum appeal"
            : "Immigration and Asylum appeal", actualPersonalisation.get("subjectPrefix"));
    }

    private AsylumCase writeTestAsylumCase(YesOrNo isDetained) {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "RP/50001/2020");
        asylumCase.write(APPELLANT_GIVEN_NAMES, "Lacy Dawson");
        asylumCase.write(APPELLANT_FAMILY_NAME, "Venus Blevins");
        asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, "A1234567");
        asylumCase.write(APPELLANT_IN_DETENTION, isDetained);
        return asylumCase;
    }
}
