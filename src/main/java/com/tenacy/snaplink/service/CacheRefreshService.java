package com.tenacy.snaplink.service;

import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.domain.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CacheRefreshService {
    private final UrlRepository urlRepository;
    private final CacheManager cacheManager;

    // 자주 접근되는 URL을 캐시에 미리 로드
    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void preloadHotUrls() {
        // 가장 많이 클릭된 상위 100개 URL 조회
        List<Url> hotUrls = urlRepository.findTop100ByOrderByClickCountDesc();

        Cache cache = cacheManager.getCache("urls");
        if (cache != null) {
            hotUrls.forEach(url ->
                    cache.put(url.getShortCode(), url)
            );
        }
    }

    // 만료된 URL 캐시 제거
    @Scheduled(fixedRate = 86400000) // 24시간마다 실행
    public void evictExpiredUrls() {
        List<Url> expiredUrls = urlRepository.findByExpiresAtBefore(LocalDateTime.now());

        Cache cache = cacheManager.getCache("urls");
        if (cache != null) {
            expiredUrls.forEach(url ->
                    cache.evict(url.getShortCode())
            );
        }
    }
}