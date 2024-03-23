package io.hhplus.tdd.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.PointHistory;
import io.hhplus.tdd.domain.TransactionType;
import io.hhplus.tdd.domain.UserPoint;
import io.hhplus.tdd.dto.request.UserPointRequest;
import io.hhplus.tdd.dto.reseponse.PointHistoryResponse;
import io.hhplus.tdd.dto.reseponse.UserPointResponse;
import io.hhplus.tdd.handle.LockHandler;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;
	private final LockHandler lockHandler;

	public UserPointResponse getUserPoint(Long userId) {
		UserPoint userPoint = userPointTable.selectById(userId);
		return UserPointResponse.of(userPoint);
	}

	public List<PointHistoryResponse> getUserPointHistories(Long userId) {
		List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(userId);
		return pointHistories.stream()
			.map(PointHistoryResponse::of)
			.collect(Collectors.toList());
	}

	public UserPointResponse charge(Long userId, UserPointRequest userPointRequest) {
		try {
			lockHandler.acquireLockForUser(userId);

			UserPoint originUserPoint = userPointTable.selectById(userId);
			originUserPoint.charge(userPointRequest.amount(), System.currentTimeMillis());
			UserPoint userPoint = userPointTable.insertOrUpdate(userId, originUserPoint.getPoint());
			pointHistoryTable.insert(userId, userPointRequest.amount(), TransactionType.CHARGE, System.currentTimeMillis());
			return UserPointResponse.of(userPoint);
		} finally {
			lockHandler.releaseLockForUser(userId);
		}
	}

	public UserPointResponse use(Long userId, UserPointRequest userPointRequest) {
		try {
			lockHandler.acquireLockForUser(userId);

			UserPoint originUserPoint = userPointTable.selectById(userId);
			originUserPoint.use(userPointRequest.amount(), System.currentTimeMillis());
			UserPoint userPoint = userPointTable.insertOrUpdate(userId, originUserPoint.getPoint());
			pointHistoryTable.insert(userId, userPointRequest.amount(), TransactionType.USE, System.currentTimeMillis());
			return UserPointResponse.of(userPoint);
		} finally {
			lockHandler.releaseLockForUser(userId);
		}
	}

}
