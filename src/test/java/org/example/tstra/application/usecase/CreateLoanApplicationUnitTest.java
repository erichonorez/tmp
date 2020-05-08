package org.example.tstra.application.usecase;

import org.example.tstra.domain.Merchant;
import org.example.tstra.domain.PositiveAmount;
import org.example.tstra.domain.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.example.tstra.domain.builders.MerchantBuilder.aMerchant;
import static org.example.tstra.domain.builders.ProductBuilder.aProduct;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CreateLoanApplicationUnitTest {

    @Mock
    private CreateLoanApplication.LoanApplicationIdGenerator loanApplicationIdGenerator;

    @Mock
    private CreateLoanApplication.MerchantService merchantService;

    @Mock
    private CreateLoanApplication.ProductService productService;

    @InjectMocks
    private CreateLoanApplication useCase;

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

        given(this.loanApplicationIdGenerator.generateId()).willReturn(loanApplicationId);
        given(this.merchantService.findMerchantId(merchantId)).willReturn(merchant);
        given(this.productService.findProductId(merchantId, productId)).willReturn(product);

        // when
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
    public void whenThenMerchantDoesNotExist_itShouldThrowAnException() throws CreateLoanApplication.MerchantNotFoundException, CreateLoanApplication.ProductNotFoundException {
        // given
        final String merchantId = UUID.randomUUID().toString();
        final String productId = UUID.randomUUID().toString();
        final Product product = aProduct().withProductId(productId);
        final String language = "fr";
        final int purchaseAmount = 40000; // in cents

        given(this.merchantService.findMerchantId(merchantId)).willThrow(CreateLoanApplication.MerchantNotFoundException.class);

        // when // then
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
    public void whenTheProductDoesNotExist_itShouldThrowAnException() throws CreateLoanApplication.MerchantNotFoundException, CreateLoanApplication.ProductNotFoundException {
        // given
        final String merchantId = UUID.randomUUID().toString();
        final Merchant merchant = aMerchant().withMerchantId(merchantId);
        final String productId = UUID.randomUUID().toString();
        final Product product = aProduct();
        final String language = "fr";
        final int purchaseAmount = 40000; // in cents

        given(this.merchantService.findMerchantId(merchantId)).willReturn(merchant);
        given(this.productService.findProductId(merchantId, productId)).willThrow(CreateLoanApplication.ProductNotFoundException.class);

        // when then
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
    public void whenTheLanguageIsInvalid_itShouldThrowAnException() throws CreateLoanApplication.MerchantNotFoundException, CreateLoanApplication.ProductNotFoundException {
        // given
        final String merchantId = UUID.randomUUID().toString();
        final Merchant merchant = aMerchant().withMerchantId(merchantId);
        final String productId = UUID.randomUUID().toString();
        final Product product = aProduct().withProductId(productId);
        final String language = "en";
        final int purchaseAmount = 40000; // in cents

        given(this.merchantService.findMerchantId(merchantId)).willReturn(merchant);
        given(this.productService.findProductId(merchantId, productId)).willReturn(product);

        // when // then
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
    public void whenThePurchaseAmountIsInvalid_itShouldThrowAnException() throws CreateLoanApplication.MerchantNotFoundException, CreateLoanApplication.ProductNotFoundException {
        // given
        final String merchantId = UUID.randomUUID().toString();
        final Merchant merchant = aMerchant().withMerchantId(merchantId);
        final String productId = UUID.randomUUID().toString();
        final Product product = aProduct().withProductId(productId);
        final String language = "fr";
        final int purchaseAmount = -40000; // in cents

        given(this.merchantService.findMerchantId(merchantId)).willReturn(merchant);
        given(this.productService.findProductId(merchantId, productId)).willReturn(product);

        // when then
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
    public void whenThePurchaseAmountIsContainedInTheMerchantRange_itShouldThrowAnException() throws PositiveAmount.InvalidPositiveAmount, CreateLoanApplication.MerchantNotFoundException, CreateLoanApplication.ProductNotFoundException {
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

        given(this.merchantService.findMerchantId(merchantId)).willReturn(merchant);
        given(this.productService.findProductId(merchantId, productId)).willReturn(product);

        // when then
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