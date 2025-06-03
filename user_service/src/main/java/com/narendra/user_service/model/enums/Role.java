package com.narendra.user_service.model.enums;

import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

public enum Role implements GrantedAuthority {
    ROLE_USER("USER", Set.of(
            "ACCOUNT_READ_SELF",
            "ACCOUNT_UPDATE_SELF",
            "ACCOUNT_DELETE_SELF"
    )),
    ROLE_ADMIN("ADMIN", Set.of(
            "ACCOUNT_CREATE",
            "ACCOUNT_READ",
            "ACCOUNT_UPDATE",
            "ACCOUNT_DELETE"
    ));

    private final String value;
    private final Set<String> permissions;

    Role(String value, Set<String> permissions) {
        this.value = value;
        this.permissions = permissions;
    }

    public String getValue() {
        return value;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public String getAuthority() {
        return name();
    }
}
