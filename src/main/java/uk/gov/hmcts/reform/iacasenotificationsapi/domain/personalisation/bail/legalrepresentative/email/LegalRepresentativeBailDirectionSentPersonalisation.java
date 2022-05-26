package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_LIST;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import javax.validation.constraints.NotNull;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailDirection;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
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
        return isDirectRecipient(findLatestDirection(bailCase))
            ? legalRepresentativeBailDirectionSentDirectRecipientPersonalisationTemplateId
            : legalRepresentativeBailDirectionSentOtherPartiesPersonalisationTemplateId;
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        Optional<BailDirection> direction = findLatestDirection(bailCase);

        return isDirectRecipient(direction)
            ? ImmutableMap
            .<String, String>builder()
            .put("sendDirectionDescription", direction.map(BailDirection::getSendDirectionDescription).orElse(""))
            .put("dateOfCompliance", direction.map(BailDirection::getDateOfCompliance).orElse(""))
            .build()
            : ImmutableMap
            .<String, String>builder()
            .put("sendDirectionDescription", direction.map(BailDirection::getSendDirectionDescription).orElse(""))
            .put("dateOfCompliance", direction.map(BailDirection::getDateOfCompliance).orElse(""))
            .put("party", direction.map(BailDirection::getSendDirectionList).orElse(""))
            .build();
    }

    private boolean isDirectRecipient(Optional<BailDirection> direction) {
        return direction.map(BailDirection::getSendDirectionList).orElse("").equals("Legal Representative");
    }

    private Optional<BailDirection> findLatestDirection(BailCase bailCase) {
        Optional<List<IdValue<BailDirection>>> optionalDirections = bailCase.read(BailCaseFieldDefinition.DIRECTIONS);
        List<IdValue<BailDirection>> directions = optionalDirections.orElse(Collections.emptyList());

        if (!directions.isEmpty()) {
            return directions.stream()
                .max(Comparator.comparing(direction -> Integer.parseInt(direction.getId())))
                .map(IdValue::getValue);
        }
        return Optional.empty();
    }
}