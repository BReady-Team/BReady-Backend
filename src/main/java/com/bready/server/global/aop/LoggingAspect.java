package com.bready.server.global.aop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* com.bready.server..service..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        log.info("[START] {}.{}() args = {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                safeArgs(joinPoint)
        );
    }

    @After("execution(* com.bready.server..service..*(..))")
    public void logAfter(JoinPoint joinPoint) {
        log.info("[ END ] {}.{}()",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName()
        );
    }

    @AfterThrowing(
            pointcut = "execution(* com.bready.server..service..*(..))",
            throwing = "ex"
    )
    public void logException(JoinPoint joinPoint, Throwable ex) {
        log.error("[ERROR] {}.{}() => {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                ex.getMessage(),
                ex
        );
    }

    private String safeArgs(JoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .map(this::safeValue)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String safeValue(Object arg) {
        if (arg == null) return "null";
        if (arg instanceof MultipartFile file)
            return "MultipartFile(size=" + file.getSize() + ")";
        if (arg instanceof byte[] b)
            return "byte[](" + b.length + ")";
        if (arg instanceof HttpServletRequest)
            return "HttpServletRequest";
        if (arg instanceof HttpServletResponse)
            return "HttpServletResponse";
        if (arg instanceof BindingResult br)
            return "BindingResult(errors=" + br.getErrorCount() + ")";
        return arg.toString();
    }
}