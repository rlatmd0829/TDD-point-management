package io.hhplus.tdd.dto.request;

import java.util.Objects;

import jakarta.validation.constraints.NotNull;

public record UserPointRequest(
	@NotNull
	Long amount
) {
	public UserPointRequest {
		Objects.requireNonNull(amount, "Amount must not be null");
	}
	public static UserPointRequest of(Long amount) {
		return new UserPointRequest(amount);
	}
}
