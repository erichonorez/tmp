package org.example.tstra.domain;

import lombok.NonNull;
import lombok.Value;
import lombok.With;
import org.example.tstra.domain.PositiveAmount;

@Value
@With
public class Product {
    @NonNull
    public final String productId;
    @NonNull
    public final PositiveAmount minAmount;
    @NonNull
    public final PositiveAmount maxAmount;
}
