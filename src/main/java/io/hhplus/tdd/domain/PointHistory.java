package io.hhplus.tdd.domain;

public record PointHistory(
        Long id,
        Long userId,
        TransactionType type,
        Long amount,
        Long timeMillis
) {
}
