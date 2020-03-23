package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class LegalRepresentativeChangeDirectionDueDatePersonalisationTest {

    @Mock Callback<AsylumCase> callback;
    @Mock AsylumCase asylumCase;

    @Mock EmailAddressFinder emailAddressFinder;
    @Mock PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";

    private String legalRepEmailAddress = "legalRep@example.com";

    private String hmctsReference = "hmctsReference";
    private String legalRepReference = "legalRepresentativeReference";
    private String homeOfficeReference = "homeOfficeReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private LegalRepresentativeChangeDirectionDueDatePersonalisation legalRepresentativeChangeDirectionDueDatePersonalisation;

    @Before
    public void setUp() {
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);

        legalRepresentativeChangeDirectionDueDatePersonalisation = new LegalRepresentativeChangeDirectionDueDatePersonalisation(
            templateId,
            personalisationProvider,
            emailAddressFinder
        );
    }

    @Test
    public void should_return_the_given_email_address_from_asylum_case() {
        assertEquals(Collections.singleton(legalRepEmailAddress), legalRepresentativeChangeDirectionDueDatePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_given_template_id() {
        assertEquals(templateId, legalRepresentativeChangeDirectionDueDatePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REP_CHANGE_DIRECTION_DUE_DATE", legalRepresentativeChangeDirectionDueDatePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForLegalRep());

        Map<String, String> personalisation = legalRepresentativeChangeDirectionDueDatePersonalisation.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_throw_exception_on_personalistaion_when_case_is_null() {
        assertThatThrownBy(() -> legalRepresentativeChangeDirectionDueDatePersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    private Map<String, String> getPersonalisationForLegalRep() {
        return ImmutableMap
            .<String, String>builder()
            .put("hmctsReference", hmctsReference)
            .put("legalRepReference", legalRepReference)
            .put("homeOfficeReference", homeOfficeReference)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .build();
    }
}
