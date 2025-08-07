package com.tenacy.snaplink.cache;

import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.domain.UrlRepository;
import com.tenacy.snaplink.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@SpringBootTest
@Testcontainers // Testcontainers 활성화
@ActiveProfiles("test")
public class RedisCachingIntegrationTest {
    // 컨테이너 인스턴스를 정의하고 관리하는 필드에 적용
    // GenericContainer -> 모든 Docker 이미지를 실행할 수 있는 범용 컨테이너
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0")
            .withExposedPorts(6379);

    // 컨테이너의 동적 속성(호스트, 포트)을 Spring 환경에 등록
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private UrlService urlService;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void testCachingBehavior() {
        // given
        Url url = new Url();
        url.setOriginalUrl("https://example.com");
        url.setShortCode("test123");
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0L);

        urlRepository.save(url);

        // when & then
        // 첫 번째 호출 - 캐시 미스
        Url firstCall = urlService.getUrlByShortCode("test123");
        assertNotNull(firstCall);

        // 두 번째 호출 - 캐시 히트
        Url secondCall = urlService.getUrlByShortCode("test123");
        assertNotNull(secondCall);

        // DB 접근 횟수 검증을 위해 모킹된 리포지토리를 사용해야 함
        // 실제 테스트에서는 Repository를 Spy로 래핑하여 호출 횟수 검증

        // 캐시에 저장되었는지 확인
        Cache cache = cacheManager.getCache("urls");
        assertNotNull(cache);
        assertNotNull(cache.get("test123"));
    }
}