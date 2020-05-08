package org.example.tstra.domain;

import lombok.NonNull;
import lombok.Value;

@Value
public final class PositiveAmount {

    @NonNull
    private final int value;

    public static PositiveAmount of(int amount) throws InvalidPositiveAmount {
        if (amount < 0) {
            throw new InvalidPositiveAmount();
        }
        return new PositiveAmount(amount);
    }

    public static class InvalidPositiveAmount extends Exception {
    }
}
