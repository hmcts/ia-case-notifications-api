package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PreSubmitCallbackDispatcher;

@Tag(name = "Asylum Service")
@RequestMapping(
    path = "/bail",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class BailPreSubmitCallbackController extends PreSubmitCallbackController<BailCase> {

    public BailPreSubmitCallbackController(PreSubmitCallbackDispatcher<BailCase> callbackDispatcher) {
        super(callbackDispatcher);
    }

    @Operation(
        summary = "Handles 'AboutToStartEvent' callbacks from CCD or delegated calls from IA Case API",
        security =
        {
            @SecurityRequirement(name = "Authorization"),
            @SecurityRequirement(name = "ServiceAuthorization")
        },
        responses =
        {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transformed Bail case data, with any identified error or warning messages",
                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))
                ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))
                ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))
                ),
            @ApiResponse(
                    responseCode = "415",
                    description = "Unsupported Media Type",
                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))
                ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))
                )
        }
    )
    @PostMapping(path = "/ccdAboutToStart")
    public ResponseEntity<PreSubmitCallbackResponse<BailCase>> ccdAboutToStart(
        @Parameter(name = "Bail case data", required = true) @NotNull @RequestBody Callback<BailCase> callback
    ) {
        return super.ccdAboutToStart(callback);
    }

    @Operation(
            summary = "Handles 'AboutToSubmitEvent' callbacks from CCD or delegated calls from IA Case API",
            security =
            {
                @SecurityRequirement(name = "Authorization"),
                @SecurityRequirement(name = "ServiceAuthorization")
            },
            responses =
            {
                @ApiResponse(
                        responseCode = "200",
                        description = "Transformed Bail case data, with any identified error or warning messages",
                        content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Bad Request",
                        content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden",
                        content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
                @ApiResponse(
                        responseCode = "415",
                        description = "Unsupported Media Type",
                        content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal Server Error",
                        content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class)))
            }
    )
    @PostMapping(path = "/ccdAboutToSubmit")
    public ResponseEntity<PreSubmitCallbackResponse<BailCase>> ccdAboutToSubmit(
        @Parameter(name = "Bail case data", required = true) @NotNull @RequestBody Callback<BailCase> callback
    ) {
        return super.ccdAboutToSubmit(callback);
    }

}
