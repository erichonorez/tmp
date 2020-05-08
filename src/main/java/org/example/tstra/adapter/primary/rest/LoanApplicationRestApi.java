package org.example.tstra.adapter.primary.rest;

import lombok.NonNull;
import lombok.Value;
import org.example.tstra.application.usecase.CreateLoanApplication;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    public CreateLoanApplicationRestResponse createLoanApplication(
        @RequestBody @NotNull @Valid CreateLoanApplicationRestRequest request,
        @RequestHeader("merchantId") @NotNull String merchantId) {

        return new CreateLoanApplicationRestResponse(
            UUID.randomUUID().toString(),
            "www.djf.com"
        );
    }

    @Value
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
        public String loanApplicationId;
        public String authenticationUrl;
    }
}
