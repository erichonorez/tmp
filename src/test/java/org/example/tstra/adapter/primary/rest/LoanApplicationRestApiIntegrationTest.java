package org.example.tstra.adapter.primary.rest;

import org.example.tstra.application.usecase.CreateLoanApplication;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoanApplicationRestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateLoanApplication createLoanApplication;

    @Test
    public void itShouldCreateALoanApplication() throws Exception {
        String loanApplicationId = UUID.randomUUID().toString();
        String merchantId = "123";
        String productId = "123";
        String language = "fr";
        int purchaseAmount = 4000;

        given(this.createLoanApplication.execute(new CreateLoanApplication.CreateLoanApplicationRequest(
            merchantId,
            productId,
            language,
            4000
        ))).willReturn(new CreateLoanApplication.CreateLoanApplicationResponse(loanApplicationId));

        String requestBody = String.format("{\"productId\": \"%s\", \"language\": \"%s\", \"purchaseAmount\": %s }", productId, language, purchaseAmount);
        String expectedResponseBody = String.format("{\"loanApplicationId\": \"%s\", \"authenticationUrl\": \"%s\"}", loanApplicationId, "www.djf.com");

        this.mockMvc.perform(
            post("/api/v1/loan-applications")
            .header("X-Custom-MerchantId", "123")
            .contentType("application/json")
            .content(requestBody)
        ).andDo(print())
        .andExpect(content().json(expectedResponseBody))
        .andExpect(status().isCreated());
    }

    @Test
    public void whenCreateLoanApplicationUseCaseFails_itShouldMapTheError() throws Exception {
        String merchantId = "123";
        String productId = "123";
        String language = "fr";
        int purchaseAmount = 4000;

        given(this.createLoanApplication.execute(new CreateLoanApplication.CreateLoanApplicationRequest(
            merchantId,
            productId,
            language,
            4000
        ))).willThrow(new CreateLoanApplication.MerchantNotFoundException());

        String requestBody = String.format("{\"productId\": \"%s\", \"language\": \"%s\", \"purchaseAmount\": %s }", productId, language, purchaseAmount);
        this.mockMvc.perform(
            post("/api/v1/loan-applications")
                .header("X-Custom-MerchantId", "123")
                .contentType("application/json")
                .content(requestBody)
        ).andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().json("{\"type\": \"MerchantNotFoundException \"}"));
    }

    @Test
    public void whenUnplannedExceptionIsThrown_itShouldResponseProperly() throws Exception {
        String merchantId = "123";
        String productId = "123";
        String language = "fr";
        int purchaseAmount = 4000;

        given(this.createLoanApplication.execute(new CreateLoanApplication.CreateLoanApplicationRequest(
            merchantId,
            productId,
            language,
            4000
        ))).willThrow(new RuntimeException());

        String requestBody = String.format("{\"productId\": \"%s\", \"language\": \"%s\", \"purchaseAmount\": %s }", productId, language, purchaseAmount);
        this.mockMvc.perform(
            post("/api/v1/loan-applications")
                .header("X-Custom-MerchantId", "123")
                .contentType("application/json")
                .content(requestBody)
        ).andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(content().json("{\"type\": \"RuntimeException\"}"));
    }

    @TestFactory
    public Stream<DynamicTest> whenRequestBodyDoesNotHaveAValidSchema_itShouldReturnAnError() throws Exception {
        return Arrays.asList(
            "{ }",
            "{ ",
            ";alkdfbh",
            "{ \"toto\": \"hello\" }",
            "{ \"productId\": null }",
            "{ \"productId\": 12 }",
            "{ \"productId\": \"qwe\", \"language\": }"
        ).stream().map(json -> {
            return dynamicTest(json, () -> {
                this.mockMvc.perform(
                    post("/api/v1/loan-applications")
                        .header("X-Custom-MerchantId", "123")
                        .contentType("application/json")
                        .content("{ }")
                )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("{\"type\": \"MethodArgumentNotValidException\"}"));
            });
        });
    }

}
