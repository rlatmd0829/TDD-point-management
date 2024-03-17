package io.hhplus.tdd;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.UserPoint;
import io.hhplus.tdd.dto.request.UserPointRequest;
import io.hhplus.tdd.dto.reseponse.PointHistoryResponse;
import io.hhplus.tdd.dto.reseponse.UserPointResponse;
import io.hhplus.tdd.service.PointService;

public class PointServiceTest {
	/*- PATCH  `/point/{id}/charge` : 포인트를 충전한다.
		- PATCH `/point/{id}/use` : 포인트를 사용한다.
		- GET `/point/{id}` : 포인트를 조회한다.
		- GET `/point/{id}/histories` : 포인트 내역을 조회한다.
		- 잔고가 부족할 경우, 포인트 사용은 실패하여야 합니다.
		- 동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다.*/

	private PointService pointService;

	@BeforeEach
	void setUp() {
		UserPointTable userPointTable = new UserPointTable();
		PointHistoryTable pointHistoryTable = new PointHistoryTable();
		pointService = new PointService(userPointTable, pointHistoryTable);
	}

	@Test
	@DisplayName("유저의 포인트를 조회한다")
	void getUserPointTest() {
		// given
		Long userId = 1L;
		Long point = 0L;
		Long updateMillis = 0L;

		// when
		UserPoint expectResult = new UserPoint(userId, point, updateMillis);
		UserPointResponse result = pointService.getUserPoint(1L);

		// then
		assertThat(result.id()).isEqualTo(expectResult.id());
		assertThat(result.point()).isEqualTo(expectResult.point());
	}

	@Test
	@DisplayName("유저의 포인트를 충전한다")
	void chargePointTest() {
		// given
		Long userId = 1L;
		Long amount = 100L;
		UserPointRequest userPointRequest = new UserPointRequest(amount);

		// when
		UserPointResponse expectResult = pointService.getUserPoint(1L);
		UserPointResponse result = pointService.charge(userId, userPointRequest);

		// then
		assertThat(result.id()).isEqualTo(expectResult.id());
		assertThat(result.point()).isEqualTo(amount);
	}

	@Test
	@DisplayName("포인트가 null일 경우 NullPointerException을 던진다")
	void chargePointTest_whenAmountIsNull_thenThrowNullPointerException() {
		// given & when & then
		assertThrows(NullPointerException.class, () -> new UserPointRequest(null));
	}

	@Test
	@DisplayName("유저의 포인트 내역을 조회한다.")
	void getUserPointHistoriesTest() {
		// given
		Long userId = 1L;
		Long amount = 100L;
		UserPointRequest userPointRequest = new UserPointRequest(amount);
		pointService.charge(userId, userPointRequest);

		// when
		List<PointHistoryResponse> pointHistories = pointService.getUserPointHistories(userId);

		// then
		assertThat(pointHistories).isNotEmpty();

	}

	@Test
	@DisplayName("유저의 포인트를 사용한다.")
	void usePointTest() {
		// given
		Long userId = 1L;
		Long amount = 100L;
		UserPointRequest userPointRequest = new UserPointRequest(amount);
		pointService.charge(userId, userPointRequest);

		// when
		UserPointResponse result = pointService.use(userId, userPointRequest);
		UserPointResponse expectResult = pointService.getUserPoint(userId);

		// then
		assertThat(result.id()).isEqualTo(expectResult.id());
		assertThat(result.point()).isEqualTo(expectResult.point());
	}

	@Test
	@DisplayName("포인트를 사용할때 잔고가 부족하면 RuntimeException을 던진다")
	void usePointTest_whenAmountExceedsBalance_thenThrowException() {
		// given
		Long userId = 1L;
		Long amount = 100L;
		UserPointRequest userPointRequest = new UserPointRequest(amount);

		// when & then
		assertThrows(RuntimeException.class, () -> pointService.use(userId, userPointRequest));
	}
}
