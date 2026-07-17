package com.zestia.datn.zestia.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RequestRateLimiter {
    private static final int MAX_KEYS = 10_000;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong();

    public boolean tryAcquire(String namespace, String key, int limit, long windowSeconds) {
        long now = Instant.now().getEpochSecond();
        if ((requestCounter.incrementAndGet() & 255) == 0) cleanup(now);
        if (buckets.size() >= MAX_KEYS) cleanup(now);

        String bucketKey = namespace + ":" + (key == null || key.isBlank() ? "unknown" : key);
        Bucket bucket = buckets.computeIfAbsent(bucketKey, ignored -> new Bucket());
        synchronized (bucket) {
            evictExpired(bucket.entries, now, windowSeconds);
            bucket.lastTouched = now;
            if (bucket.entries.size() >= limit) return false;
            bucket.entries.addLast(now);
            return true;
        }
    }

    public void reset(String namespace, String key) {
        buckets.remove(namespace + ":" + (key == null || key.isBlank() ? "unknown" : key));
    }

    private void cleanup(long now) {
        for (Map.Entry<String, Bucket> entry : buckets.entrySet()) {
            Bucket bucket = entry.getValue();
            if (now - bucket.lastTouched > 3600) buckets.remove(entry.getKey(), bucket);
        }
        if (buckets.size() < MAX_KEYS) return;
        buckets.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((left, right) -> Long.compare(left.lastTouched, right.lastTouched)))
                .limit(Math.max(1, buckets.size() - MAX_KEYS + 100))
                .forEach(entry -> buckets.remove(entry.getKey(), entry.getValue()));
    }

    private void evictExpired(Deque<Long> entries, long now, long windowSeconds) {
        while (!entries.isEmpty() && now - entries.peekFirst() >= windowSeconds) {
            entries.removeFirst();
        }
    }

    private static final class Bucket {
        private final Deque<Long> entries = new ArrayDeque<>();
        private volatile long lastTouched = Instant.now().getEpochSecond();
    }
}
