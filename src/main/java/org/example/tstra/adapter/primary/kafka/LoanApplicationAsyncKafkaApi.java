package org.example.tstra.adapter.primary.kafka;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.example.tstra.application.usecase.CreateLoanApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
public class LoanApplicationAsyncKafkaApi {

    private final CreateLoanApplication useCase;
    private final ObjectMapper objectMapper;

    public LoanApplicationAsyncKafkaApi(
        @NonNull CreateLoanApplication useCase,
        @NonNull ObjectMapper objectMapper) {
        this.useCase = useCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(id = "loan-application-app", topics = "requests")
    @SendTo("responses")
    public String handleEvent(String json) {
        try {
            CreateLoanApplicationKafkaRequest request = this.objectMapper.readValue(json, CreateLoanApplicationKafkaRequest.class);
            try {
                CreateLoanApplication.CreateLoanApplicationResponse result = this.useCase.execute(new CreateLoanApplication.CreateLoanApplicationRequest(
                    request.merchantId,
                    request.productId,
                    request.language,
                    request.purchaseAmount
                ));
                return this.objectMapper.writeValueAsString(new CreateLoanApplicationKafkaResponse(
                    request.correlationId,
                    "success",
                    result.getLoanApplicationId()
                ));
            } catch (CreateLoanApplication.CreateLoanApplicationException e) {
                return this.objectMapper.writeValueAsString(new CreateLoanApplicationKafkaResponse(
                    request.correlationId,
                    e.getClass().getSimpleName(),
                    e.getMessage()
                ));
            }
        } catch (JsonProcessingException e) {
            return "{\"type\":\"error\", \"message\":\"JsonProcessingException\"}";
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateLoanApplicationKafkaRequest {
        String correlationId;
        String merchantId;
        String productId;
        String language;
        int purchaseAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateLoanApplicationKafkaResponse {
        String correlationId;
        String type;
        String message;
    }

}
