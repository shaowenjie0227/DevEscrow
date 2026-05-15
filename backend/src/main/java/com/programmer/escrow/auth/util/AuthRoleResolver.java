package com.programmer.escrow.auth.util;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class AuthRoleResolver {

    private AuthRoleResolver() {
    }

    public static List<String> resolveRoles(Integer userType, Integer developerStatus) {
        if (userType == null) {
            return List.of();
        }
        List<String> roles = new ArrayList<>();
        roles.add("CLIENT");
        if (developerStatus != null && developerStatus == 2) {
            roles.add("DEVELOPER");
        }
        return roles;
    }

    public static Collection<SimpleGrantedAuthority> resolveAuthorities(List<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    public static String resolveRedirectPath() {
        return "/me";
    }
}
