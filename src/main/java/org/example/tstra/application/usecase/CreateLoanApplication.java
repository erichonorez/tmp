package org.example.tstra.application.usecase;

import lombok.NonNull;
import lombok.Value;
import org.example.tstra.application.service.PurchaseAmountInRangePolicy;
import org.example.tstra.domain.Language;
import org.example.tstra.domain.Merchant;
import org.example.tstra.domain.PositiveAmount;
import org.example.tstra.domain.Product;

public final class CreateLoanApplication {

    private final LoanApplicationIdGenerator loanApplicationIdGenerator;
    private final MerchantService merchantService;
    private final ProductService productService;
    private final PurchaseAmountInRangePolicy purchaseAmountInRangePolicy;

    public CreateLoanApplication(
        @NonNull LoanApplicationIdGenerator loanApplicationIdGenerator,
        @NonNull MerchantService merchantService,
        @NonNull ProductService productService) {
        this.loanApplicationIdGenerator = loanApplicationIdGenerator;
        this.merchantService = merchantService;
        this.productService = productService;
        this.purchaseAmountInRangePolicy = new PurchaseAmountInRangePolicy();
    }

    public CreateLoanApplicationResponse execute(@NonNull CreateLoanApplicationRequest request) throws CreateLoanApplicationException {
        Merchant merchant = this.merchantService.findMerchantId(request.getMerchantId());
        Product product = this.productService.findProductId(merchant.getMerchantId(), request.getProductId());
        PositiveAmount purchaseAmount = this.validatePurchaseAmount(request.getPurchaseAmount());
        this.validatePurchaseAmountIsInRange(purchaseAmount, merchant, product);
        this.validateLanguage(request.getLanguage());

        return new CreateLoanApplicationResponse(
            loanApplicationIdGenerator.generateId()
        );
    }

    private PositiveAmount validatePurchaseAmountIsInRange(PositiveAmount purchaseAmount, Merchant merchant, Product product) throws PurchaseAmountOutOfRange {
        try {
            return this.purchaseAmountInRangePolicy.validate(purchaseAmount, merchant, product);
        } catch (PurchaseAmountInRangePolicy.InvalidPurchaseAmount invalidPurchaseAmount) {
            throw new PurchaseAmountOutOfRange();
        }
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
        Merchant findMerchantId(@NonNull String merchantId) throws MerchantNotFoundException;

    }

    public interface ProductService {
        Product findProductId(@NonNull String merchantId, @NonNull String productId) throws ProductNotFoundException;

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
