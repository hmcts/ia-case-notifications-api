package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class CustomerServicesProvider {

    private final String customerServicesTelephone;
    private final String customerServicesEmail;
    private final String ftpaCustomerServicesEmail;

    public CustomerServicesProvider(
        @Value("${customerServices.telephoneNumber}") String customerServicesTelephone,
        @Value("${customerServices.emailAddress}") String customerServicesEmail,
        @Value("${customerServices.ftpaEmailAddress}") String ftpaCustomerServicesEmail
    ) {
        requireNonNull(customerServicesTelephone);
        requireNonNull(customerServicesEmail);
        requireNonNull(ftpaCustomerServicesEmail);

        this.customerServicesTelephone = customerServicesTelephone;
        this.customerServicesEmail = customerServicesEmail;
        this.ftpaCustomerServicesEmail = ftpaCustomerServicesEmail;
    }

    public Map<String, String> getCustomerServicesPersonalisation() {

        final Builder<String, String> customerServicesValues = ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail);

        return customerServicesValues.build();
    }


    public Map<String, String> getFtpaCustomerServicesPersonalisation() {

        final Builder<String, String> customerServicesValues = ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", ftpaCustomerServicesEmail);

        return customerServicesValues.build();
    }

    public String getCustomerServicesTelephone() {
        requireNonNull(customerServicesTelephone);
        return customerServicesTelephone;
    }

    public String getCustomerServicesEmail() {
        requireNonNull(customerServicesEmail);
        return customerServicesEmail;
    }
}
