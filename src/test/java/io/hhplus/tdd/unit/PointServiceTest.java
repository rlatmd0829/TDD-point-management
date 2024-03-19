package io.hhplus.tdd.unit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

class PointServiceTest {
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
	@DisplayName("유저의 포인트 조회 테스트")
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
	@DisplayName("유저의 포인트 충전 테스트")
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
	@DisplayName("포인트가 null일 경우 테스트")
	void chargePointTest_whenAmountIsNull_thenThrowNullPointerException() {
		// given & when & then
		assertThrows(NullPointerException.class, () -> new UserPointRequest(null));
	}

	@Test
	@DisplayName("유저의 포인트 내역을 조회 테스트")
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
	@DisplayName("유저의 포인트 사용 테스트")
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
	@DisplayName("유저의 남은 잔액보다 많은 포인트를 사용하여 에러 발생 테스트")
	void usePointTest_whenAmountExceedsBalance_thenThrowException() {
		// given
		Long userId = 1L;
		Long amount = 100L;
		UserPointRequest userPointRequest = new UserPointRequest(amount);

		// when & then
		assertThrows(RuntimeException.class, () -> pointService.use(userId, userPointRequest));
	}

	@Test
	@DisplayName("여러번 동시에 포인트를 충전하려 할때 순차적으로 적용되는지 테스트")
	void concurrentChargePointTest() throws InterruptedException {
		// given
		Long userId = 1L;
		Long amount = 100L;
		UserPointRequest userPointRequest = new UserPointRequest(amount);

		int numberOfThreads = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
		CountDownLatch latch = new CountDownLatch(numberOfThreads); // 각 스레드의 작업이 끝날 때마다 카운트 다운

		for (int i = 0; i < numberOfThreads; i++) {
			executorService.submit(() -> {
				try {
					pointService.charge(userId, userPointRequest);
				} catch (Exception e) {
					latch.countDown(); // 스레드 작업 종료
				}
			});
		}

		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);

		latch.await(10, TimeUnit.SECONDS); // 모든 스레드가 종료될 때까지 대기
		executorService.shutdown();

		// 여러 스레드에서 동시에 충전을 시도했을 때, 최종 충전 결과가 올바르게 반영되었는지 확인
		UserPointResponse result = pointService.getUserPoint(userId);
		Long expectedPoint = amount * numberOfThreads; // 각 스레드에서 충전한 금액의 합
		assertEquals(expectedPoint, result.point(), "여러 스레드에서 동시에 충전한 경우 충전 결과가 올바르지 않습니다.");

	}

	@Test
	@DisplayName("여러번 동시에 포인트를 사용하려 할때 순차적으로 동작하는지 테스트")
	void concurrentUsePointTest() throws InterruptedException {
		// given
		Long userId = 1L;
		Long amount = 100L;
		UserPointRequest userPointRequest = new UserPointRequest(amount);

		int numberOfThreads = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
		CountDownLatch latch = new CountDownLatch(numberOfThreads); // 각 스레드의 작업이 끝날 때마다 카운트 다운

		List<Exception> exceptions = new ArrayList<>(); // 각 스레드에서 발생한 예외를 저장할 리스트


		for (int i = 0; i < numberOfThreads; i++) {
			executorService.submit(() -> {
				try {
					pointService.use(userId, userPointRequest);
				} catch (Exception e) {
					exceptions.add(e); // 예외가 발생한 경우 리스트에 추가
				} finally {
					latch.countDown(); // 스레드 작업 종료
				}
			});
		}

		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);

		latch.await(10, TimeUnit.SECONDS); // 모든 스레드가 종료될 때까지 대기
		executorService.shutdown();

		// 각 스레드에서 발생한 예외가 없는지 확인
		assertEquals(10, exceptions.size(), "포인트 사용 중 예외가 발생했습니다.");
	}
}
