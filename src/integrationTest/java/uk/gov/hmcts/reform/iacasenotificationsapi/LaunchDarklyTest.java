package uk.gov.hmcts.reform.iacasenotificationsapi;

import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PreSubmitCallbackDispatcher;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers.PreSubmitCallbackController;


/*
To get this to work you will have to set an env variable called LAUNCH_DARKLY_SDK_KEY in your env
Also this assumes there is a feature toggle called some-feature-key switched on the IAC Launch Darkly project
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
public class LaunchDarklyTest {

    @MockBean private UserDetailsProvider userDetailsProvider;
    @MockBean private PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UserDetails userDetails;

    @Autowired
    private PreSubmitCallbackController preSubmitCallbackController;

    @Before
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(12345L);
    }

    @Test
    public void should_get_real_feature_toggle_value_and_skip_req_to_dispatcher() {

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn("123");
        when(userDetails.getForename()).thenReturn("forename");
        when(userDetails.getSurname()).thenReturn("surname");
        when(userDetails.getEmailAddress()).thenReturn("some-email");

        preSubmitCallbackController.ccdAboutToSubmit(callback);

        //Check no interactions with dispatcher
        verifyZeroInteractions(callbackDispatcher);
    }


}
