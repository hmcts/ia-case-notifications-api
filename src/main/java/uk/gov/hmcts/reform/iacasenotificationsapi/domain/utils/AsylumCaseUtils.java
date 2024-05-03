package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

public class AsylumCaseUtils {

    private AsylumCaseUtils() {
    }

    public static boolean isAppellantInDetention(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(NO).equals(YES);
    }

    public static boolean isInternalCase(AsylumCase asylumCase) {
        return asylumCase.read(IS_ADMIN, YesOrNo.class).map(isAdmin -> YES == isAdmin).orElse(false);
    }

    public static boolean isSubmissionOutOfTime(AsylumCase asylumCase) {
        return asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class).orElse(NO).equals(YES);
    }

    public static List<String> getAppellantAddressAsList(final AsylumCase asylumCase) {
        AddressUk address = asylumCase
            .read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)
            .orElseThrow(() -> new IllegalStateException("appellantAddress is not present"));

        List<String> appellantAddressAsList = new ArrayList<>();

        appellantAddressAsList.add(address.getAddressLine1().orElseThrow(() -> new IllegalStateException("appellantAddress line 1 is not present")));
        String addressLine2 = address.getAddressLine2().orElse(null);
        String addressLine3 = address.getAddressLine3().orElse(null);

        if (addressLine2 != null) {
            appellantAddressAsList.add(addressLine2);
        }
        if (addressLine3 != null) {
            appellantAddressAsList.add(addressLine3);
        }
        appellantAddressAsList.add(address.getPostTown().orElseThrow(() -> new IllegalStateException("appellantAddress postTown is not present")));
        appellantAddressAsList.add(address.getPostCode().orElseThrow(() -> new IllegalStateException("appellantAddress postCode is not present")));

        return appellantAddressAsList;
    }
}
