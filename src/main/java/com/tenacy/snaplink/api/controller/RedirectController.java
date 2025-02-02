package com.tenacy.snaplink.api.controller;

import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.exception.UrlExpiredException;
import com.tenacy.snaplink.service.ClickTrackingService;
import com.tenacy.snaplink.service.UrlService;
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
    private final UrlService urlService;
    private final ClickTrackingService clickTrackingService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortCode, HttpServletRequest request) { // HttpServletRequest 파라미터 추가 필요
        Url url = urlService.getUrlByShortCode(shortCode);

        // 만료 검사
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException(shortCode);
        }

        // 클릭 추적 및 통계 업데이트 (추후 비동기 처리)
        clickTrackingService.trackClick(shortCode, request);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url.getOriginalUrl()))
                .build();
    }
}