package com.epam.rd.autocode.assessment.appliances.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


@Component
public class SecurityEventsListener {
    private static final Logger securityLogger = LoggerFactory.getLogger("securityLogger");

    @EventListener
    public void onAuthSuccess(AuthenticationSuccessEvent ev) {
        var username = ((UserDetails)ev.getAuthentication().getPrincipal()).getUsername();
        securityLogger.info("✅ Login success for user “{}”", username);
    }

    @EventListener
    public void onAuthFail(AbstractAuthenticationFailureEvent ev) {
        var username = ev.getAuthentication().getName();
        securityLogger.warn("🔒 Login failure for user “{}”: {}", username, ev.getException().getMessage());
    }

    @EventListener
    public void onAccessDenied(AuthorizationFailureEvent ev) {
        securityLogger.warn("⛔ Access denied: {}", ev.getAccessDeniedException().getMessage());
    }
}
