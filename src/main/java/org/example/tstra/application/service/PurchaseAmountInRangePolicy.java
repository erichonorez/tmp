package org.example.tstra.application.service;

import org.example.tstra.domain.Merchant;
import org.example.tstra.domain.Product;
import org.example.tstra.domain.PositiveAmount;

public class PurchaseAmountInRangePolicy {

    public PositiveAmount validate(PositiveAmount purchaseAmount, Merchant merchant, Product product) throws InvalidPurchaseAmount {
        int max = Math.min(merchant.getMaxLoanAmount().getValue(), product.getMaxAmount().getValue());
        int min = Math.max(merchant.getMinLoanAmount().getValue(), product.getMinAmount().getValue());
        int value = purchaseAmount.getValue();

        if (value < min || value > max) {
            throw new InvalidPurchaseAmount();
        }

        return purchaseAmount;
    }

    public static class InvalidPurchaseAmount extends Exception { }
}
