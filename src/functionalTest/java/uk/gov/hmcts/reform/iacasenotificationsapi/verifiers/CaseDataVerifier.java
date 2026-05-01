package uk.gov.hmcts.reform.iacasenotificationsapi.verifiers;

import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.util.MapFieldAssertor;

@Component
public class CaseDataVerifier implements Verifier {

    public void verify(
        String fileName,
        long testCaseId,
        Map<String, Object> scenario,
        Map<String, Object> expectedResponse,
        Map<String, Object> actualResponse
    ) {
        MapFieldAssertor.assertFields(expectedResponse, actualResponse, (fileName + ": "));
    }
}
