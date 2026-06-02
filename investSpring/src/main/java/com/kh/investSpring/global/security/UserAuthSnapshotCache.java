package com.kh.investSpring.global.security;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.kh.investSpring.domain.user.dao.UserDao;
import com.kh.investSpring.domain.user.vo.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserAuthSnapshotCache {

	private static final long TTL_MS = 60_000L;

	private final UserDao userDao;
	private final ConcurrentHashMap<Long, CachedSnapshot> cache = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, Object> locks = new ConcurrentHashMap<>();

	public UserAuthSnapshot get(Long userNo) {
		if (userNo == null) {
			return null;
		}

		long now = System.currentTimeMillis();
		CachedSnapshot cached = cache.get(userNo);
		if (cached != null && cached.expiresAt() > now) {
			return cached.snapshot();
		}

		Object lock = locks.computeIfAbsent(userNo, ignored -> new Object());
		synchronized (lock) {
			try {
				now = System.currentTimeMillis();
				cached = cache.get(userNo);
				if (cached != null && cached.expiresAt() > now) {
					return cached.snapshot();
				}

				User user = userDao.selectUserByUserNo(userNo);
				if (user == null) {
					cache.remove(userNo);
					return null;
				}

				UserAuthSnapshot snapshot = new UserAuthSnapshot((long) user.getUserNo(), user.getAuth());
				cache.put(userNo, new CachedSnapshot(snapshot, now + TTL_MS));
				return snapshot;
			} finally {
				locks.remove(userNo, lock);
			}
		}
	}

	public void evict(Long userNo) {
		if (userNo != null) {
			cache.remove(userNo);
		}
	}

	public record UserAuthSnapshot(Long userNo, int auth) {
	}

	private record CachedSnapshot(UserAuthSnapshot snapshot, long expiresAt) {
	}
}
