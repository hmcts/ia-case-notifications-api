package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Service
public class DirectionFinder {

    public Optional<Direction> findFirst(
        AsylumCase asylumCase,
        DirectionTag directionTag
    ) {
        Optional<List<IdValue<Direction>>> maybeDirections = asylumCase
                .read(AsylumCaseDefinition.DIRECTIONS);
        return
            maybeDirections
                .orElse(Collections.emptyList())
                .stream()
                .map(IdValue::getValue)
                .filter(direction -> direction.getTag() == directionTag)
                .findFirst();
    }

    public Optional<Direction> findFirst(
            AsylumCase asylumCase,
            DirectionTag directionTag,
            Parties parties
    ) {
        return asylumCase
                .<List<IdValue<Direction>>>read(AsylumCaseDefinition.DIRECTIONS)
                .orElse(Collections.emptyList())
                .stream()
                .map(IdValue::getValue)
                .filter(direction -> direction.getTag() == directionTag)
                .filter(direction -> direction.getParties() == parties)
                .findFirst();
    }
}
