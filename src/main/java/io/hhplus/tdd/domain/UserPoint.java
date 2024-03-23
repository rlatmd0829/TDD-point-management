package io.hhplus.tdd.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPoint {
	Long id;
	Long point;
	Long updateMillis;

	public void charge(long amount, long updateMillis) {
		point += amount;
		this.updateMillis = updateMillis;
	}

	public void use(Long amount, Long updateMillis) {
		if (point < amount) {
			throw new RuntimeException("Not enough points to use");
		}
		point -= amount;
		this.updateMillis = updateMillis;
	}
}
