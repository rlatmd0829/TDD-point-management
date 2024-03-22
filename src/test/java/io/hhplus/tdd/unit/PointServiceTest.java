package io.hhplus.tdd.unit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.PointHistory;
import io.hhplus.tdd.domain.TransactionType;
import io.hhplus.tdd.domain.UserPoint;
import io.hhplus.tdd.dto.request.UserPointRequest;
import io.hhplus.tdd.dto.reseponse.PointHistoryResponse;
import io.hhplus.tdd.dto.reseponse.UserPointResponse;
import io.hhplus.tdd.service.PointService;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
	/*- PATCH  `/point/{id}/charge` : 포인트를 충전한다.
		- PATCH `/point/{id}/use` : 포인트를 사용한다.
		- GET `/point/{id}` : 포인트를 조회한다.
		- GET `/point/{id}/histories` : 포인트 내역을 조회한다.
		- 잔고가 부족할 경우, 포인트 사용은 실패하여야 합니다.
		- 동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다.*/

	@InjectMocks
	private PointService pointService;

	@Mock
	private UserPointTable userPointTable;

	@Mock
	private PointHistoryTable pointHistoryTable;

	@Test
	@DisplayName("유저의 포인트 조회 테스트")
	void getUserPointTest() {
		// given
		Long userId = 1L;
		Long point = 0L;
		Long updateMillis = 0L;

		// when
		when(userPointTable.selectById(anyLong())).thenReturn(new UserPoint(userId, point, updateMillis));
		UserPointResponse expectResult = new UserPointResponse(userId, point, updateMillis);
		UserPointResponse result = pointService.getUserPoint(1L);

		// then
		assertThat(result.id()).isEqualTo(expectResult.id());
		assertThat(result.point()).isEqualTo(expectResult.point());
	}

	@Test
	@DisplayName("유저의 포인트 충전 테스트")
	void chargePointTest() {
		// given
		Long id = 1L;
		Long userId = 1L;
		Long point = 100L;
		Long amount = 100L;
		Long updateMillis = 0L;
		TransactionType transactionType = TransactionType.CHARGE;
		UserPointRequest userPointRequest = new UserPointRequest(amount);

		// when
		when(userPointTable.selectById(anyLong())).thenReturn(new UserPoint(userId, point, updateMillis));
		when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(new UserPoint(userId, point + amount, updateMillis));
		when(pointHistoryTable.insert(anyLong(), anyLong(), any(), anyLong())).thenReturn(new PointHistory(id, userId, transactionType, amount, System.currentTimeMillis()));

		UserPointResponse expectResult = new UserPointResponse(userId, point + amount, updateMillis);
		UserPointResponse result = pointService.charge(userId, userPointRequest);

		// then
		assertThat(result.id()).isEqualTo(expectResult.id());
		assertThat(result.point()).isEqualTo(expectResult.point());
	}

	@Test
	@DisplayName("포인트가 null일 경우 테스트")
	void chargePointTest_whenAmountIsNull_thenThrowNullPointerException() {
		// given & when & then
		assertThatThrownBy(() -> new UserPointRequest(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("유저의 포인트 내역을 조회 테스트")
	void getUserPointHistoriesTest() {
		// given
		Long userId = 1L;
		Long amount = 100L;

		// when
		when(pointHistoryTable.selectAllByUserId(anyLong())).thenReturn(List.of(new PointHistory(1L, userId, TransactionType.CHARGE, amount, System.currentTimeMillis())));
		List<PointHistoryResponse> expectResult = List.of(new PointHistoryResponse(1L, userId, TransactionType.CHARGE, amount, System.currentTimeMillis()));
		List<PointHistoryResponse> pointHistories = pointService.getUserPointHistories(userId);

		// then
		assertThat(pointHistories.size()).isEqualTo(expectResult.size());
		assertThat(pointHistories.get(0).id()).isEqualTo(expectResult.get(0).id());
		assertThat(pointHistories.get(0).userId()).isEqualTo(expectResult.get(0).userId());
	}

	@Test
	@DisplayName("유저의 포인트 사용 테스트")
	void usePointTest() {
		// given
		Long userId = 1L;
		Long amount = 100L;
		Long point = 100L;
		Long updateMillis = 0L;
		UserPointRequest userPointRequest = new UserPointRequest(amount);

		// when
		when(userPointTable.selectById(anyLong())).thenReturn(new UserPoint(userId, point, updateMillis));
		when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(new UserPoint(userId, point - amount, updateMillis));
		when(pointHistoryTable.insert(anyLong(), anyLong(), any(), anyLong())).thenReturn(new PointHistory(1L, userId, TransactionType.USE, amount, System.currentTimeMillis()));

		UserPointResponse expectResult = new UserPointResponse(userId, point - amount, updateMillis);
		UserPointResponse result = pointService.use(userId, userPointRequest);

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

		when(userPointTable.selectById(anyLong())).thenReturn(new UserPoint(userId, 50L, 0L));

		// when & then
		assertThatThrownBy(() -> pointService.use(userId, userPointRequest)).isInstanceOf(RuntimeException.class);
	}

	// TODO 여러번 동시에 포인트를 충전하려 할때 순차적으로 적용되는지 테스트 어떻게 테스트 하지
	// mock을 사용해서 테이블에 값을 쌓이지 않아서 get을 해도 모든 요청에 대한 값은 못가져오잖아
	@Test
	@DisplayName("여러번 동시에 포인트를 충전하려 할때 순차적으로 적용되는지 테스트")
	void concurrentChargePointTest() throws InterruptedException {
		// given
		Long id = 1L;
		Long userId = 1L;
		Long amount = 100L;
		Long point = 100L;
		Long updateMillis = 0L;
		TransactionType transactionType = TransactionType.CHARGE;
		UserPointRequest userPointRequest = new UserPointRequest(amount);

		int numberOfThreads = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
		CountDownLatch latch = new CountDownLatch(numberOfThreads);

		// when
		for (int i = 0; i < numberOfThreads; i++) {
			executorService.submit(() -> {
				// when(userPointTable.selectById(anyLong())).thenReturn(new UserPoint(userId, point, updateMillis));
				// when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(new UserPoint(userId, point + amount, updateMillis));
				// when(pointHistoryTable.insert(anyLong(), anyLong(), any(), anyLong())).thenReturn(new PointHistory(id, userId, transactionType, amount, System.currentTimeMillis()));
				pointService.charge(userId, userPointRequest);
				latch.countDown();
			});
		}

		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);

		latch.await(10, TimeUnit.SECONDS);
		executorService.shutdown();

		// then
		// assertThat(latch.getCount()).isEqualTo(0);

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
