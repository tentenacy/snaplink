package com.tenacy.snaplink.api.dto;

import com.tenacy.snaplink.util.DocumentationDescriptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@Schema(description = "시스템 메트릭 응답 DTO")
public class MetricsResponse {

    @Schema(description = DocumentationDescriptions.RESPONSE_CACHE_HIT_RATIO, example = DocumentationDescriptions.EXAMPLE_CACHE_HIT_RATIO)
    private String cacheHitRatio;

    @Schema(description = DocumentationDescriptions.RESPONSE_AVG_RESPONSE_TIME, example = DocumentationDescriptions.EXAMPLE_AVG_RESPONSE_TIME)
    private String avgResponseTime;

    @Schema(description = DocumentationDescriptions.RESPONSE_P95_RESPONSE_TIME, example = DocumentationDescriptions.EXAMPLE_P95_RESPONSE_TIME)
    private String p95ResponseTime;

    @Schema(description = DocumentationDescriptions.RESPONSE_P99_RESPONSE_TIME, example = DocumentationDescriptions.EXAMPLE_P99_RESPONSE_TIME)
    private String p99ResponseTime;

    @Schema(description = DocumentationDescriptions.RESPONSE_TPS, example = DocumentationDescriptions.EXAMPLE_TPS)
    private String tps;

    @Schema(description = DocumentationDescriptions.RESPONSE_TOTAL_REQUESTS, example = DocumentationDescriptions.EXAMPLE_TOTAL_REQUESTS)
    private Long totalRequests;

    @Schema(description = DocumentationDescriptions.RESPONSE_ERROR_RATE, example = DocumentationDescriptions.EXAMPLE_ERROR_RATE)
    private String errorRate;

    @Schema(description = DocumentationDescriptions.RESPONSE_DB_QUERY_COUNT, example = DocumentationDescriptions.EXAMPLE_DB_QUERY_COUNT)
    private Double dbQueryCount;

    @Schema(description = DocumentationDescriptions.RESPONSE_ACTIVE_URL_COUNT, example = DocumentationDescriptions.EXAMPLE_ACTIVE_URL_COUNT)
    private Long activeUrlCount;

    // 정적 팩토리 메서드
    public static MetricsResponse from(Map<String, Object> metricsMap) {
        return MetricsResponse.builder()
                .cacheHitRatio((String) metricsMap.get("cacheHitRatio"))
                .avgResponseTime((String) metricsMap.get("avgResponseTime"))
                .p95ResponseTime((String) metricsMap.get("p95ResponseTime"))
                .p99ResponseTime((String) metricsMap.get("p99ResponseTime"))
                .tps((String) metricsMap.get("tps"))
                .totalRequests((Long) metricsMap.get("totalRequests"))
                .errorRate((String) metricsMap.get("errorRate"))
                .dbQueryCount((Double) metricsMap.get("dbQueryCount"))
                .activeUrlCount((Long) metricsMap.get("activeUrlCount"))
                .build();
    }
}