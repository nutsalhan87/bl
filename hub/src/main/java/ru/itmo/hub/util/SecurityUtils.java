package ru.itmo.hub.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClaimAccessor;
import ru.itmo.hub.exception.ServiceException;

public class SecurityUtils {
    public static String getUsername(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            throw ServiceException.gettingUnauthorizedUsername();
        }
        var claimAccessor = (ClaimAccessor) authentication.getPrincipal();
        return claimAccessor.getClaim("preferred_username");
    }

    public static boolean isAuthenticated(Authentication authentication) {
        return authentication != null;
    }
}
