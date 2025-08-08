package com.tenacy.snaplink.api.controller;

import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.exception.UrlExpiredException;
import com.tenacy.snaplink.service.ClickTrackingService;
import com.tenacy.snaplink.service.MetricsUrlService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class RedirectController {
    private final ClickTrackingService clickTrackingService;
    private final MetricsUrlService metricsService;

    @Hidden
    @GetMapping("/{shortCode}")
    @ApiResponses(@ApiResponse(responseCode = "302", description = "성공"))
//    @ApiErrorCodeExample({CommonErrorCode._URL_NOT_FOUND, CommonErrorCode._URL_EXPIRED})
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortCode, HttpServletRequest request) {
        // 타이머로 전체 요청 시간 측정
        return metricsService.recordTime("url.redirect.time", () -> {
            Url url = metricsService.getUrlByShortCode(shortCode);

            // 만료 검사
            if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw UrlExpiredException.EXCEPTION;
            }

            // 클릭 추적을 비동기로 처리
            clickTrackingService.trackClick(shortCode, request.getHeader("User-Agent"), getClientIp(request));

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(url.getOriginalUrl()))
                    .build();
        });
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
}