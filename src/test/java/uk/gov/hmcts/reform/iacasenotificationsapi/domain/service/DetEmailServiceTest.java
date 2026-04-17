package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DetEmailServiceTest {

    private final Map<String, String> mockIrcToExpectedEmailMap =
        ImmutableMap
            .<String, String>builder()
            .put("Brookhouse", "det-irc-brookhouse@example.com")
            .put("CampsfieldHouse", "det-irc-campsfieldhouse@example.com")
            .put("Colnbrook", "det-irc-colnbrook@example.com")
            .put("Derwentside", "det-irc-derwentside@example.com")
            .put("Dungavel", "det-irc-dungavel@example.com")
            .put("Harmondsworth", "det-irc-harmondsworth@example.com")
            .put("TinsleyHouse", "det-irc-tinsleyhouse@example.com")
            .put("Yarlswood", "det-irc-yarlswood@example.com")
            .build();
    private final String ircValue = "immigrationRemovalCentre";
    @Mock
    AsylumCase asylumCase;
    private DetEmailService detEmailService;

    @BeforeEach
    void setUp() {
        detEmailService = new DetEmailService(mockIrcToExpectedEmailMap);
    }

    @Test
    void should_return_det_email_address_based_off_Irc_mapping() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(ircValue));
        for (Map.Entry<String, String> entry : mockIrcToExpectedEmailMap.entrySet()) {
            when(asylumCase.read(AsylumCaseDefinition.IRC_NAME, String.class)).thenReturn(Optional.of(entry.getKey()));

            assertEquals(entry.getValue(), detEmailService.getDetEmailAddress(asylumCase));
        }
    }

    @Test
    void should_return_det_email_address_for_tinsley_house_after_formatting_string() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(ircValue));
        when(asylumCase.read(AsylumCaseDefinition.IRC_NAME, String.class)).thenReturn(Optional.of("Tinsley House"));

        assertEquals(detEmailService.getDetEmailAddress(asylumCase), mockIrcToExpectedEmailMap.get("TinsleyHouse"));
    }

    @Test
    void should_return_det_email_address_for_campsfield_house_after_formatting_string() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(ircValue));
        when(asylumCase.read(AsylumCaseDefinition.IRC_NAME, String.class)).thenReturn(Optional.of("Campsfield House"));

        assertEquals(detEmailService.getDetEmailAddress(asylumCase), mockIrcToExpectedEmailMap.get("CampsfieldHouse"));
    }

    @Test
    public void should_throw_exception_on_email_address_when_Irc_name_is_empty() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(ircValue));
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> detEmailService.getDetEmailAddress(asylumCase));
        assertEquals("IRC name is not present", exception.getMessage());
    }

    @Test
    public void should_throw_exception_on_email_address_when_Irc_name_is_not_mapped() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(ircValue));
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Larne House"));

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> detEmailService.getDetEmailAddress(asylumCase));
        assertEquals("DET email address not found for: Larne House", exception.getMessage());
    }

    @Test
    public void should_return_empty_string_if_prison_is_selected() {
        String prisonValue = "prison";
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(prisonValue));

        assertEquals("", detEmailService.getDetEmailAddress(asylumCase));
    }
}
