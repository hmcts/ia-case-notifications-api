package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PRISON_NAME;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
class DetEmailServiceTest {

    @Mock
    private AsylumCase asylumCase;
    
    @Mock
    private PrisonEmailMappingService prisonEmailMappingService;

    private DetEmailService detEmailService;

    private Map<String, String> ircEmailMappings;

    @BeforeEach
    void setUp() {
        ircEmailMappings = ImmutableMap.<String, String>builder()
            .put("Brookhouse", "irc-brookhouse@example.com")
            .put("Colnbrook", "irc-colnbrook@example.com")
            .put("Harmondsworth", "irc-harmondsworth@example.com")
            .build();

        detEmailService = new DetEmailService(ircEmailMappings, prisonEmailMappingService);
    }

    @Test
    void shouldReturnIrcEmailAddressForIrcDetentionFacility() {
        // Given
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Brookhouse"));

        // When
        Optional<String> emailAddress = detEmailService.getDetEmailAddress(asylumCase);

        // Then
        assertThat(emailAddress).isPresent();
        assertThat(emailAddress.get()).isEqualTo("irc-brookhouse@example.com");
    }

    @Test
    void shouldReturnPrisonEmailAddressForPrisonDetentionFacility() {
        // Given
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of("Belmarsh"));
        when(prisonEmailMappingService.getPrisonEmail("Belmarsh")).thenReturn(Optional.of("prison-belmarsh@example.com"));

        // When
        Optional<String> emailAddress = detEmailService.getDetEmailAddress(asylumCase);

        // Then
        assertThat(emailAddress).isPresent();
        assertThat(emailAddress.get()).isEqualTo("prison-belmarsh@example.com");
    }

    @Test
    void shouldReturnEmptyForOtherDetentionFacility() {
        // Given
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        // When
        Optional<String> emailAddress = detEmailService.getDetEmailAddress(asylumCase);

        // Then
        assertThat(emailAddress).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNonDetainedAppeal() {
        // Given
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        // When
        Optional<String> emailAddress = detEmailService.getDetEmailAddress(asylumCase);

        // Then
        assertThat(emailAddress).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenDetentionFacilityNotPresent() {
        // Given
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        // When
        Optional<String> emailAddress = detEmailService.getDetEmailAddress(asylumCase);

        // Then
        assertThat(emailAddress).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenIrcNameNotPresent() {
        // Given
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> detEmailService.getDetEmailAddress(asylumCase))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("IRC name is not present");
    }

    @Test
    void shouldThrowExceptionWhenIrcNotFound() {
        // Given
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("NonexistentIrc"));

        // When & Then
        assertThatThrownBy(() -> detEmailService.getDetEmailAddress(asylumCase))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("DET email address not found for: NonexistentIrc");
    }

    @Test
    void shouldReturnEmptyWhenPrisonNameNotPresent() {
        // Given
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());

        // When
        Optional<String> emailAddress = detEmailService.getDetEmailAddress(asylumCase);

        // Then
        assertThat(emailAddress).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenPrisonNotFound() {
        // Given
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of("NonexistentPrison"));
        when(prisonEmailMappingService.getPrisonEmail("NonexistentPrison")).thenReturn(Optional.empty());

        // When
        Optional<String> emailAddress = detEmailService.getDetEmailAddress(asylumCase);

        // Then
        assertThat(emailAddress).isEmpty();
    }

    @Test
    void shouldReturnRecipientsListWithEmailAddress() {
        // Given
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Brookhouse"));

        // When
        Set<String> recipients = detEmailService.getRecipientsList(asylumCase);

        // Then
        assertThat(recipients).containsExactly("irc-brookhouse@example.com");
    }

    @Test
    void shouldReturnEmptyRecipientsListWhenNoEmailFound() {
        // Given
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        // When
        Set<String> recipients = detEmailService.getRecipientsList(asylumCase);

        // Then
        assertThat(recipients).isEmpty();
    }
}