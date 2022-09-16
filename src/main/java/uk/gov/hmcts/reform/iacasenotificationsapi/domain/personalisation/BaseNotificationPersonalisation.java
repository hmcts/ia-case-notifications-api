package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

public interface BaseNotificationPersonalisation {

    String getReferenceId(Long caseId);

    default String getTemplateId() {
        return null;
    }

    default String getTemplateId(AsylumCase asylumCase) {
        return null;
    }

    Set<String> getRecipientsList(AsylumCase asylumCase);

    default Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return Collections.emptyMap();
    }

    default Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        return getPersonalisation(callback.getCaseDetails().getCaseData());
    }

    default String defaultDateFormat(String dateString) {
        try {
            return LocalDate.parse(dateString).format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        } catch (DateTimeParseException e) {
            return dateString;
        }
    }
}
