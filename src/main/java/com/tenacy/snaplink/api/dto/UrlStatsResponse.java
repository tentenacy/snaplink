package com.tenacy.snaplink.api.dto;

import com.tenacy.snaplink.util.DocumentationDescriptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@Schema(description = "URL 통계 응답 DTO")
public class UrlStatsResponse {

    @Schema
    private UrlResponse url;

    @Schema
    private ClickStats clicks;

    @Schema(description = DocumentationDescriptions.RESPONSE_DAILY_TREND, example = DocumentationDescriptions.EXAMPLE_DAILY_TREND)
    private Map<String, Long> dailyTrend;

    @Schema(description = DocumentationDescriptions.RESPONSE_BROWSERS, example = DocumentationDescriptions.EXAMPLE_BROWSERS)
    private Map<String, Long> browsers;

    @Schema(description = DocumentationDescriptions.RESPONSE_COUNTRIES, example = DocumentationDescriptions.EXAMPLE_COUNTRIES)
    private Map<String, Long> countries;

    // 내부 클래스로 클릭 통계 정의
    @Data
    @Builder
    @Schema(description = "클릭 통계 정보 DTO")
    public static class ClickStats {
        @Schema(description = DocumentationDescriptions.RESPONSE_CLICK_STATS_TOTAL_CLICKS, example = DocumentationDescriptions.EXAMPLE_CLICK_STATS_TOTAL_CLICKS)
        private Long totalClicks;

        @Schema(description = DocumentationDescriptions.RESPONSE_CLICK_STATS_DAILY_CLICKS, example = DocumentationDescriptions.EXAMPLE_CLICK_STATS_DAILY_CLICKS)
        private Long dailyClicks;
    }

    // Map 응답을 DTO로 변환하는 정적 팩토리 메서드
    public static UrlStatsResponse from(
            UrlResponse urlResponse,
            Map<String, Long> clicks,
            Map<String, Long> dailyTrend,
            Map<String, Long> browsers,
            Map<String, Long> countries) {

        return UrlStatsResponse.builder()
                .url(urlResponse)
                .clicks(ClickStats.builder()
                        .totalClicks(clicks.getOrDefault("totalClicks", 0L))
                        .dailyClicks(clicks.getOrDefault("dailyClicks", 0L))
                        .build())
                .dailyTrend(dailyTrend)
                .browsers(browsers)
                .countries(countries)
                .build();
    }
}