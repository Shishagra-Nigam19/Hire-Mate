package com.hiremate.module.audit.aspect;

import com.hiremate.module.audit.annotation.Audit;
import com.hiremate.module.audit.service.AuditLogService;
import com.hiremate.security.services.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @AfterReturning(pointcut = "@annotation(auditAnnotation)", returning = "result")
    public void auditMethodExecution(JoinPoint joinPoint, Audit auditAnnotation, Object result) {
        try {
            Long userId = null;
            String userEmail = "system";

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal principal) {
                userId = principal.getId();
                userEmail = principal.getEmail();
            }

            String ipAddress = "N/A";
            String userAgent = "N/A";

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = getClientIp(request);
                userAgent = request.getHeader("User-Agent");
            }

            auditLogService.logEvent(
                    userId,
                    userEmail,
                    auditAnnotation.action(),
                    auditAnnotation.entityType(),
                    null,
                    ipAddress,
                    userAgent,
                    "Executed method: " + joinPoint.getSignature().getName()
            );
        } catch (Exception ex) {
            log.error("Failed in AuditAspect", ex);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
