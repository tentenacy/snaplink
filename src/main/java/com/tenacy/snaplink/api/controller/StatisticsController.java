package com.tenacy.snaplink.api.controller;

import com.tenacy.snaplink.api.dto.UrlResponse;
import com.tenacy.snaplink.api.dto.UrlStatsResponse;
import com.tenacy.snaplink.doc.ApiErrorCodeExample;
import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.exception.CommonErrorCode;
import com.tenacy.snaplink.service.StatisticsService;
import com.tenacy.snaplink.service.UrlService;
import com.tenacy.snaplink.util.DocumentationDescriptions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "2. 통계 API", description = DocumentationDescriptions.TAG_STATISTICS_API)
public class StatisticsController {
    private final UrlService urlService;
    private final StatisticsService statisticsService;

    @Value("${app.shorturl.domain}")
    private String domain;

    @GetMapping("/{shortCode}")
    @Operation(summary = "URL 통계", description = DocumentationDescriptions.OPERATION_GET_URL_STATS)
    @ApiResponses(@ApiResponse(responseCode = "200", description = "성공"))
    @ApiErrorCodeExample(CommonErrorCode._URL_NOT_FOUND)
    public ResponseEntity<UrlStatsResponse> getUrlStats(
            @Parameter(description = DocumentationDescriptions.PARAM_SHORT_CODE)
            @PathVariable String shortCode
    ) {

        Url url = urlService.getUrlByShortCode(shortCode);

        return ResponseEntity.ok(UrlStatsResponse.from(
                // 기본 정보
                UrlResponse.from(url, domain),
                // 클릭 통계
                statisticsService.getAllStats(shortCode),
                // 일별 트렌드 (최근 7일)
                statisticsService.getDailyTrend(shortCode, 7),
                // 브라우저별 통계
                statisticsService.getBrowserStats(shortCode),
                // 국가별 통계
                statisticsService.getCountryStats(shortCode)
        ));
    }
}