package com.tenacy.snaplink.api.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Tag(name = "3. 메트릭 API", description = "")
public class MetricsController {
    private final MeterRegistry meterRegistry;

    @GetMapping
    @Operation(summary = "전체 메트릭 조회", description = "")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "성공"))
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // 캐시 히트율
        double hits = meterRegistry.find("cache.hits").counter().count();
        double misses = meterRegistry.find("cache.misses").counter().count();
        double total = hits + misses;
        double hitRatio = total > 0 ? hits / total : 0;

        metrics.put("cacheHitRatio", String.format("%.2f%%", hitRatio * 100));

        // 응답시간
        Timer redirectTimer = meterRegistry.find("url.redirect.time").timer();
        if (redirectTimer != null) {
            metrics.put("avgResponseTime", String.format("%.2f ms", redirectTimer.mean(TimeUnit.MILLISECONDS)));
            metrics.put("p95ResponseTime", String.format("%.2f ms", redirectTimer.percentile(0.95, TimeUnit.MILLISECONDS)));
            metrics.put("p99ResponseTime", String.format("%.2f ms", redirectTimer.percentile(0.99, TimeUnit.MILLISECONDS)));
        }

        // TPS
        double tps = meterRegistry.find("url.tps").gauge().value();
        metrics.put("tps", String.format("%.2f", tps));

        // 총 요청 수
        if (redirectTimer != null) {
            metrics.put("totalRequests", redirectTimer.count());
        }

        // 오류율
        double errors = meterRegistry.find("application.errors").counter().count();
        double errorRate = total > 0 ? errors / total : 0;
        metrics.put("errorRate", String.format("%.4f%%", errorRate * 100));

        // 데이터베이스 쿼리 수
        double dbQueries = meterRegistry.find("db.query.count").counter().count();
        metrics.put("dbQueryCount", dbQueries);

        // 활성 URL 개수
        double activeUrls = meterRegistry.find("urls.active.count").gauge().value();
        metrics.put("activeUrlCount", (long) activeUrls);

        return ResponseEntity.ok(metrics);
    }

    // 상세 메트릭 (지정된 기간의 시계열 데이터)
    @Hidden
    @GetMapping("/timeseries")
    @Operation(summary = "상세 메트릭 조회 (미구현)", description = "")
    public ResponseEntity<Map<String, Object>> getTimeseriesMetrics(
            @RequestParam(defaultValue = "5") int minutes) {
        // 실제 구현에서는 시계열 데이터를 수집하는 로직 추가
        // 이 예제에서는 간단하게 현재 값만 반환

        Map<String, Object> timeseriesData = new HashMap<>();
        // ... 시계열 데이터 수집 로직

        return ResponseEntity.ok(timeseriesData);
    }
}