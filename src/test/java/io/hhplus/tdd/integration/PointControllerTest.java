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

		userPointTable.insertOrUpdate(userId, amount);
		pointHistoryTable.insert(userId, amount, transactionType, updateMillis);
	}

	@Test
	@DisplayName("유저의 포인트를 조회한다.")
	void getUserPointTest() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.get("/point/{id}", 1L))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.point").value(100L));
	}

	@Test
	@DisplayName("유저의 포인트를 충전한다.")
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

	/*TODO 포인트 충전이 null이 들어온다*/

	@Test
	@DisplayName("유저의 포인트 내역을 조회한다.")
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
	@DisplayName("유저의 포인트를 사용한다.")
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

	/*TODO 유저의 남은 잔액보다 많은 포인트를 사용하여 에러가 발생하는 테스트*/

}
