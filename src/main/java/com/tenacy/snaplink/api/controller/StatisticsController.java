package com.tenacy.snaplink.api.controller;

import com.tenacy.snaplink.api.dto.UrlDto;
import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.service.StatisticsService;
import com.tenacy.snaplink.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatisticsController {
    private final UrlService urlService;
    private final StatisticsService statisticsService;

    @Value("${app.shorturl.domain}")
    private String domain;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Map<String, Object>> getUrlStats(@PathVariable String shortCode) {
        Map<String, Object> stats = new HashMap<>();

        // 기본 정보
        Url url = urlService.getUrlByShortCode(shortCode);
        stats.put("url", UrlDto.from(url, domain));

        // 클릭 통계
        stats.put("clicks", statisticsService.getAllStats(shortCode));

        // 일별 트렌드 (최근 7일)
        stats.put("dailyTrend", statisticsService.getDailyTrend(shortCode, 7));

        // 브라우저별 통계
        stats.put("browsers", statisticsService.getBrowserStats(shortCode));

        // 국가별 통계
        stats.put("countries", statisticsService.getCountryStats(shortCode));

        return ResponseEntity.ok(stats);
    }
}