package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_LIST;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BailEmailNotificationPersonalisation;

@Service
public class HomeOfficeBailDirectionSentPersonalisation implements BailEmailNotificationPersonalisation {

    private final String homeOfficeBailDirectionSentDirectRecipientPersonalisationTemplateId;
    private final String homeOfficeBailDirectionSentOtherPartiesPersonalisationTemplateId;
    private final String bailHomeOfficeEmailAddress;

    public HomeOfficeBailDirectionSentPersonalisation(
        @NotNull(message = "homeOfficeBailApplicationSubmittedPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.sendDirectionDirectRecipient.email}") String homeOfficeBailDirectionSentDirectRecipientPersonalisationTemplateId,
        @Value("${govnotify.bail.template.sendDirectionOtherParties.email}") String homeOfficeBailDirectionSentOtherPartiesPersonalisationTemplateId,
        @Value("${bailHomeOfficeEmailAddress}") String bailHomeOfficeEmailAddress
    ) {
        this.homeOfficeBailDirectionSentDirectRecipientPersonalisationTemplateId = homeOfficeBailDirectionSentDirectRecipientPersonalisationTemplateId;
        this.homeOfficeBailDirectionSentOtherPartiesPersonalisationTemplateId = homeOfficeBailDirectionSentOtherPartiesPersonalisationTemplateId;
        this.bailHomeOfficeEmailAddress = bailHomeOfficeEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_SENT_DIRECTION_HOME_OFFICE";
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return isDirectRecipient(bailCase)
            ? homeOfficeBailDirectionSentDirectRecipientPersonalisationTemplateId
            : homeOfficeBailDirectionSentOtherPartiesPersonalisationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(bailHomeOfficeEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return isDirectRecipient(bailCase)
            ? ImmutableMap
            .<String, String>builder()
            .put("sendDirectionDescription", bailCase.read(BailCaseFieldDefinition.SEND_DIRECTION_DESCRIPTION, String.class).orElse(""))
            .put("dateOfCompliance", bailCase.read(BailCaseFieldDefinition.DATE_OF_COMPLIANCE, String.class).orElse(""))
            .build()
            : ImmutableMap
            .<String, String>builder()
            .put("sendDirectionDescription", bailCase.read(BailCaseFieldDefinition.SEND_DIRECTION_DESCRIPTION, String.class).orElse(""))
            .put("dateOfCompliance", bailCase.read(BailCaseFieldDefinition.DATE_OF_COMPLIANCE, String.class).orElse(""))
            .put("party", bailCase.read(SEND_DIRECTION_LIST, String.class).orElse(""))
            .build();
    }

    private boolean isDirectRecipient(BailCase bailCase) {
        return bailCase.read(SEND_DIRECTION_LIST, String.class).orElse("").equals("Home Office");
    }
}