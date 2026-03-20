package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
@Builder
@AllArgsConstructor
public class NonLegalRepDetails {
    private String idamId;
    private String emailAddress;
    private String givenNames;
    private String familyName;
    private String phoneNumber;
}
