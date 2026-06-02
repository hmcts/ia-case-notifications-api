package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isInternalCase;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;


@Service
public class CustomerServicesProvider {

    private final String customerServicesTelephone;
    private final String standardCustomerServicesEmail;
    private final String internalCaseCustomerServicesEmail;
    private final String appealIaCustomerServicesEmail;

    public CustomerServicesProvider(
        @Value("${customerServices.telephoneNumber}") String customerServicesTelephone,
        @Value("${customerServices.emailAddress}") String standardCustomerServicesEmail,
        @Value("${customerServices.internalCaseEmailAddress}") String internalCaseCustomerServicesEmail,
        @Value("${customerServices.appealIaEmailAddress}") String appealIaCustomerServicesEmail
    ) {
        requireNonNull(customerServicesTelephone);
        requireNonNull(standardCustomerServicesEmail);
        requireNonNull(internalCaseCustomerServicesEmail);
        requireNonNull(appealIaCustomerServicesEmail);

        this.customerServicesTelephone = customerServicesTelephone;
        this.standardCustomerServicesEmail = standardCustomerServicesEmail;
        this.internalCaseCustomerServicesEmail = internalCaseCustomerServicesEmail;
        this.appealIaCustomerServicesEmail = appealIaCustomerServicesEmail;
    }

    private String getCorrectEmail(AsylumCase asylumCase) {
        return isInternalCase(asylumCase) && isAcceleratedDetainedAppeal(asylumCase)
            ? internalCaseCustomerServicesEmail
            : standardCustomerServicesEmail;
    }

    public Map<String, String> getCustomerServicesPersonalisation(AsylumCase asylumCase) {

        final Builder<String, String> customerServicesValues = ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", getCorrectEmail(asylumCase))
            .put("AppealIAEmail", appealIaCustomerServicesEmail);

        return customerServicesValues.build();
    }

    public Map<String, String> getCustomerServicesPersonalisation(Callback<AsylumCase> callback) {
        return getCustomerServicesPersonalisation(callback.getCaseDetails().getCaseData());
    }
}
