package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class AppealTypeTest {

    @Test
    public void has_correct_asylum_appeal_types() {
        assertEquals(AppealType.RP, AppealType.from("revocationOfProtection").get());
        assertEquals(AppealType.PA, AppealType.from("protection").get());
        assertEquals(AppealType.EA, AppealType.from("refusalOfEu").get());
        assertEquals(AppealType.HU, AppealType.from("refusalOfHumanRights").get());
        assertEquals(AppealType.DC, AppealType.from("deprivation").get());
        assertEquals(AppealType.EU, AppealType.from("euSettlementScheme").get());
        assertEquals(AppealType.AG, AppealType.from("ageAssessment").get());
    }

    @Test
    public void returns_optional_for_unknown_appeal_type() {
        assertEquals(AppealType.from("some_unknown_type"), Optional.empty());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(7, AppealType.values().length);
    }
}
