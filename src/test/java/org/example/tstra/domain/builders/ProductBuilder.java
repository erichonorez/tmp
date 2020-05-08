package org.example.tstra.domain.builders;

import lombok.experimental.UtilityClass;
import org.example.tstra.domain.Product;
import org.example.tstra.domain.PositiveAmount;

import java.util.UUID;

@UtilityClass
public class ProductBuilder {

    public static Product aProduct() {
        try {
            return new Product(
                UUID.randomUUID().toString(),
                PositiveAmount.of(1),
                PositiveAmount.of(Integer.MAX_VALUE)
            );
        } catch (PositiveAmount.InvalidPositiveAmount invalidPositiveAmount) {
            throw new RuntimeException(invalidPositiveAmount);
        }
    }
}
