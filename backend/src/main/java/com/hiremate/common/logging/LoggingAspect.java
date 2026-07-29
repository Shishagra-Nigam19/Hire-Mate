package com.hiremate.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerPointcut() {}

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void servicePointcut() {}

    @Around("controllerPointcut() || servicePointcut()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();

        log.debug("Entering [{}.{}]", className, methodName);

        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;

            if (elapsedTime > 500) {
                log.warn("SLOW EXECUTION DETECTED: [{}.{}] took {} ms", className, methodName, elapsedTime);
            } else {
                log.debug("Exiting [{}.{}] - Executed in {} ms", className, methodName, elapsedTime);
            }

            return result;
        } catch (Throwable t) {
            long elapsedTime = System.currentTimeMillis() - start;
            log.error("Exception in [{}.{}] after {} ms - Error: {}", className, methodName, elapsedTime, t.getMessage());
            throw t;
        }
    }
}
