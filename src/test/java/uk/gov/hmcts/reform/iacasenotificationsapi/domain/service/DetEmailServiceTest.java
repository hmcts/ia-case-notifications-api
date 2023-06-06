package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
    private final Map<String, String> detEmailAddressMap =
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
        detEmailService = new DetEmailService(detEmailAddressMap);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Brookhouse", "Colnbrook", "Derwentside", "Dungavel", "Harmondsworth", "Tinsley House", "Yarlswood"})
    void should_return_det_email_address_based_off_Irc_mapping(String ircName) {

        when(asylumCase.read(AsylumCaseDefinition.IRC_NAME, String.class)).thenReturn(Optional.of(ircName));

        if (ircName.equals("Tinsley House")) {
            ircName = "TinsleyHouse";
        }
        String expectedDetEmailAddress = detEmailAddressMap.get(ircName);
        String actualDetEmailAddress = detEmailService.getDetEmailAddress(asylumCase);

        switch (ircName) {
            case "Brookhouse":
                assertEquals(expectedDetEmailAddress, actualDetEmailAddress);
            case "Colnbrook":
                assertEquals(expectedDetEmailAddress, actualDetEmailAddress);
            case "Derwentside":
                assertEquals(expectedDetEmailAddress, actualDetEmailAddress);
            case "Dungavel":
                assertEquals(expectedDetEmailAddress, actualDetEmailAddress);
            case "Harmondsworth":
                assertEquals(expectedDetEmailAddress, actualDetEmailAddress);
            case "Tinsley House":
                assertEquals(expectedDetEmailAddress, actualDetEmailAddress);
            case "Yarlswood":
                assertEquals(expectedDetEmailAddress, actualDetEmailAddress);
        }
    }
}