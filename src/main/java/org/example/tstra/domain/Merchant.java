package org.example.tstra.domain;

import lombok.NonNull;
import lombok.Value;
import lombok.With;
import org.example.tstra.domain.PositiveAmount;

@Value
@With
public class Merchant {
    @NonNull
    public final String merchantId;
    @NonNull
    public final PositiveAmount minLoanAmount;
    @NonNull
    public final PositiveAmount maxLoanAmount;
}
