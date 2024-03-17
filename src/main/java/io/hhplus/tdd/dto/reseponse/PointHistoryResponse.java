package io.hhplus.tdd.dto.reseponse;

import io.hhplus.tdd.domain.PointHistory;
import io.hhplus.tdd.domain.TransactionType;

public record PointHistoryResponse(
	Long id,
	Long userId,
	TransactionType type,
	Long amount,
	Long timeMillis
) {
	public static PointHistoryResponse of(PointHistory pointHistory) {
		return new PointHistoryResponse(
			pointHistory.id(),
			pointHistory.userId(),
			pointHistory.type(),
			pointHistory.amount(),
			pointHistory.timeMillis()
		);
	}
}
