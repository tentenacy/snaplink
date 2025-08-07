package com.tenacy.snaplink.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ClickTrackingService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final UrlService urlService;

    @Async
    public void trackClick(String shortCode, String userAgent, String ipAddress) {
        // 기본 클릭 카운트 증가
        urlService.incrementClickCount(shortCode);

        // 총 클릭 카운트 키
        String clicksKey = "stats:clicks:" + shortCode;
        redisTemplate.opsForValue().increment(clicksKey, 1);

        // 일별 통계
        String dailyKey = "stats:daily:" + shortCode + ":" + LocalDate.now();
        redisTemplate.opsForValue().increment(dailyKey, 1);
        redisTemplate.expire(dailyKey, 30, TimeUnit.DAYS);

        // 브라우저 통계
        String browser = extractBrowser(userAgent);
        String browserKey = "stats:browser:" + shortCode;
        redisTemplate.opsForHash().increment(browserKey, browser, 1);
        redisTemplate.expire(browserKey, 90, TimeUnit.DAYS);

        // 국가별 통계 (IP 기반)
        String country = getCountryFromIp(ipAddress);
        String countryKey = "stats:country:" + shortCode;
        redisTemplate.opsForHash().increment(countryKey, country, 1);
        redisTemplate.expire(countryKey, 90, TimeUnit.DAYS);
    }

    private String extractBrowser(String userAgent) {
        // User-Agent 파싱 로직
        if (userAgent == null) return "unknown";

        // 가장 구체적인 브라우저부터 확인 (순서 중요)
        if (userAgent.contains("Edg/") || userAgent.contains("Edge/")) return "Edge";
        if (userAgent.contains("OPR/") || userAgent.contains("Opera/")) return "Opera";
        if (userAgent.contains("Firefox/")) return "Firefox";
        // Chrome 확인은 Safari 이전에 해야 함 (Safari에도 Chrome이 포함됨)
        if (userAgent.contains("Chrome/")) return "Chrome";
        // Safari는 Chrome 체크 이후에 해야 함
        if (userAgent.contains("Safari/")) return "Safari";
        if (userAgent.contains("MSIE") || userAgent.contains("Trident/")) return "IE";

        return "other";
    }

    private String getCountryFromIp(String ip) {
        // 실제 구현에서는 IP 지오로케이션 서비스 사용
        // 간단한 구현을 위해 더미 데이터 반환
        return "unknown";
    }
}