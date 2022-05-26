package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_LIST;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailDirection;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
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
        return isDirectRecipient(findLatestDirection(bailCase))
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
        return direction.map(BailDirection::getSendDirectionList).orElse("").equals("Home Office");
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