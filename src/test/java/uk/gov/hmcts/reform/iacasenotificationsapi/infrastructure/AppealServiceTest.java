package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre.TAYLOR_HOUSE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
class AppealServiceTest {
    private AppealService appealService;

    @BeforeEach
    void setUp() {
        appealService = new AppealService();
    }

    @Mock
    private AsylumCase asylumCase;

    static Stream<Arguments> getValuesForAppealListed() {
        return Stream.of(
            Arguments.of(Optional.of(TAYLOR_HOUSE), Optional.empty(), true),
            Arguments.of(Optional.of(TAYLOR_HOUSE), Optional.of(YES), true),
            Arguments.of(Optional.of(TAYLOR_HOUSE), Optional.of(NO), true),
            Arguments.of(Optional.empty(), Optional.empty(), false),
            Arguments.of(Optional.empty(), Optional.of(YES), true),
            Arguments.of(Optional.empty(), Optional.of(NO), false)
        );
    }

    @ParameterizedTest
    @MethodSource("getValuesForAppealListed")
    void isAppealListed_returns_correct_value(
        Optional<HearingCentre> hearingCentre,
        Optional<YesOrNo> isDecisionWithoutHearing,
        boolean expectedResult
    ) {
        // given
        // when
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(hearingCentre);
        when(asylumCase.read(IS_DECISION_WITHOUT_HEARING, YesOrNo.class))
                .thenReturn(isDecisionWithoutHearing);

        // then
        assertEquals(expectedResult, appealService.isAppealListed(asylumCase));
    }

    @Test
    void isAppellantInPersonJourney() {
        AppealService appealService = new AppealService();
        assertFalse(appealService.isAppellantInPersonJourney(asylumCase));

        JourneyType journeyType = JourneyType.AIP;
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(journeyType));
        assertTrue(appealService.isAppellantInPersonJourney(asylumCase));
    }
}
