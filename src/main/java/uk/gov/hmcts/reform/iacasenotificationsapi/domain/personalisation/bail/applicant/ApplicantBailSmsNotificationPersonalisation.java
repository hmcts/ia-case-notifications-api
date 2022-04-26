package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collections;
import java.util.Set;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BailSmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

public interface ApplicantBailSmsNotificationPersonalisation extends BailSmsNotificationPersonalisation {

    static final org.slf4j.Logger LOG = getLogger(GovNotifyNotificationSender.class);

    @Override
    default Set<String> getRecipientsList(BailCase bailCase) {
        final String applicantMobileNumber = bailCase
                .read(BailCaseFieldDefinition.APPLICANT_MOBILE_NUMBER, String.class).orElse(null);

        if (applicantMobileNumber == null) {
            LOG.info("Applicant does not have a mobile number");
        }

        return applicantMobileNumber != null
                ? Collections.singleton(applicantMobileNumber) : Collections.emptySet();
    }
}
