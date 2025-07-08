package com.epam.rd.autocode.assessment.appliances.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingServices {
    private static final Logger businessLogger = LoggerFactory.getLogger("businessLogger");

    @Pointcut("@annotation(com.epam.rd.autocode.assessment.appliances.aspect.Loggable)")
    public void loggableMethods() {}

    @Before("loggableMethods()")
    public void logBefore(JoinPoint jp) {
        businessLogger.debug("⏳ Entering: {}.{}()",
                jp.getSignature().getDeclaringTypeName(),
                jp.getSignature().getName());
    }

    @AfterReturning(pointcut = "loggableMethods()", returning = "result")
    public void logAfter(JoinPoint jp, Object result) {
        businessLogger.debug("✅ Exiting: {}.{}() → {}",
                jp.getSignature().getDeclaringTypeName(),
                jp.getSignature().getName(),
                result);
    }

    @AfterThrowing(pointcut = "loggableMethods()", throwing = "ex")
    public void logException(JoinPoint jp, Throwable ex) {
        businessLogger.error("❌ Exception in {}.{}(): {}",
                jp.getSignature().getDeclaringTypeName(),
                jp.getSignature().getName(),
                ex.getMessage(), ex);
    }
}
