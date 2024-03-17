package io.hhplus.tdd.service;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.UserPoint;
import io.hhplus.tdd.dto.reseponse.UserPointResponse;

@Service
public class PointService {
	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;

	public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
		this.userPointTable = userPointTable;
		this.pointHistoryTable = pointHistoryTable;
	}

	public UserPointResponse getUserPoint(Long userId) {
		UserPoint userPoint = userPointTable.selectById(userId);
		return UserPointResponse.of(userPoint);
	}

}
