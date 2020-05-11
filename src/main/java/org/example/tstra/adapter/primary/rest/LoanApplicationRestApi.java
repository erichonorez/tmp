package org.example.tstra.adapter.primary.rest;

import lombok.NonNull;
import lombok.Value;
import org.example.tstra.application.usecase.CreateLoanApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/loan-applications")
public class LoanApplicationRestApi {

    private final CreateLoanApplication createLoanApplication;

    @Inject
    public LoanApplicationRestApi(@NonNull CreateLoanApplication createLoanApplication) {
        this.createLoanApplication = createLoanApplication;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> createLoanApplication(
        @RequestBody @NotNull @Valid CreateLoanApplicationRestRequest request,
        @RequestHeader("X-Custom-MerchantId") @NotNull String merchantId) throws URISyntaxException {

        try {
            CreateLoanApplication.CreateLoanApplicationResponse result = this.createLoanApplication.execute(new CreateLoanApplication.CreateLoanApplicationRequest(
                merchantId,
                request.productId,
                request.language,
                request.purchaseAmount
            ));

            return ResponseEntity.created(new URI("/api/v1/loan-applications/" + result.getLoanApplicationId()))
                .body(new CreateLoanApplicationRestResponse(
                    result.getLoanApplicationId(),
                    "www.djf.com"
                ));
        } catch (CreateLoanApplication.CreateLoanApplicationException e) {
            return ResponseEntity.badRequest()
                .body(new Error(
                   e.getClass().getSimpleName(),
                   e.getMessage()
                ));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest()
            .body(new LoanApplicationRestApi.Error(
                ex.getClass().getSimpleName(),
                ex.getMessage()
            ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> runtimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new LoanApplicationRestApi.Error(
                ex.getClass().getSimpleName(),
                ex.getMessage()
            ));
    }

    public static class CreateLoanApplicationRestRequest {
        @NotNull
        public String productId;
        @NotNull
        public String language;
        @NotNull
        public int purchaseAmount;
    }

    @Value
    public static class CreateLoanApplicationRestResponse {
        String loanApplicationId;
        String authenticationUrl;
    }

    @Value
    public static class Error {
        String type;
        String message;
    }
}
