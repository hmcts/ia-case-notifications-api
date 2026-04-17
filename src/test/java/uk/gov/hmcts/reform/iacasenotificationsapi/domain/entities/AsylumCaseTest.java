package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre.MANCHESTER;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@SuppressWarnings("OperatorWrap")
public class AsylumCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void reads_string() throws IOException {

        String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<String> maybeAppealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER);

        assertEquals("PA/50222/2019", maybeAppealReferenceNumber.get());
    }

    @Test
    public void reads_hearing_centre() throws IOException {

        String caseData = "{\"hearingCentre\": \"manchester\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<HearingCentre> maybeHearingCentre = asylumCase.read(HEARING_CENTRE);

        assertEquals(MANCHESTER, maybeHearingCentre.get());
    }

    @Test
    public void reads_id_value_list() throws IOException {

        String caseData = """
            {"directions": [
                {
                  "id": "2",
                  "value": {
                    "tag": "buildCase",
                    "dateDue": "2019-06-13",
                    "parties": "legalRepresentative",
                    "dateSent": "2019-05-16",
                    "explanation": "some-explanation"
                  }
                },
                {
                  "id": "1",
                  "value": {
                    "tag": "respondentEvidence",
                    "dateDue": "2019-05-30",
                    "parties": "respondent",
                    "dateSent": "2019-05-16",
                    "explanation": "some-other-explanation"
                  }
                }
              ]}""";

        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<List<IdValue<Direction>>> maybeRespondentDocuments = asylumCase.read(DIRECTIONS);

        List<IdValue<Direction>> idValues = maybeRespondentDocuments.get();

        Direction direction1 = idValues.getFirst().getValue();

        assertEquals("2", idValues.getFirst().getId());
        assertEquals(DirectionTag.BUILD_CASE, direction1.getTag());
        assertEquals("2019-06-13", direction1.getDateDue());
        assertEquals(Parties.LEGAL_REPRESENTATIVE, direction1.getParties());
        assertEquals("2019-05-16", direction1.getDateSent());
        assertEquals("some-explanation", direction1.getExplanation());

        assertEquals("1", idValues.get(1).getId());
        Direction direction2 = idValues.get(1).getValue();
        assertEquals(DirectionTag.RESPONDENT_EVIDENCE, direction2.getTag());
        assertEquals("2019-05-30", direction2.getDateDue());
        assertEquals(Parties.RESPONDENT, direction2.getParties());
        assertEquals("2019-05-16", direction2.getDateSent());
        assertEquals("some-other-explanation", direction2.getExplanation());
    }

    @Test
    public void reads_using_parameter_type_generics() throws IOException {

        String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        assertEquals("PA/50222/2019", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get());
    }

    @Test
    public void writes_simple_type() {

        AsylumCase asylumCase = new AsylumCase();

        asylumCase.write(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number");

        assertEquals("some-appeal-reference-number", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get());
    }

    @Test
    public void writes_complex_type() {

        AsylumCase asylumCase = new AsylumCase();

        IdValue<Direction> idValue = new IdValue<>(
            "some-id",
            new Direction(
                "some-explanation",
                Parties.BOTH,
                "some-date",
                "some-other-date",
                DirectionTag.CASE_EDIT,
                Collections.emptyList(),
                Collections.emptyList(),
                UUID.randomUUID().toString(),
                "someDirectionType"
            ));


        asylumCase.write(DIRECTIONS, List.of(idValue));


        Optional<List<IdValue<Direction>>> maybeDocuments = asylumCase.read(DIRECTIONS);

        IdValue<Direction> documents = maybeDocuments.get().getFirst();


        assertEquals(1, maybeDocuments.get().size());

        assertEquals("some-id", documents.getId());

        assertEquals(DirectionTag.CASE_EDIT, documents.getValue().getTag());

        assertEquals("some-date", documents.getValue().getDateDue());

        assertEquals("some-other-date", documents.getValue().getDateSent());

        assertEquals("some-explanation", documents.getValue().getExplanation());
    }
}
