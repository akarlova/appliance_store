package com.epam.rd.autocode.assessment.appliances.config.security;

import com.epam.rd.autocode.assessment.appliances.service.security.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final LoginAttemptService loginAttemptService;
    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    public CustomAuthenticationFailureHandler(LoginAttemptService svc) {
        this.loginAttemptService = svc;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        log.error(">>> onAuthenticationFailure called, exception = {}", exception.getClass().getSimpleName());

        String login = request.getParameter("username");
        if (login != null) {
            loginAttemptService.loginFailed(login);
        }

        if (exception instanceof LockedException) {
            response.sendRedirect(request.getContextPath() + "/login?blocked");
        } else {
            response.sendRedirect(request.getContextPath() + "/login?error");
        }
    }
}
