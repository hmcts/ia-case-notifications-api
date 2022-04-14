package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PostSubmitCallbackDispatcher;

@Slf4j
@Api(
    value = "/asylum",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class AsylumPostSubmitCallbackController extends PostSubmitCallbackController<AsylumCase> {

    public AsylumPostSubmitCallbackController(PostSubmitCallbackDispatcher<AsylumCase> callbackDispatcher) {
        super(callbackDispatcher);
    }

    @ApiOperation(
        value = "Handles 'SubmittedEvent' callbacks from CCD",
        response = PostSubmitCallbackResponse.class,
        authorizations =
            {
            @Authorization(value = "Authorization"),
            @Authorization(value = "ServiceAuthorization")
            }
    )
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Optional confirmation text for CCD UI",
            response = PostSubmitCallbackResponse.class
            ),
        @ApiResponse(
            code = 400,
            message = "Bad Request",
            response = PostSubmitCallbackResponse.class
            ),
        @ApiResponse(
            code = 403,
            message = "Forbidden",
            response = PostSubmitCallbackResponse.class
            ),
        @ApiResponse(
            code = 415,
            message = "Unsupported Media Type",
            response = PostSubmitCallbackResponse.class
            ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error",
            response = PostSubmitCallbackResponse.class
            )
    })
    @PostMapping(path = "/ccdSubmitted")
    public ResponseEntity<PostSubmitCallbackResponse> ccdSubmitted(
        @ApiParam(value = "Asylum case data", required = true) @RequestBody Callback<AsylumCase> callback
    ) {
        return super.ccdSubmitted(callback);
    }
}
