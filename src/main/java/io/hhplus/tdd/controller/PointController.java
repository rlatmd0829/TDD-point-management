package io.hhplus.tdd.controller;

import io.hhplus.tdd.dto.request.UserPointRequest;
import io.hhplus.tdd.dto.reseponse.PointHistoryResponse;
import io.hhplus.tdd.dto.reseponse.UserPointResponse;
import io.hhplus.tdd.service.PointService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequestMapping("/point")
@RestController
@Validated
public class PointController {
    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public UserPointResponse point(@PathVariable Long id) {
        return pointService.getUserPoint(id);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistoryResponse> history(@PathVariable Long id) {
        return pointService.getUserPointHistories(id);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public UserPointResponse charge(
        @PathVariable Long id,
        @Valid @RequestBody UserPointRequest userPointRequest
	) {
        return pointService.charge(id, userPointRequest);
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public UserPointResponse use(
        @PathVariable Long id,
        @Valid @RequestBody UserPointRequest userPointRequest
    ) {
        return pointService.use(id, userPointRequest);
    }
}
