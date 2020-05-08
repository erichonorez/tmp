package org.example.tstra.application.usecase;

import org.example.tstra.domain.Merchant;
import org.example.tstra.domain.PositiveAmount;
import org.example.tstra.domain.Product;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.example.tstra.domain.builders.MerchantBuilder.aMerchant;
import static org.example.tstra.domain.builders.ProductBuilder.aProduct;
import static org.junit.jupiter.api.Assertions.*;

class CreateLoanApplicationUnitTest {

    @Test
    public void happyFlow() throws CreateLoanApplication.CreateLoanApplicationException, PositiveAmount.InvalidPositiveAmount {
        // given
        PositiveAmount minLoanAmount = PositiveAmount.of(1);
        PositiveAmount maxLoanAmount = PositiveAmount.of(50000);

        final String merchantId = UUID.randomUUID().toString();
        final Merchant merchant = aMerchant()
            .withMerchantId(merchantId)
            .withMinLoanAmount(minLoanAmount)
            .withMaxLoanAmount(maxLoanAmount);

        final String productId = UUID.randomUUID().toString();
        final Product product = aProduct()
            .withProductId(productId)
            .withMinAmount(minLoanAmount)
            .withMaxAmount(maxLoanAmount);

        final String language = "fr";
        final int purchaseAmount = 40000; // in cents

        final String loanApplicationId = UUID.randomUUID().toString();

        // when
        CreateLoanApplication useCase = new CreateLoanApplication(
            () -> loanApplicationId,
            existingMerchantId -> merchant,
            (existingMerchantId, existingProductId) -> product
        );

        CreateLoanApplication.CreateLoanApplicationResponse result = useCase.execute(
            new CreateLoanApplication.CreateLoanApplicationRequest(
                merchantId,
                productId,
                language,
                purchaseAmount
            )
        );

        // then
        assertEquals(loanApplicationId, result.getLoanApplicationId());
    }

    @Test
    public void whenThenMerchantDoesNotExist_itShouldThrowAnException() {
        // given
        final String merchantId = UUID.randomUUID().toString();
        final String productId = UUID.randomUUID().toString();
        final Product product = aProduct().withProductId(productId);
        final String language = "fr";
        final int purchaseAmount = 40000; // in cents

        final String loanApplicationId = UUID.randomUUID().toString();

        // when
        CreateLoanApplication useCase = new CreateLoanApplication(
            () -> loanApplicationId,
            nonExistingMerchantId -> {
                throw new CreateLoanApplication.MerchantNotFoundException();
            },
            (nonExistingMerchant, existingProductId) -> product
        );

        assertThrows(CreateLoanApplication.MerchantNotFoundException.class, () -> {
            useCase.execute(
                new CreateLoanApplication.CreateLoanApplicationRequest(
                    merchantId,
                    productId,
                    language,
                    purchaseAmount
                )
            );
        });
    }

    @Test
    public void whenTheProductDoesNotExist_itShouldThrowAnException() {
        // given
        final String merchantId = UUID.randomUUID().toString();
        final Merchant merchant = aMerchant().withMerchantId(merchantId);
        final String productId = UUID.randomUUID().toString();
        final Product product = aProduct();
        final String language = "fr";
        final int purchaseAmount = 40000; // in cents

        final String loanApplicationId = UUID.randomUUID().toString();

        // when
        CreateLoanApplication useCase = new CreateLoanApplication(
            () -> loanApplicationId,
            existingMerchantId -> merchant,
            (existingMerchantId, nonExistingProductId) -> {
                throw new CreateLoanApplication.ProductNotFoundException();
            }
        );

        assertThrows(CreateLoanApplication.ProductNotFoundException.class, () -> {
            useCase.execute(
                new CreateLoanApplication.CreateLoanApplicationRequest(
                    merchantId,
                    productId,
                    language,
                    purchaseAmount
                )
            );
        });
    }

    @Test
    public void whenTheLanguageIsInvalid_itShouldThrowAnException() {
        // given
        final String merchantId = UUID.randomUUID().toString();
        final Merchant merchant = aMerchant().withMerchantId(merchantId);
        final String productId = UUID.randomUUID().toString();
        final Product product = aProduct().withProductId(productId);
        final String language = "en";
        final int purchaseAmount = 40000; // in cents

        final String loanApplicationId = UUID.randomUUID().toString();

        // when
        CreateLoanApplication useCase = new CreateLoanApplication(
            () -> loanApplicationId,
            existingMerchantId -> merchant,
            (existingMerchantId, existingProductId) -> product
        );

        assertThrows(CreateLoanApplication.InvalidLanguageException.class, () -> {
            useCase.execute(
                new CreateLoanApplication.CreateLoanApplicationRequest(
                    merchantId,
                    productId,
                    language,
                    purchaseAmount
                )
            );
        });
    }

    @Test
    public void whenThePurchaseAmountIsInvalid_itShouldThrowAnException() {
        // given
        final String merchantId = UUID.randomUUID().toString();
        final Merchant merchant = aMerchant().withMerchantId(merchantId);
        final String productId = UUID.randomUUID().toString();
        final Product product = aProduct().withProductId(productId);
        final String language = "fr";
        final int purchaseAmount = -40000; // in cents

        final String loanApplicationId = UUID.randomUUID().toString();

        // when
        CreateLoanApplication useCase = new CreateLoanApplication(
            () -> loanApplicationId,
            existingMerchantId -> merchant,
            (existingMerchantId, existingProductId) -> product
        );

        assertThrows(CreateLoanApplication.InvalidPurchaseAmountException.class, () -> {
            useCase.execute(
                new CreateLoanApplication.CreateLoanApplicationRequest(
                    merchantId,
                    productId,
                    language,
                    purchaseAmount
                )
            );
        });
    }

    @Test
    public void whenThePurchaseAmountIsContainedInTheMerchantRange_itShouldThrowAnException() throws PositiveAmount.InvalidPositiveAmount {
        // given
        PositiveAmount maxLoanAmount = PositiveAmount.of(30000);

        final String merchantId = UUID.randomUUID().toString();
        final Merchant merchant = aMerchant()
            .withMerchantId(merchantId)
            .withMaxLoanAmount(maxLoanAmount);

        final String productId = UUID.randomUUID().toString();
        final Product product = aProduct()
            .withProductId(productId)
            .withMaxAmount(maxLoanAmount);

        final String language = "fr";
        final int purchaseAmount = 40000; // in cents

        final String loanApplicationId = UUID.randomUUID().toString();

        // when
        CreateLoanApplication useCase = new CreateLoanApplication(
            () -> loanApplicationId,
            existingMerchantId -> merchant,
            (existingMerchantId, existingProductId) -> product
        );

        assertThrows(CreateLoanApplication.PurchaseAmountOutOfRange.class, () -> {
            useCase.execute(
                new CreateLoanApplication.CreateLoanApplicationRequest(
                    merchantId,
                    productId,
                    language,
                    purchaseAmount
                )
            );
        });
    }

}