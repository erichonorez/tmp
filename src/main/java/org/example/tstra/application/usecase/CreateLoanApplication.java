package org.example.tstra.application.usecase;

import lombok.NonNull;
import lombok.Value;
import org.example.tstra.domain.Language;
import org.example.tstra.domain.PositiveAmount;

public final class CreateLoanApplication {

    private final LoanApplicationIdGenerator loanApplicationIdGenerator;
    private final MerchantService merchantService;
    private final ProductService productService;

    public CreateLoanApplication(
        @NonNull LoanApplicationIdGenerator loanApplicationIdGenerator,
        @NonNull MerchantService merchantService,
        @NonNull ProductService productService) {
        this.loanApplicationIdGenerator = loanApplicationIdGenerator;
        this.merchantService = merchantService;
        this.productService = productService;
    }

    public CreateLoanApplicationResponse execute(@NonNull CreateLoanApplicationRequest request) throws CreateLoanApplicationException {
        String merchantId = this.merchantService.findMerchantId(request.merchantId);
        this.productService.findProductId(merchantId, request.productId);
        this.validateLanguage(request.language);
        this.validatePurchaseAmount(request.purchaseAmount);

        return new CreateLoanApplicationResponse(
            loanApplicationIdGenerator.generateId()
        );
    }

    private Language validateLanguage(String language) throws InvalidLanguageException {
        try {
            return Language.of(language);
        } catch (Language.InvalidLanguageException e) {
            throw new InvalidLanguageException();
        }
    }

    private PositiveAmount validatePurchaseAmount(int amount) throws InvalidPurchaseAmountException {
        try {
            return PositiveAmount.of(amount);
        } catch (PositiveAmount.InvalidPositiveAmount invalidPositiveAmount) {
            throw new InvalidPurchaseAmountException();
        }
    }

    // Dependencies
    public interface LoanApplicationIdGenerator {
        String generateId();
    }

    public interface MerchantService {
        String findMerchantId(@NonNull String merchantId) throws MerchantNotFoundException;
    }

    public interface ProductService {
        String findProductId(@NonNull String merchantId, @NonNull String productId) throws ProductNotFoundException;
    }

    @Value
    public static class CreateLoanApplicationResponse {
        @NonNull
        private final String loanApplicationId;
    }

    @Value
    public static class CreateLoanApplicationRequest {
        @NonNull
        private final String merchantId;
        @NonNull
        private final String productId;
        @NonNull
        private final String language;
        @NonNull
        private final int purchaseAmount;
    }


    public static abstract class CreateLoanApplicationException extends Exception { }

    public static final class MerchantNotFoundException extends CreateLoanApplicationException { }

    public static final class ProductNotFoundException extends CreateLoanApplicationException { }

    public static final class InvalidLanguageException extends CreateLoanApplicationException { }

    public static final class InvalidPurchaseAmountException extends CreateLoanApplicationException { }

    public static final class PurchaseAmountOutOfRange extends CreateLoanApplicationException { }

}
