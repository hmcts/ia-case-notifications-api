package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

@Service
public class AppellantInternalHomeOfficeApplyForFtpaNonDetainedAndOutOfCountryPersonalisation implements LetterNotificationPersonalisation {
    private final String appellantInternalHomeOfficeApplyForFtpaTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final DirectionFinder directionFinder;

    public AppellantInternalHomeOfficeApplyForFtpaNonDetainedAndOutOfCountryPersonalisation(
        @Value("${govnotify.template.ftpaSubmitted.nonDetainedAndOoc.homeOffice.letter}") String appellantInternalHomeOfficeApplyForFtpaTemplateId,
        CustomerServicesProvider customerServicesProvider,
        DirectionFinder directionFinder) {

        this.appellantInternalHomeOfficeApplyForFtpaTemplateId = appellantInternalHomeOfficeApplyForFtpaTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.directionFinder = directionFinder;

    }

    @Override
    public String getTemplateId() {
        return appellantInternalHomeOfficeApplyForFtpaTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return getAppellantAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_HO_APPLY_FOR_FTPA_NON_DETAINED_OR_OOC_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        List<String> appellantAddress =  inCountryAppeal(asylumCase) ?
            getAppellantAddressAsList(asylumCase) :
            getAppellantAddressAsListOoc(asylumCase);


        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""));

        for (int i = 0; i < appellantAddress.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return personalizationBuilder.build();
    }
}