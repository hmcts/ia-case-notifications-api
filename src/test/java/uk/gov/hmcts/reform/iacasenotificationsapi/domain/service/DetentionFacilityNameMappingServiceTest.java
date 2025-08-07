package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionFacilityNameMappingServiceTest {

    @Mock
    private DetentionFacilityNameMappingConfig mockConfig;

    private DetentionFacilityNameMappingService service;

    private final Map<String, String> prisonNamesMapping = new HashMap<>();
    private final Map<String, String> ircNamesMapping = new HashMap<>();

    @BeforeEach
    void setUp() {
        when(mockConfig.getPrisonNamesMapping()).thenReturn(prisonNamesMapping);
        when(mockConfig.getIrcNamesMapping()).thenReturn(ircNamesMapping);

        service = new DetentionFacilityNameMappingService(mockConfig);
    }

    @Test
    void should_return_mapped_prison_name_when_in_prison_mapping() {
        prisonNamesMapping.put("HMP Altcourse", "Altcourse Prison");

        String result = service.getDetentionFacility("HMP Altcourse");

        assertEquals("Altcourse Prison", result);
    }

    @Test
    void should_return_mapped_irc_name_when_not_in_prison_but_in_irc_mapping() {
        ircNamesMapping.put("Harmondsworth IRC", "Harmondsworth Immigration Centre");

        String result = service.getDetentionFacility("Harmondsworth IRC");

        assertEquals("Harmondsworth Immigration Centre", result);
    }

    @Test
    void should_return_original_name_when_not_found_in_any_mapping() {
        String unknownName = "Unknown Facility";

        String result = service.getDetentionFacility(unknownName);

        assertEquals("Unknown Facility", result);
    }
}
