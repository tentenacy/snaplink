package com.tenacy.snaplink.config;

import com.tenacy.snaplink.domain.UrlRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.LocalDateTime;

@Configuration
public class MetricsConfig {

    // 공통 태그 설정
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("application", "snaplink")
                .meterFilter(MeterFilter.acceptNameStartsWith("url"))
                .meterFilter(MeterFilter.acceptNameStartsWith("cache"))
                .meterFilter(MeterFilter.acceptNameStartsWith("http"));
    }

    // 캐시 히트 카운터
    @Bean
    public Counter cacheHitCounter(MeterRegistry registry) {
        return Counter.builder("cache.hits")
                .description("캐시 히트 횟수")
                .tag("type", "url")
                .register(registry);
    }

    // 캐시 미스 카운터
    @Bean
    public Counter cacheMissCounter(MeterRegistry registry) {
        return Counter.builder("cache.misses")
                .description("캐시 미스 횟수")
                .tag("type", "url")
                .register(registry);
    }

    // URL 리다이렉트 타이머
    @Bean
    public Timer urlRedirectTimer(MeterRegistry registry) {
        return Timer.builder("url.redirect.time")
                .description("URL 리다이렉트 응답 시간")
                .publishPercentiles(0.5, 0.95, 0.99) // 중앙값, 95%, 99% 백분위수
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(2))
                .register(registry);
    }

    // URL 생성 타이머
    @Bean
    public Timer urlCreationTimer(MeterRegistry registry) {
        return Timer.builder("url.creation.time")
                .description("URL 단축 생성 시간")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    // 오류율 카운터
    @Bean
    public Counter errorCounter(MeterRegistry registry) {
        return Counter.builder("application.errors")
                .description("애플리케이션 오류 발생 횟수")
                .tag("type", "url")
                .register(registry);
    }

    // 데이터베이스 쿼리 카운터
    @Bean
    public Counter dbQueryCounter(MeterRegistry registry) {
        return Counter.builder("db.query.count")
                .description("데이터베이스 쿼리 실행 횟수")
                .register(registry);
    }

    // 커스텀 게이지: 활성 URL 개수
    @Bean
    public MeterBinder activeUrlsGauge(UrlRepository urlRepository) {
        return registry -> Gauge.builder("urls.active.count", urlRepository, repo ->
                        repo.countByExpiresAtAfterOrExpiresAtIsNull(LocalDateTime.now()))
                .description("유효한 URL 개수")
                .register(registry);
    }
}