package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

@Component
public class LegalRepRefundConfirmationPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String inCountryTemplateId;
    private final String oocTemplateId;
    private final String iaExUiFrontendUrl;

    public LegalRepRefundConfirmationPersonalisation(
        @Value("${govnotify.template.refundConfirmation.legalRep.inCountry.email}") String inCountryTemplateId,
        @Value("${govnotify.template.refundConfirmation.legalRep.ooc.email}") String oocTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl
    ) {
        this.inCountryTemplateId = inCountryTemplateId;
        this.oocTemplateId = oocTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        if (asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            return oocTemplateId;
        } else {
            return inCountryTemplateId;
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REFUND_CONFIRMATION_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            /*.put("feeAmount", convertAsylumCaseFeeValue(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class).orElse("")))
            .put("feeAmountRejected", convertAsylumCaseFeeValue(asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("")))
            .put("onlineCaseReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .put("deadline", asylumCase.read(REMISSION_REJECTED_DATE_PLUS_14DAYS, String.class).orElse(""))*/
            .build();
    }
}

