package com.tenacy.snaplink.aspect;

import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class DatabaseMetricsAspect {
    private final Counter dbQueryCounter;

    // 리포지토리 메소드 실행 시 쿼리 카운트 증가
    @Around("execution(* com.tenacy.snaplink.domain.*Repository.*(..))")
    public Object countDatabaseQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        dbQueryCounter.increment();
        return joinPoint.proceed();
    }
}