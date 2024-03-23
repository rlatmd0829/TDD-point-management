package io.hhplus.tdd.dto.reseponse;

import io.hhplus.tdd.domain.UserPoint;

public record UserPointResponse(
	Long id,
	Long point,
	Long updateMillis
) {
	public static UserPointResponse of(UserPoint userPoint) {
		return new UserPointResponse(
			userPoint.getId(),
			userPoint.getPoint(),
			userPoint.getUpdateMillis()
		);
	}
}
