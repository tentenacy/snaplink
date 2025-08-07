package com.tenacy.snaplink.service;

import com.tenacy.snaplink.api.dto.UrlCreationRequest;
import com.tenacy.snaplink.api.dto.UrlDto;
import com.tenacy.snaplink.domain.Url;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@Primary
public class MetricsUrlService implements UrlServiceInterface {
    private final UrlService delegate;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Timer urlRedirectTimer;
    private final Timer urlCreationTimer;
    private final Counter errorCounter;
    private final MeterRegistry meterRegistry;

    @Autowired
    public MetricsUrlService(@Qualifier("originalUrlService") UrlService delegate,
                             Counter cacheHitCounter, Counter cacheMissCounter, Timer urlRedirectTimer,
                             Timer urlCreationTimer, Counter errorCounter, MeterRegistry meterRegistry) {
        this.delegate = delegate;
        this.cacheHitCounter = cacheHitCounter;
        this.cacheMissCounter = cacheMissCounter;
        this.urlRedirectTimer = urlRedirectTimer;
        this.urlCreationTimer = urlCreationTimer;
        this.errorCounter = errorCounter;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Url getUrlByShortCode(String shortCode) {
        try {
            Timer.Sample sample = Timer.start(meterRegistry);

            // 캐시 히트/미스 추적을 위한 플래그
            boolean[] cacheHit = {false};

            // 서비스 호출과 캐시 히트 여부 추적
            Url result = Timer.builder("url.lookup.time")
                    .description("URL 조회 시간")
                    .tag("shortCode", shortCode)
                    .register(meterRegistry)
                    .record(() -> {
                        try {
                            // 실제 URL 조회 로직
                            Url url = delegate.getUrlByShortCode(shortCode);

                            // 캐시 히트 여부 확인 (구현에 따라 달라질 수 있음)
                            // 여기서는 응답 시간이 10ms 미만이면 캐시 히트로 가정
                            if (sample.stop(Timer.builder("temp").register(meterRegistry)) < TimeUnit.MILLISECONDS.toNanos(10)) {
                                cacheHitCounter.increment();
                                cacheHit[0] = true;
                            } else {
                                cacheMissCounter.increment();
                            }

                            return url;
                        } catch (Exception e) {
                            errorCounter.increment();
                            throw e;
                        }
                    });

            // 캐시 히트율 업데이트
            updateCacheHitRatio();

            return result;
        } catch (Exception e) {
            errorCounter.increment();
            throw e;
        }
    }

    @Override
    public UrlDto createShortUrl(UrlCreationRequest request) {
        try {
            return urlCreationTimer.record(() -> {
                try {
                    return delegate.createShortUrl(request);
                } catch (Exception e) {
                    errorCounter.increment();
                    throw e;
                }
            });
        } catch (Exception e) {
            errorCounter.increment();
            throw e;
        }
    }

    @Override
    public Url incrementClickCount(String shortCode) {
        try {
            return delegate.incrementClickCount(shortCode);
        } catch (Exception e) {
            errorCounter.increment();
            throw e;
        }
    }

    // URL 리다이렉트 시간 측정
    public void recordRedirectTime(Runnable operation) {
        urlRedirectTimer.record(operation);
    }

    // 타이머로 감싸서 작업 수행 및 시간 측정
    public <T> T recordTime(String timerName, Supplier<T> operation) {
        return Timer.builder(timerName)
                .register(meterRegistry)
                .record(operation);
    }

    // 캐시 히트율 측정을 위한 게이지 설정
    @PostConstruct
    public void setupCacheHitRatioGauge() {
        Gauge.builder("cache.hit.ratio", this, service -> calculateCacheHitRatio())
                .description("캐시 히트율")
                .register(meterRegistry);
    }

    // TPS 측정을 위한 게이지 설정
    @PostConstruct
    public void setupTpsGauge() {
        Gauge.builder("url.tps", meterRegistry, registry ->
                        registry.find("url.redirect.time").timer().count() / 60.0)
                .description("초당 트랜잭션 수 (1분 기준)")
                .register(meterRegistry);
    }

    // 캐시 히트율 계산
    private double calculateCacheHitRatio() {
        double hits = meterRegistry.find("cache.hits").counter().count();
        double misses = meterRegistry.find("cache.misses").counter().count();
        double total = hits + misses;
        return total > 0 ? hits / total : 0;
    }

    // 캐시 히트율 업데이트
    private void updateCacheHitRatio() {
        calculateCacheHitRatio(); // 게이지 업데이트 트리거
    }
}