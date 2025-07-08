package com.epam.rd.autocode.assessment.appliances.service.security;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private final int MAX_ATTEMPT = 3;
    private final long LOCK_TIME_MINUTES = 15;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    private static class Attempt {
        int count;
        LocalDateTime blockTime;
    }

    public void loginSucceeded(String login) {
        attempts.remove(login);
    }

    public void loginFailed(String login) {
        Attempt a = attempts.getOrDefault(login, new Attempt());
        a.count++;
        if (a.count >= MAX_ATTEMPT) {
            a.blockTime = LocalDateTime.now();
        }
        attempts.put(login, a);
    }

    public boolean isBlocked(String login) {
        Attempt a = attempts.get(login);
        if (a == null || a.blockTime == null) return false;
        if (LocalDateTime.now().isAfter(a.blockTime.plusMinutes(LOCK_TIME_MINUTES))) {
            loginSucceeded(login);
            return false;
        }
        return true;
    }
}
