package com.tenacy.snaplink.service;

import com.tenacy.snaplink.api.dto.UrlCreationRequest;
import com.tenacy.snaplink.api.dto.UrlDto;
import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.domain.UrlRepository;
import com.tenacy.snaplink.exception.DuplicateShortCodeException;
import com.tenacy.snaplink.exception.UrlNotFoundException;
import com.tenacy.snaplink.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlRepository urlRepository;
    private final Base62Encoder encoder;

    @Value("${app.shorturl.domain}")
    private String domain;

    @Cacheable(value = "urls", key = "#shortCode", unless = "#result == null")
    public Url getUrlByShortCode(String shortCode) {
        return urlRepository.findActiveByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
    }

    @Transactional
    public UrlDto createShortUrl(UrlCreationRequest request) {
        // 커스텀 코드 처리
        String shortCode;
        if (StringUtils.hasText(request.getCustomCode())) {
            shortCode = request.getCustomCode();
            if (urlRepository.existsByShortCode(shortCode)) {
                throw new DuplicateShortCodeException(shortCode);
            }
        } else {
            // 자동 생성된 코드가 중복되지 않을 때까지 반복
            do {
                shortCode = encoder.encode(System.currentTimeMillis());
            } while (urlRepository.existsByShortCode(shortCode));
        }

        // 만료일 계산
        LocalDateTime expiresAt = null;
        if (request.getValidityInDays() != null && request.getValidityInDays() > 0) {
            expiresAt = LocalDateTime.now().plusDays(request.getValidityInDays());
        }

        // 엔티티 생성 및 저장
        Url url = new Url();
        url.setOriginalUrl(request.getOriginalUrl());
        url.setShortCode(shortCode);
        url.setCreatedAt(LocalDateTime.now());
        url.setExpiresAt(expiresAt);
        url.setClickCount(0L);
        url.setCustom(StringUtils.hasText(request.getCustomCode()));

        urlRepository.save(url);

        // 응답 DTO 변환
        return UrlDto.from(url, domain);
    }

    @Transactional
    @CachePut(value = "urls", key = "#shortCode")
    public Url incrementClickCount(String shortCode) {
        // 단일 쿼리로 클릭 카운트 증가
        urlRepository.incrementClickCount(shortCode);

        // 캐시 업데이트를 위해 업데이트된 엔티티 반환
        return getUrlByShortCode(shortCode);
    }
}