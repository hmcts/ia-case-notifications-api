package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.*;

@Service
public class HomeOfficeEmailFinder {

    private static final String CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL_FLAG_IS_NOT_PRESENT =
        "currentCaseStateVisibleToHomeOfficeAll flag is not present";

    private static final Set<State> STATES_FOR_APC_EMAIL = Set.of(
        APPEAL_SUBMITTED,
        PENDING_PAYMENT,
        AWAITING_RESPONDENT_EVIDENCE,
        AWAITING_CLARIFYING_QUESTIONS_ANSWERS,
        CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED
    );

    private static final Set<State> STATES_FOR_LART_EMAIL = Set.of(
        CASE_BUILDING,
        CASE_UNDER_REVIEW,
        RESPONDENT_REVIEW,
        AWAITING_REASONS_FOR_APPEAL,
        REASONS_FOR_APPEAL_SUBMITTED
    );

    private static final Set<State> STATES_FOR_FTPA_EMAIL = Set.of(
        FTPA_SUBMITTED,
        FTPA_DECIDED
    );

    private static final Set<State> STATES_FOR_UNLISTED_APPEAL = Set.of(
        LISTING,
        SUBMIT_HEARING_REQUIREMENTS,
        ENDED,
        APPEAL_TAKEN_OFFLINE
    );

    private static final Set<State> STATES_FOR_LISTED_APPEAL = Set.of(
        LISTING,
        PREPARE_FOR_HEARING,
        FINAL_BUNDLING,
        PRE_HEARING,
        DECISION,
        ADJOURNED,
        DECIDED,
        ENDED,
        APPEAL_TAKEN_OFFLINE
    );

    private final AppealService appealService;
    private final EmailAddressFinder emailAddressFinder;
    private final String apcHomeOfficeEmailAddress;
    private final String lartHomeOfficeEmailAddress;

    public HomeOfficeEmailFinder(AppealService appealService,
        EmailAddressFinder emailAddressFinder,
        @Value("${apcHomeOfficeEmailAddress}") String apcHomeOfficeEmailAddress,
        @Value("${lartHomeOfficeEmailAddress}") String lartHomeOfficeEmailAddress) {
        this.appealService = appealService;
        this.emailAddressFinder = emailAddressFinder;
        this.apcHomeOfficeEmailAddress = apcHomeOfficeEmailAddress;
        this.lartHomeOfficeEmailAddress = lartHomeOfficeEmailAddress;
    }

    /**
     * Returns the Home Office email address(es) that should receive notifications
     * for the given asylum case, based on its current state and listing status.
     *
     * @param asylumCase the case to inspect
     * @return a singleton set with the appropriate recipient email
     * @throws IllegalStateException if the visible-to-Home-Office state flag is missing
     *                               or the state is not covered by any routing rule
     */
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        State currentState = asylumCase
            .read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)
            .orElseThrow(() -> new IllegalStateException(CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL_FLAG_IS_NOT_PRESENT));

        if (STATES_FOR_APC_EMAIL.contains(currentState)) {
            return Set.of(apcHomeOfficeEmailAddress);
        }

        if (STATES_FOR_LART_EMAIL.contains(currentState)) {
            return Set.of(lartHomeOfficeEmailAddress);
        }

        if (STATES_FOR_FTPA_EMAIL.contains(currentState)) {
            return Set.of(emailAddressFinder.getListCaseFtpaHomeOfficeEmailAddress(asylumCase));
        }

        if (STATES_FOR_UNLISTED_APPEAL.contains(currentState)
            && !appealService.isAppealListed(asylumCase)) {
            return Set.of(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
        }

        if (STATES_FOR_LISTED_APPEAL.contains(currentState) && appealService.isAppealListed(asylumCase)) {

            Optional<HearingCentre> maybeHearingCentre = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

            return maybeHearingCentre.isPresent()
                ? Set.of(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase))
                : Set.of(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
        }

        // Any state that reaches here is unexpected for Home Office routing
        throw new IllegalStateException(CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL_FLAG_IS_NOT_PRESENT);
    }
}
