package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils;

import org.springframework.test.util.ReflectionTestUtils;

public class SubjectPrefixesInitializer {

    private SubjectPrefixesInitializer() {
        // for checkStyle
    }

    public static void initializePrefixes(Object testClass) {
        ReflectionTestUtils.setField(testClass, "adaPrefix", "Accelerated detained appeal");
        ReflectionTestUtils.setField(testClass, "nonAdaPrefix", "Immigration and Asylum appeal");
    }
}
