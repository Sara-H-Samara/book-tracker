package com.sara.booktracker.aspect;

import com.sara.booktracker.singleton.ActivityLog;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ActivityLogAspect {

    @AfterReturning("@annotation(logActivity)")
    public void recordActivity(LogActivity logActivity) {
        String username = currentUsername();
        ActivityLog.getInstance().record(username + ": " + logActivity.value());
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        return auth.getName();
    }
}