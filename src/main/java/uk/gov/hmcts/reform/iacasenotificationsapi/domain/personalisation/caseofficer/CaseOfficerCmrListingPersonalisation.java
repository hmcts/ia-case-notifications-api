package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CMR_IS_REMOTE_HEARING;

@Service
public class CaseOfficerCmrListingPersonalisation implements EmailNotificationPersonalisation {

    private final String caseOfficerCmrListingTemplateId;
    private final DateTimeExtractor dateTimeExtractor;
    private final String iaExUiFrontendUrl;
    private final String caseOfficerRemoteCmrListingTemplateId;
    private final EmailAddressFinder emailAddressFinder;
    private final HearingDetailsFinder hearingDetailsFinder;

    public CaseOfficerCmrListingPersonalisation(
            @Value("${govnotify.template.listAssistHearing.caseListed.caseOfficer.email}") String caseOfficerCmrListingTemplateId,
            @Value("${govnotify.template.listAssistHearing.caseListed.remoteHearing.caseOfficer.email}") String caseOfficerRemoteCmrListingTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            HearingDetailsFinder hearingDetailsFinder, DateTimeExtractor dateTimeExtractor) {
        this.caseOfficerCmrListingTemplateId = caseOfficerCmrListingTemplateId;
        this.caseOfficerRemoteCmrListingTemplateId = caseOfficerRemoteCmrListingTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.dateTimeExtractor = dateTimeExtractor;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (asylumCase.read(CMR_IS_REMOTE_HEARING).orElse(YesOrNo.NO).equals(YesOrNo.YES)) {
            return caseOfficerRemoteCmrListingTemplateId;
        } else {
            return caseOfficerCmrListingTemplateId;

        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseCaseOfficerHearingCentreEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_LISTED_CASE_OFFICER";
    }

    //    need to confirm placeholders here
    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                .put("hearingCentreAddress", hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase))
                .build();
    }
}
