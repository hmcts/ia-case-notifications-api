package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IRC_NAME;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;

@ExtendWith(MockitoExtension.class)
public class DetEmailServiceTest {

    @Mock
    private AsylumCase asylumCase;
    private final String expectedDetBrookhouseEmailAddress = "det-irc-brookhouse@example.com";
    private final String expectedDetColnbrookEmailAddress = "det-irc-colnbrook@example.com";
    private final String expectedDetDerwentsideEmailAddress = "det-irc-derwentside@example.com";
    private final String expectedDetDungavelEmailAddress = "det-irc-dungavel@example.com";
    private final String expectedDetHarmondsworthEmailAddress = "det-irc-harmondsworth@example.com";
    private final String expectedDetTinsleyHouseEmailAddress = "det-irc-tinsleyhouse@example.com";
    private final String expectedDetYarlswoodEmailAddress = "det-irc-yarlswood@example.com";
    private final String expectedDetDefaultEmailAddress = "det-default@example.com";
    private DetEmailService detEmailService;

    @BeforeEach
    void setUp() {
        detEmailService = new DetEmailService(
            expectedDetDefaultEmailAddress, expectedDetBrookhouseEmailAddress,
            expectedDetColnbrookEmailAddress, expectedDetDerwentsideEmailAddress,
            expectedDetDungavelEmailAddress, expectedDetHarmondsworthEmailAddress,
            expectedDetTinsleyHouseEmailAddress, expectedDetYarlswoodEmailAddress
        );
    }

    @Test
    void should_map_det_email_address_for_Irc_Brookhouse() {

        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Brookhouse"));

        final String actualDetBrookhouseEmailAddress = detEmailService.getDetEmailAddress(asylumCase);
        assertEquals(expectedDetBrookhouseEmailAddress, actualDetBrookhouseEmailAddress);
    }

    @Test
    void should_map_det_email_address_for_Irc_Colnbrook() {

        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Colnbrook"));

        final String actualDetColnbrookEmailAddress = detEmailService.getDetEmailAddress(asylumCase);
        assertEquals(expectedDetColnbrookEmailAddress, actualDetColnbrookEmailAddress);
    }

    @Test
    void should_map_det_email_address_for_Irc_Derwentside() {

        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Derwentside"));

        final String actualDetDerwentsideEmailAddress = detEmailService.getDetEmailAddress(asylumCase);
        assertEquals(expectedDetDerwentsideEmailAddress, actualDetDerwentsideEmailAddress);
    }

    @Test
    void should_map_det_email_address_for_Irc_Dungavel() {

        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Dungavel"));

        final String actualDetDungavelEmailAddress = detEmailService.getDetEmailAddress(asylumCase);
        assertEquals(expectedDetDungavelEmailAddress, actualDetDungavelEmailAddress);
    }

    @Test
    void should_map_det_email_address_for_Irc_Harmondsworth() {

        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Harmondsworth"));

        final String actualDetHarmondsworthEmailAddress = detEmailService.getDetEmailAddress(asylumCase);
        assertEquals(expectedDetHarmondsworthEmailAddress, actualDetHarmondsworthEmailAddress);
    }

    @Test
    void should_map_det_email_address_for_Irc_TinsleyHouse() {

        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Tinsley House"));

        final String actualDetTinsleyHouseEmailAddress = detEmailService.getDetEmailAddress(asylumCase);
        assertEquals(expectedDetTinsleyHouseEmailAddress, actualDetTinsleyHouseEmailAddress);
    }

    @Test
    void should_map_det_email_address_for_Irc_Yarlswood() {

        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Yarlswood"));

        final String actualDetYarlswoodEmailAddress = detEmailService.getDetEmailAddress(asylumCase);
        assertEquals(expectedDetYarlswoodEmailAddress, actualDetYarlswoodEmailAddress);
    }

    @Test
    void should_map_default_det_email_address_for_no_Irc() {

        final String actualDetDefaultEmailAddress = detEmailService.getDetEmailAddress(asylumCase);
        assertEquals(expectedDetDefaultEmailAddress, actualDetDefaultEmailAddress);
    }

}
