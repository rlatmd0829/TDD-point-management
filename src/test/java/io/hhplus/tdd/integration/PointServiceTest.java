package io.hhplus.tdd.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.hhplus.tdd.dto.request.UserPointRequest;
import io.hhplus.tdd.dto.reseponse.UserPointResponse;
import io.hhplus.tdd.service.PointService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class PointServiceTest {

	@Autowired
	private PointService pointService;

	@Test
	void testConcurrentDeposits() {
		// setup
		Long userId = 1L;

		// execute
		CompletableFuture.allOf(
			CompletableFuture.runAsync(() -> pointService.charge(userId, new UserPointRequest(100L))),
			CompletableFuture.runAsync(() -> pointService.charge(userId, new UserPointRequest(400L))),
			CompletableFuture.runAsync(() -> pointService.charge(userId, new UserPointRequest(300L))),
			CompletableFuture.runAsync(() -> pointService.charge(userId, new UserPointRequest(200L)))
		).join();

		// assert
		UserPointResponse result = pointService.getUserPoint(userId);
		assertEquals(100 + 400 + 300 + 200, result.point());
	}
}
