package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
class PaPayLaterListingPersonalisationSmsTest {

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private RecipientsFinder recipientsFinder;

    @Mock
    private SystemDateProvider systemDateProvider;

    private PaPayLaterListingPersonalisationSms personalisation;

    private final Long caseId = 12345L;
    private final String templateId = "PaPayLaterListingTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final int daysAfterNotificationSent = 14;
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedDueDate = "14/04/2024";

    @BeforeEach
    void setup() {

        personalisation = new PaPayLaterListingPersonalisationSms(
                templateId,
                daysAfterNotificationSent,
                iaAipFrontendUrl,
                recipientsFinder,
                systemDateProvider
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(
                caseId + "_PA_PAY_LATER_LISTING_SMS",
                personalisation.getReferenceId(caseId)
        );
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));

        Map<String, String> personalisationMap =
                personalisation.getPersonalisation(callback);

        assertEquals(
                mockedAppealReferenceNumber,
                personalisationMap.get("appealReferenceNumber")
        );
    }
}
