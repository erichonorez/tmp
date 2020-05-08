package org.example.tstra.domain.builders;

import lombok.experimental.UtilityClass;
import org.example.tstra.domain.Merchant;
import org.example.tstra.domain.PositiveAmount;

import java.util.UUID;

@UtilityClass
public class MerchantBuilder {

    public static Merchant aMerchant() {
        try {
            return new Merchant(
                UUID.randomUUID().toString(),
                PositiveAmount.of(0),
                PositiveAmount.of(Integer.MAX_VALUE)
            );
        } catch (PositiveAmount.InvalidPositiveAmount invalidPositiveAmount) {
            throw new RuntimeException(invalidPositiveAmount);
        }
    }

}
