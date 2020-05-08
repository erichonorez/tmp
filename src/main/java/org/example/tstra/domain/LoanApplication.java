package org.example.tstra.domain;

import lombok.NonNull;
import lombok.Value;

@Value
public class LoanApplication {
    @NonNull
    private final String loanApplicationId;
    @NonNull
    private final String merchantId;
    @NonNull
    private final String productId;
    @NonNull
    private final Language language;
    @NonNull
    private final PositiveAmount purchaseAmount;
}
