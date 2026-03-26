package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;

@Slf4j
@Service
public class AppellantCmrHearingCancelledPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appellantCmrCancelledEmailTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;


    public AppellantCmrHearingCancelledPersonalisationEmail(
        @Value("${govnotify.template.cmrHearingCancelled.appellant.email}")
        String appellantCmrCancelledEmailTemplateId,
        RecipientsFinder recipientsFinder,
        DateTimeExtractor dateTimeExtractor,
        HearingDetailsFinder hearingDetailsFinder
    ) {
        this.appellantCmrCancelledEmailTemplateId = appellantCmrCancelledEmailTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        boolean isAip = asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(type -> type == AIP).orElse(false);

        return isAip ? recipientsFinder.findAll(asylumCase, NotificationType.EMAIL) : Collections.emptySet();
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appellantCmrCancelledEmailTemplateId;
    }

    @Override
    public String getTemplateId() {
        return appellantCmrCancelledEmailTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_HEARING_CANCELLED_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "asylumCase must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

        String oldHearingDate;
        String oldHearingTime;
        String oldHearingCentreAddress;
        if (caseDetailsBefore.isPresent()) {
            AsylumCase asylumCaseBefore = caseDetailsBefore.get().getCaseData();
            oldHearingDate = dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getCmrHearingDateTime(asylumCaseBefore));
            oldHearingTime = dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getCmrHearingDateTime(asylumCaseBefore));
            oldHearingCentreAddress = hearingDetailsFinder.getCmrHearingCentreAddress(asylumCaseBefore);
        } else {
            oldHearingDate = "";
            oldHearingTime = "";
            oldHearingCentreAddress = "";
        }

        log.info("Legal Representative Personalisation - Appeal Reference Number: {}, Legal Rep Reference Number: {}, Appellant Given Names: {}, Appellant Family Name: {}, Old Hearing Date: {}, Old Hearing Time: {}, Old Hearing Centre Address: {}",
                asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""),
                asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""),
                asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""),
                asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""),
                oldHearingDate,
                oldHearingTime,
                oldHearingCentreAddress
        );

        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("oldHearingDate", oldHearingDate)
                .put("oldHearingTime", oldHearingTime)
                .put("oldHearingCentreAddress", oldHearingCentreAddress)
                .build();
    }
}
