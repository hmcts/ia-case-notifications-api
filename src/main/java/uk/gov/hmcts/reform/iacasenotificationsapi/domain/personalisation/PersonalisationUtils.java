package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PersonalisationUtils {

    private PersonalisationUtils() {
    }

    public static String defaultDateFormat(String dateString) {
        try {
            return LocalDate.parse(dateString)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        } catch (DateTimeParseException e) {
            return dateString;
        }
    }

    public static String formatCaseId(long caseId) {
        String caseIdStr = String.valueOf(caseId);

        if (caseIdStr.length() == 16) {
            caseIdStr = caseIdStr.substring(0,4) + '-' + caseIdStr.substring(4,8) + '-' + caseIdStr.substring(8,12) + '-' + caseIdStr.substring(12,16);
        }

        return caseIdStr;
    }
}
