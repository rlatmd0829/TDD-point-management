package io.hhplus.tdd.handle;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

@Component
public class LockHandler {

	private final ConcurrentHashMap<Long, Lock> userLocks = new ConcurrentHashMap<>();

	public void acquireLockForUser(Long userId) {
		Lock userLock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
		userLock.lock();
	}

	public void releaseLockForUser(Long userId) {
		Lock userLock = userLocks.get(userId);
		if (userLock != null) {
			userLock.unlock();
		}
	}
}
