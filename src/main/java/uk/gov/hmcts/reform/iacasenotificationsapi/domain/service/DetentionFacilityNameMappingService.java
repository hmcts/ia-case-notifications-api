package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import org.springframework.stereotype.Service;

@Service
public class DetentionFacilityNameMappingService {
    private final DetentionFacilityNameMappingConfig detentionFacilityNameMappingConfig;

    public DetentionFacilityNameMappingService(DetentionFacilityNameMappingConfig detentionFacilityNameMappingConfig) {
        this.detentionFacilityNameMappingConfig = detentionFacilityNameMappingConfig;
    }

    public String getDetentionFacility(String detentionFacilityName) {
        if (detentionFacilityNameMappingConfig.getPrisonNamesMapping().containsKey(detentionFacilityName)) {
            return detentionFacilityNameMappingConfig.getPrisonNamesMapping().get(detentionFacilityName);
        } else {
            return detentionFacilityNameMappingConfig.getIrcNamesMapping().getOrDefault(detentionFacilityName, detentionFacilityName);
        }
    }
}
