package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

@Service
public class RespondentEvidenceDirectionPersonalisation implements NotificationPersonalisation {

    private final String respondentEvidenceDirectionTemplateId;
    private final String respondentEvidenceDirectionEmailAddress;
    private final StringProvider stringProvider;
    private final DirectionFinder directionFinder;

    public RespondentEvidenceDirectionPersonalisation(@Value("${govnotify.template.respondentEvidenceDirection}") String respondentEvidenceDirectionTemplateId,
                                                      @Value("${respondentEmailAddresses.respondentEvidenceDirection}") String respondentEvidenceDirectionEmailAddress,
                                                      StringProvider stringProvider,
                                                      DirectionFinder directionFinder) {

        this.respondentEvidenceDirectionTemplateId = respondentEvidenceDirectionTemplateId;
        this.respondentEvidenceDirectionEmailAddress = respondentEvidenceDirectionEmailAddress;
        this.stringProvider = stringProvider;
        this.directionFinder = directionFinder;
    }

    @Override
    public String getTemplateId() {
        return respondentEvidenceDirectionTemplateId;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {
        return respondentEvidenceDirectionEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_EVIDENCE_DIRECTION";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final HearingCentre hearingCentre =
            asylumCase
                .read(HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));

        final String hearingCentreForDisplay =
            stringProvider
                .get("hearingCentre", hearingCentre.toString())
                .orElseThrow(() -> new IllegalStateException("hearingCentre display string is not present"));

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)
                .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.RESPONDENT_EVIDENCE + "' is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
            .<String, String>builder()
            .put("HearingCentre", hearingCentreForDisplay)
            .put("Appeal Ref Number", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("HORef", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("Given names", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("Family name", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("Explanation", direction.getExplanation())
            .put("due date", directionDueDate)
            .build();
    }
}
