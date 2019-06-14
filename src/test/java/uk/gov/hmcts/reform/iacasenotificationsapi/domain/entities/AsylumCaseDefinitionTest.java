package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.Test;

public class AsylumCaseDefinitionTest {

    @Test
    public void mapped_to_equivalent_field_name() {
        Stream.of(AsylumCaseDefinition.values())
                .forEach(v -> assertThat(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()))
                        .isEqualTo(v.value()));
    }

}