package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_LIST;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.LegalRepresentativeBailEmailNotificationPersonalisation;

@Service
public class LegalRepresentativeBailDirectionSentPersonalisation implements LegalRepresentativeBailEmailNotificationPersonalisation {

    private final String legalRepresentativeBailDirectionSentDirectRecipientPersonalisationTemplateId;
    private final String legalRepresentativeBailDirectionSentOtherPartiesPersonalisationTemplateId;

    public LegalRepresentativeBailDirectionSentPersonalisation(
        @NotNull(message = "legalRepresentativeBailDirectionSentPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.sendDirectionDirectRecipient.email}") String legalRepresentativeBailDirectionSentDirectRecipientPersonalisationTemplateId,
        @Value("${govnotify.bail.template.sendDirectionOtherParties.email}") String legalRepresentativeBailDirectionSentOtherPartiesPersonalisationTemplateId
    ) {
        this.legalRepresentativeBailDirectionSentDirectRecipientPersonalisationTemplateId = legalRepresentativeBailDirectionSentDirectRecipientPersonalisationTemplateId;
        this.legalRepresentativeBailDirectionSentOtherPartiesPersonalisationTemplateId = legalRepresentativeBailDirectionSentOtherPartiesPersonalisationTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_SENT_DIRECTION_LEGAL_REPRESENTATIVE";
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return isDirectRecipient(bailCase)
            ? legalRepresentativeBailDirectionSentDirectRecipientPersonalisationTemplateId
            : legalRepresentativeBailDirectionSentOtherPartiesPersonalisationTemplateId;
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
        return bailCase.read(SEND_DIRECTION_LIST, String.class).orElse("").equals("Legal Representative");
    }
}