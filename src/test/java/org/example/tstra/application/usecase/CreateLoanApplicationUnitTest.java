package org.example.tstra.application.usecase;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreateLoanApplicationUnitTest {

    @Test
    public void happyFlow() throws CreateLoanApplication.CreateLoanApplicationException {
        // given
        final String merchantId = UUID.randomUUID().toString();
        final String productId = UUID.randomUUID().toString();
        final String language = "fr";
        final int purchaseAmount = 40000; // in cents

        final String loanApplicationId = UUID.randomUUID().toString();

        // when
        CreateLoanApplication useCase = new CreateLoanApplication(
            () -> loanApplicationId,
            existingMerchantId -> existingMerchantId,
            (existingMerchantId, existingProductId) -> existingProductId
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
        final String language = "fr";
        final int purchaseAmount = 40000; // in cents

        final String loanApplicationId = UUID.randomUUID().toString();

        // when
        CreateLoanApplication useCase = new CreateLoanApplication(
            () -> loanApplicationId,
            nonExistingMerchantId -> {
                throw new CreateLoanApplication.MerchantNotFoundException();
            },
            (nonExistingMerchant, existingProductId) -> existingProductId
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
        final String productId = UUID.randomUUID().toString();
        final String language = "fr";
        final int purchaseAmount = 40000; // in cents

        final String loanApplicationId = UUID.randomUUID().toString();

        // when
        CreateLoanApplication useCase = new CreateLoanApplication(
            () -> loanApplicationId,
            existingMerchantId -> existingMerchantId,
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
        final String productId = UUID.randomUUID().toString();
        final String language = "en";
        final int purchaseAmount = 40000; // in cents

        final String loanApplicationId = UUID.randomUUID().toString();

        // when
        CreateLoanApplication useCase = new CreateLoanApplication(
            () -> loanApplicationId,
            existingMerchantId -> existingMerchantId,
            (existingMerchantId, existingProductId) -> existingProductId
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
        final String productId = UUID.randomUUID().toString();
        final String language = "fr";
        final int purchaseAmount = -40000; // in cents

        final String loanApplicationId = UUID.randomUUID().toString();

        // when
        CreateLoanApplication useCase = new CreateLoanApplication(
            () -> loanApplicationId,
            existingMerchantId -> existingMerchantId,
            (existingMerchantId, existingProductId) -> existingProductId
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
    public void whenThePurchaseAmountIsContainedInTheMerchantRange_itShouldThrowAnException() {
        // given
        final String merchantId = UUID.randomUUID().toString();
        final String productId = UUID.randomUUID().toString();
        final String language = "fr";
        final int purchaseAmount = -40000; // in cents

        final String loanApplicationId = UUID.randomUUID().toString();

        // when
        CreateLoanApplication useCase = new CreateLoanApplication(
            () -> loanApplicationId,
            existingMerchantId -> existingMerchantId,
            (existingMerchantId, existingProductId) -> existingProductId
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