package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre.MANCHESTER;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

@SuppressWarnings("OperatorWrap")
public class AsylumCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void reads_string() throws IOException {

        String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<String> maybeAppealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER);

        assertThat(maybeAppealReferenceNumber.get()).isEqualTo("PA/50222/2019");
    }

    @Test
    public void reads_hearing_centre() throws IOException {

        String caseData = "{\"hearingCentre\": \"manchester\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<YesOrNo> maybeHearingCentre = asylumCase.read(HEARING_CENTRE);

        assertThat(maybeHearingCentre.get()).isEqualTo(MANCHESTER);
    }

    @Test
    public void reads_id_value_list() throws IOException {

        String caseData = "\"directions\": [\n" +
                "    {\n" +
                "      \"id\": \"2\",\n" +
                "      \"value\": {\n" +
                "        \"tag\": \"buildCase\",\n" +
                "        \"dateDue\": \"2019-06-13\",\n" +
                "        \"parties\": \"legalRepresentative\",\n" +
                "        \"dateSent\": \"2019-05-16\",\n" +
                "        \"explanation\": \"You must now build your case by uploading your appeal argument and evidence.\\n\\nAdvice on writing an appeal argument\\nYou must write a full argument that references:\\n- all the evidence you have or plan to rely on, including any witness statements\\n- the grounds and issues of the case\\n- any new matters\\n- any legal authorities you plan to rely on and why they are applicable to your case\\n\\nYour argument must explain why you believe the respondent's decision is wrong. You must provide all the information for the Home Office to conduct a thorough review of their decision at this stage.\\n\\nNext steps\\nOnce you have uploaded your appeal argument and all evidence, submit your case. The case officer will then review everything you've added. If your case looks ready, the case officer will send it to the respondent for their review. The respondent then has 14 days to respond.\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"1\",\n" +
                "      \"value\": {\n" +
                "        \"tag\": \"respondentEvidence\",\n" +
                "        \"dateDue\": \"2019-05-30\",\n" +
                "        \"parties\": \"respondent\",\n" +
                "        \"dateSent\": \"2019-05-16\",\n" +
                "        \"explanation\": \"A notice of appeal has been lodged against this asylum decision.\\n\\nYou must now send all documents to the case officer. The case officer will send them to the other party. You have 14 days to supply these documents.\\n\\nYou must include:\\n- the notice of decision\\n- any other document provided to the appellant giving reasons for that decision\\n- any statements of evidence\\n- the application form\\n- any record of interview with the appellant in relation to the decision being appealed\\n- any other unpublished documents on which you rely\\n- the notice of any other appealable decision made in relation to the appellant\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],";

        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<List<IdValue<Direction>>> maybeRespondentDocuments = asylumCase.read(DIRECTIONS);

        List<IdValue<Direction>> idValues = maybeRespondentDocuments.get();

        Direction direction = idValues.get(0).getValue();

        assertThat(idValues.get(0).getId()).isEqualTo("2");
        assertThat(direction.getTag()).isInstanceOf(String.class);
    }

    @Test
    public void reads_using_parameter_type_generics() throws IOException {

        String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get())
                .isEqualTo("PA/50222/2019");
    }

    @Test
    public void writes_simple_type() {

        AsylumCase asylumCase = new AsylumCase();

        asylumCase.write(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number");

        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get())
                .isEqualTo("some-appeal-reference-number");
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
                        DirectionTag.CASE_EDIT));


        asylumCase.write(DIRECTIONS, asList(idValue));


        Optional<List<IdValue<Direction>>> maybeDocuments = asylumCase.read(DIRECTIONS);

        IdValue<Direction> documents = maybeDocuments.get().get(0);


        assertThat(maybeDocuments.get().size()).isEqualTo(1);

        assertThat(documents.getId()).isEqualTo("some-id");

        assertThat(documents.getValue().getTag()).isEqualTo(DirectionTag.CASE_EDIT);

        assertThat(documents.getValue().getDateDue()).isEqualTo("some-date");

        assertThat(documents.getValue().getDateSent()).isEqualTo("some-other-date");

        assertThat(documents.getValue().getExplanation()).isEqualTo("some-explanation");
    }
}