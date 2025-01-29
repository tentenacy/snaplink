package com.tenacy.snaplink.api.controller;

import com.tenacy.snaplink.api.dto.UrlCreationRequest;
import com.tenacy.snaplink.api.dto.UrlDto;
import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;

    @Value("${app.shorturl.domain}")
    private String domain;

    @PostMapping("/shorten")
    public ResponseEntity<UrlDto> createShortUrl(@Valid @RequestBody UrlCreationRequest request) {
        UrlDto urlDto = urlService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(urlDto);
    }

    @GetMapping("/urls/{shortCode}")
    public ResponseEntity<UrlDto> getUrlInfo(@PathVariable String shortCode) {
        Url url = urlService.getUrlByShortCode(shortCode);
        return ResponseEntity.ok(UrlDto.from(url, domain));
    }
}