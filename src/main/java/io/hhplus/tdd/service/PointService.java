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

@Service
public class PointService {
	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;
	private final ConcurrentHashMap<Long, Lock> userLocks = new ConcurrentHashMap<>();

	public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
		this.userPointTable = userPointTable;
		this.pointHistoryTable = pointHistoryTable;
	}

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
		Lock userLock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());

		userLock.lock();
		try {
			UserPoint originUserPoint = userPointTable.selectById(userId);
			UserPoint userPoint = userPointTable.insertOrUpdate(userId, originUserPoint.point() + userPointRequest.amount());
			pointHistoryTable.insert(userId, userPointRequest.amount(), TransactionType.CHARGE, System.currentTimeMillis());
			return UserPointResponse.of(userPoint);
		} finally {
			userLock.unlock();
		}
	}

	public UserPointResponse use(Long userId, UserPointRequest userPointRequest) {
		Lock userLock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());

		userLock.lock();
		try {
			UserPoint originUserPoint = userPointTable.selectById(userId);

			// TODO 이걸 UserPoint 객체에 넣으면 테스트 코드로 동시성 테스트가 가능해질것 같다?
			if (originUserPoint.point() < userPointRequest.amount()) {
				throw new RuntimeException("Not enough points to use");
			}

			UserPoint userPoint = userPointTable.insertOrUpdate(userId, originUserPoint.point() - userPointRequest.amount());
			pointHistoryTable.insert(userId, userPointRequest.amount(), TransactionType.USE, System.currentTimeMillis());
			return UserPointResponse.of(userPoint);
		} finally {
			userLock.unlock();
		}
	}

}
