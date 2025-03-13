package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.TtlCcdObject;

public final class CommonUtils {
    private CommonUtils() {
        // private constructor to prevent sonar warning
    }

    public static String convertAsylumCaseFeeValue(String amountFromAsylumCase) {
        return StringUtils.isNotBlank(amountFromAsylumCase)
            ? new BigDecimal(String.valueOf(Double.parseDouble(amountFromAsylumCase) / 100))
            .setScale(2, RoundingMode.DOWN).toString()
            : "";
    }

    public static boolean notificationAlreadySentToday(AsylumCase asylumCase) {
        return false;
    }

    public static boolean bailNotificationAlreadySentToday(BailCase bailCase) {
        return false;
    }

    private static boolean isTtlNinetyDaysAhead(Optional<TtlCcdObject> ttlOpt) {
        if (ttlOpt.isPresent()) {
            String systemTtl = ttlOpt.get().getSystemTtl();
            LocalDate systemTtlDate = LocalDate.parse(systemTtl);
            return !LocalDate.now().plusDays(90).isAfter(systemTtlDate);
        }
        return false;
    }
}
