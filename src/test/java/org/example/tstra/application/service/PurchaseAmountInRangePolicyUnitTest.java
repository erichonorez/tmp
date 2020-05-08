package org.example.tstra.application.service;

import org.example.tstra.domain.Merchant;
import org.example.tstra.domain.PositiveAmount;
import org.example.tstra.domain.Product;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.example.tstra.domain.builders.MerchantBuilder.aMerchant;
import static org.example.tstra.domain.builders.ProductBuilder.aProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class PurchaseAmountInRangePolicyUnitTest {

    @TestFactory
    public Stream<DynamicTest> policyShouldPass() throws PositiveAmount.InvalidPositiveAmount {
        Merchant merchant = aMerchant()
            .withMinLoanAmount(PositiveAmount.of(100))
            .withMaxLoanAmount(PositiveAmount.of(200));

        Product product = aProduct()
            .withMinAmount(PositiveAmount.of(125))
            .withMaxAmount(PositiveAmount.of(225));

        PurchaseAmountInRangePolicy purchaseAmountInRangePolicy = new PurchaseAmountInRangePolicy();

        return IntStream.range(125, 200)
            .boxed()
            .map(i -> {
                return dynamicTest(i + "", () -> assertEquals(i, purchaseAmountInRangePolicy.validate(PositiveAmount.of(i), merchant, product).getValue()));
            });
    }

    @TestFactory
    public Stream<DynamicTest> policyShouldFail_lowerBound() throws PositiveAmount.InvalidPositiveAmount {
        Merchant merchant = aMerchant()
            .withMinLoanAmount(PositiveAmount.of(100))
            .withMaxLoanAmount(PositiveAmount.of(200));

        Product product = aProduct()
            .withMinAmount(PositiveAmount.of(125))
            .withMaxAmount(PositiveAmount.of(225));

        PurchaseAmountInRangePolicy purchaseAmountInRangePolicy = new PurchaseAmountInRangePolicy();

        return IntStream.range(90, 125)
            .boxed()
            .map(i -> {
                return dynamicTest(i + "", () -> assertThrows(PurchaseAmountInRangePolicy.InvalidPurchaseAmount.class, () -> purchaseAmountInRangePolicy.validate(PositiveAmount.of(i), merchant, product)));
            });
    }

    @TestFactory
    public Stream<DynamicTest> policyShouldFail_higherBound() throws PositiveAmount.InvalidPositiveAmount {
        Merchant merchant = aMerchant()
            .withMinLoanAmount(PositiveAmount.of(100))
            .withMaxLoanAmount(PositiveAmount.of(200));

        Product product = aProduct()
            .withMinAmount(PositiveAmount.of(125))
            .withMaxAmount(PositiveAmount.of(225));

        PurchaseAmountInRangePolicy purchaseAmountInRangePolicy = new PurchaseAmountInRangePolicy();

        return IntStream.range(201, 240)
            .boxed()
            .map(i -> {
                return dynamicTest(i + "", () -> assertThrows(PurchaseAmountInRangePolicy.InvalidPurchaseAmount.class, () -> purchaseAmountInRangePolicy.validate(PositiveAmount.of(i), merchant, product)));
            });
    }

}
