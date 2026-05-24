package com.japantravel.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Component
public class CurrentUser {

    public AppUserPrincipal principalOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object p = auth.getPrincipal();
        if (p instanceof AppUserPrincipal a) return a;
        return null;
    }

    public AppUserPrincipal require() {
        AppUserPrincipal p = principalOrNull();
        if (p == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        return p;
    }

    public long userId() { return require().getId(); }

    public boolean isAdmin() {
        AppUserPrincipal p = principalOrNull();
        return p != null && "ADMIN".equals(p.getRole());
    }
}
