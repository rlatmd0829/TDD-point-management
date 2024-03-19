package io.hhplus.tdd.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.TransactionType;
import io.hhplus.tdd.dto.request.UserPointRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PointHistoryTable pointHistoryTable;

	@Autowired
	private UserPointTable userPointTable;

	@BeforeEach
	void setUp() {
		Long userId = 1L;
		Long amount = 100L;
		TransactionType transactionType = TransactionType.CHARGE;
		Long updateMillis = System.currentTimeMillis();

		// 여기서 넣은 내용이 BeforeEach로 돌면서 데이터가 쌓이긴한다.
		userPointTable.insertOrUpdate(userId, amount);
		pointHistoryTable.insert(userId, amount, transactionType, updateMillis);
	}

	@Test
	@DisplayName("유저의 포인트 조회 테스트")
	void getUserPointTest() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.get("/point/{id}", 1L))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.point").value(100L));
	}

	@Test
	@DisplayName("유저의 포인트 충전 테스트")
	void chargePointTest() throws Exception {
		UserPointRequest userPointRequest = UserPointRequest.of(100L);

		mockMvc.perform(
				MockMvcRequestBuilders
					.patch("/point/{id}/charge", 1L)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(userPointRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.point").value(200L));
	}

	@Test
	@DisplayName("유저의 포인트 내역 조회 테스트")
	void getUserPointHistoriesTest() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.get("/point/{id}/histories", 1L))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(1L))
			.andExpect(jsonPath("$[0].userId").value(1L))
			.andExpect(jsonPath("$[0].type").value(TransactionType.CHARGE.toString()))
			.andExpect(jsonPath("$[0].amount").value(100L));
	}

	@Test
	@DisplayName("유저의 포인트 사용 테스트")
	void usePointTest() throws Exception {
		UserPointRequest userPointRequest = UserPointRequest.of(100L);

		mockMvc.perform(
				MockMvcRequestBuilders
					.patch("/point/{id}/use", 1L)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(userPointRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.point").value(0L));
	}

	@Test
	@DisplayName("유저의 남은 잔액보다 많은 포인트를 사용하여 에러 발생 테스트")
	void usePointTest_whenUserBalanceIsExceeded_thenThrowRuntimeException() throws Exception {
		UserPointRequest userPointRequest = UserPointRequest.of(200L);

		mockMvc.perform(
				MockMvcRequestBuilders
					.patch("/point/{id}/use", 1L)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(userPointRequest)))
			.andExpect(status().isInternalServerError());
	}

}
