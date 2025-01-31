package com.tenacy.snaplink.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ClickTrackingService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final UrlService urlService;

    public void trackClick(String shortCode, HttpServletRequest request) {
        // 기본 클릭 카운트 증가
        urlService.incrementClickCount(shortCode);

        // 일별 통계
        String dailyKey = "stats:daily:" + shortCode + ":" + LocalDate.now();
        redisTemplate.opsForValue().increment(dailyKey, 1);
        redisTemplate.expire(dailyKey, 30, TimeUnit.DAYS);

        // 브라우저 통계
        String browser = extractBrowser(request.getHeader("User-Agent"));
        String browserKey = "stats:browser:" + shortCode;
        redisTemplate.opsForHash().increment(browserKey, browser, 1);

        // 국가별 통계 (IP 기반)
        String country = getCountryFromIp(getClientIp(request));
        String countryKey = "stats:country:" + shortCode;
        redisTemplate.opsForHash().increment(countryKey, country, 1);
    }

    private String extractBrowser(String userAgent) {
        // User-Agent 파싱 로직
        if (userAgent == null) return "unknown";

        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) return "IE";
        if (userAgent.contains("Opera") || userAgent.contains("OPR")) return "Opera";

        return "other";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String getCountryFromIp(String ip) {
        // 실제 구현에서는 IP 지오로케이션 서비스 사용
        // 간단한 구현을 위해 더미 데이터 반환
        return "unknown";
    }
}