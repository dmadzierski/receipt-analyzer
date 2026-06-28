package pl.madzierski.daniel.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static String getCurrentUserSub() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
