package com.tenacy.snaplink.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    // 여러 통계 정보를 일괄 처리
    public Map<String, Long> getAllStats(String shortCode) {
        final String clicksKey = "stats:clicks:" + shortCode;
        final String dailyKey = "stats:daily:" + shortCode + ":" + LocalDate.now();
        final String browserKey = "stats:browser:" + shortCode;

        // Redis Pipeline으로 여러 명령 일괄 처리
        List<Object> results = stringRedisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                StringRedisConnection stringConn = (StringRedisConnection) connection;

                // 여러 명령을 한 번의 네트워크 왕복으로 처리
                stringConn.get(clicksKey);
                stringConn.get(dailyKey);
                stringConn.hGetAll(browserKey);

                return null; // 결과는 List<Object>로 반환됨
            }
        });

        // 결과 처리
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalClicks", results.get(0) != null ? Long.parseLong((String) results.get(0)) : 0);
        stats.put("dailyClicks", results.get(1) != null ? Long.parseLong((String) results.get(1)) : 0);

        // 브라우저별 통계
        if (results.get(2) != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> browserStats = (Map<String, String>) results.get(2);

            for (Map.Entry<String, String> entry : browserStats.entrySet()) {
                stats.put("browser_" + entry.getKey(), Long.parseLong(entry.getValue()));
            }
        }

        return stats;
    }
}