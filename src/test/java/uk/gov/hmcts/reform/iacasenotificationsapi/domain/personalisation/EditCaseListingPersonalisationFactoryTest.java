package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;

@RunWith(MockitoJUnitRunner.class)
public class EditCaseListingPersonalisationFactoryTest {

    @Mock private AsylumCase asylumCase;
    @Mock private StringProvider stringProvider;

    final String appealReferenceNumber = "PA/001/2018";
    final String homeOfficeReferenceNumber = "SOMETHING";
    final String legalRepresentativeReferenceNumber = "myRef";
    final String appellantGivenNames = "Jane";
    final String appellantFamilyName = "Doe";
    final String ariaListingReference = "LP/12345/2019";
    final String listCaseHearingDate = "2019-05-03T14:25:15.000";
    final String extractedHearingDateFormatted = "3 May 2019";
    final String extractedHearingTime = "14:25";
    final String listCaseHearingCentreAddress = "IAC Taylor House, 88 Rosebery Avenue, London, EC1R 4QU";
    final String listCaseHearingCentre = "taylorHouse";
    final String listCaseRequirementsVulnerabilities = "anyVulnerabilities";
    final String listCaseRequirementsMultimedia = "anyMultimedia";
    final String listCaseRequirementsSingleSexCourt = "any";
    final String listCaseRequirementsInCameraCourt = "yes";
    final String listCaseRequirementsOther = "other";
    final String oldHearingCentre = "manchester";
    final String oldHearingCentreAddress = "IAC Manchester, 1st Floor Piccadilly Exchange, 2 Piccadilly Plaza, Mosley Street, Manchester, M1 4AH";
    final String oldHearingDate = "2019-04-10T10:10:10.000";

    private EditCaseListingPersonalisationFactory editCaseListingPersonalisationFactory;
    private DateTimeExtractor dateTimeExtractor;

    @Before
    public void setUp() {

        dateTimeExtractor = new DateTimeExtractor();
        editCaseListingPersonalisationFactory = new EditCaseListingPersonalisationFactory(stringProvider, dateTimeExtractor);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepresentativeReferenceNumber));
    }

    @Test
    public void should_create_personalisation_using_defaults_where_available() {

        final Map<String, String> expectedPersonalisation =
                ImmutableMap
                        .<String, String>builder()
                        .put("appealReferenceNumber", "")
                        .put("homeOfficeReferenceNumber", "")
                        .put("legalRepReferenceNumber", "")
                        .put("ariaListingReference", "")
                        .put("appellantGivenNames", "")
                        .put("appellantFamilyName", "")
                        .put("listCaseRequirementsVulnerabilities", "")
                        .put("listCaseRequirementsMultimedia", "")
                        .put("listCaseRequirementsSingleSexCourt", "")
                        .put("listCaseRequirementsInCameraCourt", "")
                        .put("listCaseRequirementsOther", "")
                        .build();

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> actualPersonalisation = editCaseListingPersonalisationFactory.create(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
    }

    @Test
    public void should_create_personalisation_for_edit_case_listing_notification_to_home_office() {

        final Map<String, String> expectedPersonalisation =
                ImmutableMap
                        .<String, String>builder()
                        .put("appealReferenceNumber", appealReferenceNumber)
                        .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                        .put("ariaListingReference", ariaListingReference)
                        .put("appellantGivenNames", appellantGivenNames)
                        .put("appellantFamilyName", appellantFamilyName)
                        .put("listCaseHearingDate", listCaseHearingDate)
                        .put("Hearing Date", extractedHearingDateFormatted)
                        .put("Hearing Time", extractedHearingTime)
                        .put("Hearing Centre Address", listCaseHearingCentreAddress)
                        .put("listCaseHearingCentre", listCaseHearingCentre)
                        .put("listCaseRequirementsVulnerabilities", listCaseRequirementsVulnerabilities)
                        .put("listCaseRequirementsMultimedia", listCaseRequirementsMultimedia)
                        .put("listCaseRequirementsSingleSexCourt", listCaseRequirementsSingleSexCourt)
                        .put("listCaseRequirementsInCameraCourt", listCaseRequirementsInCameraCourt)
                        .put("listCaseRequirementsOther", listCaseRequirementsOther)
                        .put("oldHearingCentreAddress", oldHearingCentreAddress)
                        .put("oldHearingDate", oldHearingDate)
                        .build();

        Map<String, String> actualPersonalisation = editCaseListingPersonalisationFactory.create(asylumCase);

        assertThat(expectedPersonalisation).isEqualToComparingOnlyGivenFields(actualPersonalisation);
    }

    @Test
    public void should_create_personalisation_for_edit_case_listing_notification_to_legal_representative() {

        final Map<String, String> expectedPersonalisation =
                ImmutableMap
                        .<String, String>builder()
                        .put("appealReferenceNumber", appealReferenceNumber)
                        .put("legalRepReferenceNumber", legalRepresentativeReferenceNumber)
                        .put("ariaListingReference", ariaListingReference)
                        .put("appellantGivenNames", appellantGivenNames)
                        .put("appellantFamilyName", appellantFamilyName)
                        .put("listCaseHearingDate", listCaseHearingDate)
                        .put("Hearing Date", extractedHearingDateFormatted)
                        .put("Hearing Time", extractedHearingTime)
                        .put("Hearing Centre Address", listCaseHearingCentreAddress)
                        .put("listCaseHearingCentre", listCaseHearingCentre)
                        .put("listCaseRequirementsVulnerabilities", listCaseRequirementsVulnerabilities)
                        .put("listCaseRequirementsMultimedia", listCaseRequirementsMultimedia)
                        .put("listCaseRequirementsSingleSexCourt", listCaseRequirementsSingleSexCourt)
                        .put("listCaseRequirementsInCameraCourt", listCaseRequirementsInCameraCourt)
                        .put("listCaseRequirementsOther", listCaseRequirementsOther)
                        .put("oldHearingCentre", oldHearingCentre)
                        .put("oldHearingDate", oldHearingDate)
                        .build();

        Map<String, String> actualPersonalisation = editCaseListingPersonalisationFactory.create(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
    }



}
