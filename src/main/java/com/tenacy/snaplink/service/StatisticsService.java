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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * 지정된 일수만큼의 일별 클릭 트렌드 조회
     * @param shortCode 단축 URL 코드
     * @param days 조회할 일수
     * @return 일별 클릭 수 맵 (날짜 -> 클릭 수)
     */
    public Map<String, Long> getDailyTrend(String shortCode, int days) {
        Map<String, Long> dailyTrend = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        // Redis Pipeline으로 여러 날짜의 데이터를 한 번에 조회
        List<Object> results = stringRedisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                StringRedisConnection stringConn = (StringRedisConnection) connection;

                // 지정된 일수만큼 과거 날짜부터 오늘까지 조회
                for (int i = days - 1; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    String key = "stats:daily:" + shortCode + ":" + date;
                    stringConn.get(key);
                }

                return null;
            }
        });

        for (int i = 0; i < days; i++) {
            LocalDate date = today.minusDays(days - 1 - i);
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            Long clicks = results.get(i) != null ? Long.parseLong((String) results.get(i)) : 0L;
            dailyTrend.put(dateStr, clicks);
        }

        return dailyTrend;
    }

    /**
     * 브라우저별 클릭 통계 조회
     * @param shortCode 단축 URL 코드
     * @return 브라우저별 클릭 수 맵 (브라우저 -> 클릭 수)
     */
    public Map<String, Long> getBrowserStats(String shortCode) {
        String browserKey = "stats:browser:" + shortCode;
        Map<Object, Object> rawData = redisTemplate.opsForHash().entries(browserKey);

        Map<String, Long> browserStats = new HashMap<>();
        for (Map.Entry<Object, Object> entry : rawData.entrySet()) {
            String browser = entry.getKey().toString();
            Long count = Long.parseLong(entry.getValue().toString());
            browserStats.put(browser, count);
        }

        return browserStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * 국가별 클릭 통계 조회
     * @param shortCode 단축 URL 코드
     * @return 국가별 클릭 수 맵 (국가 -> 클릭 수)
     */
    public Map<String, Long> getCountryStats(String shortCode) {
        String countryKey = "stats:country:" + shortCode;
        Map<Object, Object> rawData = redisTemplate.opsForHash().entries(countryKey);

        Map<String, Long> countryStats = new HashMap<>();
        for (Map.Entry<Object, Object> entry : rawData.entrySet()) {
            String country = entry.getKey().toString();
            Long count = Long.parseLong(entry.getValue().toString());
            countryStats.put(country, count);
        }

        return countryStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * 특정 URL의 모든 통계 정보를 종합적으로 조회
     * @param shortCode 단축 URL 코드
     * @return 종합 통계 정보
     */
    public Map<String, Object> getCompleteStats(String shortCode) {
        Map<String, Object> completeStats = new HashMap<>();

        // 총 클릭 수
        Map<String, Long> basicStats = getAllStats(shortCode);
        completeStats.put("total", basicStats.get("totalClicks"));
        completeStats.put("today", basicStats.get("dailyClicks"));

        // 일별 트렌드 (최근 7일)
        completeStats.put("dailyTrend", getDailyTrend(shortCode, 7));

        // 브라우저별 통계
        completeStats.put("browsers", getBrowserStats(shortCode));

        // 국가별 통계
        completeStats.put("countries", getCountryStats(shortCode));

        return completeStats;
    }
}