package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@Service
public class LegalRepresentativeHearingRequirementsPersonalisation implements EmailNotificationPersonalisation {


    private final String legalRepresentativeHearingRequirementsDirectionTemplateId;
    private final String iaCcdFrontendUrl;
    private final DirectionFinder directionFinder;

    public LegalRepresentativeHearingRequirementsPersonalisation(
        @Value("${govnotify.template.hearingRequirementsDirection.legalRep.email}") String legalRepresentativeHearingRequirementsDirectionTemplateId,
        @Value("${iaCcdFrontendUrl}") String iaCcdFrontendUrl,
        DirectionFinder directionFinder
    ) {
        requireNonNull(iaCcdFrontendUrl, "iaCcdFrontendUrl must not be null");

        this.legalRepresentativeHearingRequirementsDirectionTemplateId = legalRepresentativeHearingRequirementsDirectionTemplateId;
        this.iaCcdFrontendUrl = iaCcdFrontendUrl;
        this.directionFinder = directionFinder;
    }

    @Override
    public String getTemplateId() {
        return legalRepresentativeHearingRequirementsDirectionTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
            .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present")));

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS_DIRECTION";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS)
                .orElseThrow(() -> new IllegalStateException("legal representative hearing requirements direction is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("LR reference", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Given names", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
                .put("Explanation", direction.getExplanation())
                .put("due date", directionDueDate)
                .build();
    }
}
