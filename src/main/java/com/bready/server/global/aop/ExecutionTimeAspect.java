package com.bready.server.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExecutionTimeAspect {

    @Around("execution(* com.bready.server..controller..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;
        log.info("[API 실행 시간] {}ms - {}.{}",
                executionTime,
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName()
        );

        return result;
    }
}
