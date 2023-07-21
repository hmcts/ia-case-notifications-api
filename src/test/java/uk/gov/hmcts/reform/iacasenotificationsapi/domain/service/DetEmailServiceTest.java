package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Mock
    AsylumCase asylumCase;
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
    private final Map<String, String> mockPrisonToExpectedEmailMap =
        ImmutableMap
            .<String, String>builder()
            .put("AskhamGrange", "det-prison-askham-grange@example.com")
            .put("EastSuttonPark", "det-prison-east-sutton-park@example.com")
            .put("EdinburghSaughton", "det-prison-edinburgh-saughton@example.com")
            .put("GrendonSpringHill", "det-prison-grendon-spring-hill@example.com")
            .put("LancasterFarms", "det-prison-lancaster-farms@example.com")
            .put("Perth", "det-prison-perth@example.com")
            .put("Wakefield", "det-prison-wakefield@example.com")
            .build();
    private DetEmailService detEmailService;
    private final String ircValue = "immigrationRemovalCentre";
    private final String prisonValue = "prison";

    @BeforeEach
    void setUp() {
        detEmailService = new DetEmailService(mockIrcToExpectedEmailMap, mockPrisonToExpectedEmailMap);
    }

    @Test
    void should_return_det_email_address_based_off_Irc_mapping() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(ircValue));
        for (Map.Entry<String, String> entry: mockIrcToExpectedEmailMap.entrySet()) {
            when(asylumCase.read(AsylumCaseDefinition.IRC_NAME, String.class)).thenReturn(Optional.of(entry.getKey()));

            assertEquals(entry.getValue(), detEmailService.getDetEmailAddress(asylumCase));
        }
    }

    @Test
    void should_return_det_email_address_based_off_prison_mapping() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(prisonValue));
        for (Map.Entry<String, String> entry: mockPrisonToExpectedEmailMap.entrySet()) {
            when(asylumCase.read(AsylumCaseDefinition.PRISON_NAME, String.class)).thenReturn(Optional.of(entry.getKey()));

            assertEquals(entry.getValue(), detEmailService.getDetEmailAddress(asylumCase));
        }
    }

    @Test
    void should_return_det_email_address_for_tinsley_house_after_formatting_string() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(ircValue));
        when(asylumCase.read(AsylumCaseDefinition.IRC_NAME, String.class)).thenReturn(Optional.of("Tinsley House"));

        assertThat(mockIrcToExpectedEmailMap.get("TinsleyHouse"))
            .isEqualTo(detEmailService.getDetEmailAddress(asylumCase));
    }

    @Test
    void should_return_det_email_address_for_edinburgh_saughton_after_formatting_string() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(prisonValue));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of("Edinburgh, Saughton"));

        assertThat(mockPrisonToExpectedEmailMap.get("EdinburghSaughton"))
            .isEqualTo(detEmailService.getDetEmailAddress(asylumCase));
    }

    @Test
    public void should_throw_exception_on_email_address_when_Irc_name_is_empty() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(ircValue));
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> detEmailService.getDetEmailAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("IRC name is not present");
    }

    @Test
    public void should_throw_exception_on_email_address_when_prison_name_is_empty() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(prisonValue));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> detEmailService.getDetEmailAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Prison name is not present");
    }

    @Test
    public void should_throw_exception_on_email_address_when_Irc_name_is_not_mapped() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(ircValue));
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Larne House"));

        assertThatThrownBy(() -> detEmailService.getDetEmailAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("DET email address not found for: Larne House");
    }

    @Test
    public void should_throw_exception_on_email_address_when_Prison_name_is_not_mapped() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(prisonValue));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of("Unknown Prison"));

        assertThatThrownBy(() -> detEmailService.getDetEmailAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("DET email address not found for: Unknown Prison");
    }
}