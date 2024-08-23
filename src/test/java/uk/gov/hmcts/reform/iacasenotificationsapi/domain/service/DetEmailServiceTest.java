package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;

import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DetEmailServiceTest {

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private EmailAddressFinder emailAddressFinder;

    private final Map<String, String> mockIrcToExpectedEmailMap =
        ImmutableMap
            .<String, String>builder()
            .put("Brookhouse", "det-irc-brookhouse@example.com")
            .put("Colnbrook", "det-irc-colnbrook@example.com")
            .put("Derwentside", "det-irc-derwentside@example.com")
            .put("Dungavel", "det-irc-dungavel@example.com")
            .put("Harmondsworth", "det-irc-harmondsworth@example.com")
            .put("TinsleyHouse", "det-irc-tinsleyhouse@example.com")
            .put("Yarlswood", "det-irc-yarlswood@example.com")
            .build();

    private DetEmailService detEmailService;

    @BeforeEach
    void setUp() {
        detEmailService = new DetEmailService(mockIrcToExpectedEmailMap, emailAddressFinder);
        String ircValue = "immigrationRemovalCentre";
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(ircValue));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(emailAddressFinder.getLegalRepPaperJourneyEmailAddress(asylumCase)).thenReturn("legalRep@email.com");
    }

    @Test
    void should_return_det_email_address_based_off_Irc_mapping() {
        for (Map.Entry<String, String> entry: mockIrcToExpectedEmailMap.entrySet()) {
            when(asylumCase.read(AsylumCaseDefinition.IRC_NAME, String.class)).thenReturn(Optional.of(entry.getKey()));

            assertTrue(detEmailService.getRecipientsList(asylumCase).contains(entry.getValue()));
        }
    }

    @Test
    void should_return_det_email_address_for_tinsley_house_after_formatting_string() {
        when(asylumCase.read(AsylumCaseDefinition.IRC_NAME, String.class)).thenReturn(Optional.of("Tinsley House"));

        assertTrue(detEmailService.getRecipientsList(asylumCase).contains(mockIrcToExpectedEmailMap.get("TinsleyHouse")));
    }

    @Test
    void should_throw_exception_on_email_address_when_Irc_name_is_empty() {
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> detEmailService.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("IRC name is not present");
    }

    @Test
    void should_throw_exception_on_email_address_when_Irc_name_is_not_mapped() {
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Larne House"));

        assertThatThrownBy(() -> detEmailService.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("DET email address not found for: Larne House");
    }

    @Test
    void should_return_empty_string_if_prison_is_selected() {
        String prisonValue = "prison";
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(prisonValue));

        assertTrue(detEmailService.getRecipientsList(asylumCase).isEmpty());
    }

    @Test
    void should_return_empty_string_if_appellant_not_detained() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertTrue(detEmailService.getRecipientsList(asylumCase).isEmpty());
    }

    @Test
    void should_return_empty_string_if_detention_facility_is_not_presented() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        assertTrue(detEmailService.getRecipientsList(asylumCase).isEmpty());
    }

    @Test
    void should_return_empty_string_if_detention_facility_is_other() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        assertTrue(detEmailService.getRecipientsList(asylumCase).isEmpty());
    }

    @Test
    void should_return_legal_rep_email_if_internal_paper_journey_and_appeal_represented() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertTrue(detEmailService.getRecipientsList(asylumCase).contains("legalRep@email.com"));
    }
}