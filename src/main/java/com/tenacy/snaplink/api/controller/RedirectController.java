package com.tenacy.snaplink.api.controller;

import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.exception.UrlExpiredException;
import com.tenacy.snaplink.service.ClickTrackingService;
import com.tenacy.snaplink.service.MetricsUrlService;
import com.tenacy.snaplink.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.time.LocalDateTime;

@Controller
public class RedirectController {
    private final UrlService urlService;
    private final ClickTrackingService clickTrackingService;
    private final MetricsUrlService metricsService;

    @Autowired
    public RedirectController(@Qualifier("originalUrlService") UrlService urlService,
                              ClickTrackingService clickTrackingService, MetricsUrlService  metricsService) {
        this.urlService = urlService;
        this.clickTrackingService = clickTrackingService;
        this.metricsService = metricsService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortCode, HttpServletRequest request) {
        // 타이머로 전체 요청 시간 측정
        return metricsService.recordTime("url.total.redirect.time", () -> {
            Url url = urlService.getUrlByShortCode(shortCode);

            // 만료 검사
            if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new UrlExpiredException(shortCode);
            }

            // 클릭 추적 (비동기로 처리하는 것이 좋음)
            clickTrackingService.trackClick(shortCode, request);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(url.getOriginalUrl()))
                    .build();
        });
    }
}