package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.PersonalisationUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantRemoveRepresentationPersonalisationLetter implements LetterNotificationPersonalisation {

    private final String iaAipFrontendUrl;
    private final String iaAipPathToSelfRepresentation;
    private final String removeRepresentationAppellantLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantRemoveRepresentationPersonalisationLetter(
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${iaAipPathToSelfRepresentation}") String iaAipPathToSelfRepresentation,
        @Value("${govnotify.template.removeRepresentation.appellant.letter}") String removeRepresentationAppellantLetterTemplateId,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.iaAipPathToSelfRepresentation = iaAipPathToSelfRepresentation;
        this.removeRepresentationAppellantLetterTemplateId = removeRepresentationAppellantLetterTemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return removeRepresentationAppellantLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return Collections.singleton(getAppellantAddressAsList(asylumCase).stream()
            .map(item -> item.replaceAll("\\s", "")).collect(Collectors.joining("_")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_REPRESENTATION_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        requireNonNull(asylumCase, "asylumCase must not be null");

        String linkToPiPStartPage = iaAipFrontendUrl + iaAipPathToSelfRepresentation;

        List<String> appellantAddress = getAppellantAddressAsList(asylumCase);

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("appellantDateOfBirth", PersonalisationUtils.defaultDateFormat(asylumCase.read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, String.class).orElse("")))
            .put("ccdCaseId", PersonalisationUtils.formatCaseId(callback.getCaseDetails().getId()))
            .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("linkToPiPStartPage", linkToPiPStartPage);

        for (int i = 0; i < appellantAddress.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), appellantAddress.get(i));
        }

        PinInPostDetails pip = asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class).orElse(null);
        if (pip != null) {
            personalizationBuilder.put("securityCode", pip.getAccessCode());
            personalizationBuilder.put("validDate", PersonalisationUtils.defaultDateFormat(pip.getExpiryDate()));
        } else {
            personalizationBuilder.put("securityCode", "");
            personalizationBuilder.put("validDate", "");
        }

        return personalizationBuilder.build();
    }

    private List<String> getAppellantAddressAsList(final AsylumCase asylumCase) {
        AddressUk address = asylumCase
            .read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)
            .orElseThrow(() -> new IllegalStateException("appellantAddress is not present"));
        List<String> appellantAddressAsList = new ArrayList<>();
        appellantAddressAsList.add(address.getAddressLine1().orElseThrow(() -> new IllegalStateException("appellantAddress 1 is not present")));
        String addressLine2 = address.getAddressLine2().orElse(null);
        String addressLine3 = address.getAddressLine3().orElse(null);
        if (addressLine2 != null) {
            appellantAddressAsList.add(addressLine2);
        }
        if (addressLine3 != null) {
            appellantAddressAsList.add(addressLine3);
        }
        appellantAddressAsList.add(address.getPostTown().orElseThrow(() -> new IllegalStateException("appellantAddress postTown is not present")));
        appellantAddressAsList.add(address.getPostCode().orElseThrow(() -> new IllegalStateException("appellantAddress postCode is not present")));

        return appellantAddressAsList;
    }
}
