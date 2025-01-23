package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;

@Service
public class LegalRepresentativeRequestHomeOfficeBundlePersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepEvidenceDirectionNonAdaTemplateId;
    private final String legalRepEvidenceDirectionAdaTemplateId;
    private final DirectionFinder directionFinder;
    private final StringProvider stringProvider;

    public LegalRepresentativeRequestHomeOfficeBundlePersonalisation(
            @Value("${govnotify.template.requestRespondentEvidenceDirection.legalRep.email.nonAda}") String legalRepEvidenceDirectionNonAdaTemplateId,
            @Value("${govnotify.template.requestRespondentEvidenceDirection.legalRep.email.ada}") String legalRepEvidenceDirectionAdaTemplateId,
            DirectionFinder directionFinder, StringProvider stringProvider) {

        this.legalRepEvidenceDirectionNonAdaTemplateId = legalRepEvidenceDirectionNonAdaTemplateId;
        this.legalRepEvidenceDirectionAdaTemplateId = legalRepEvidenceDirectionAdaTemplateId;
        this.directionFinder = directionFinder;
        this.stringProvider = stringProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase)
            ? legalRepEvidenceDirectionAdaTemplateId
            : legalRepEvidenceDirectionNonAdaTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_EVIDENCE_DIRECTION_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)
                .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.RESPONDENT_EVIDENCE + "' is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("HO Ref Number", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                        .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                        .put("insertDate", directionDueDate)
                        .put("HearingCentre", getHearingCentreName(asylumCase)).build();
    }

    private String getHearingCentreName(AsylumCase caseData) {

        HearingCentre hearingCentre = caseData.read(AsylumCaseDefinition.HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));
        String hearingCentreName = String.valueOf(hearingCentre);

        // Split camel case and capitalize the first letter of each word
        String[] words = hearingCentreName.replaceAll("([a-z])([A-Z])", "$1 $2").split(" ");
        StringBuilder formattedName = new StringBuilder();

        for (String word : words) {
            formattedName.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }

        // Trim the trailing space and return the result
        return formattedName.toString().trim();
    }



}
