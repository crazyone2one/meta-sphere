package com.master.meta.handle.security;

import com.master.meta.entity.SystemUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/11
 */
public record CustomUserDetails(SystemUser user, List<String> roles) implements UserDetails {
    public String getUserId() {
        return this.user.getId();
    }
    public String getUserProjectId() {
        return this.user.getLastProjectId();
    }

    public String getUserOrganizationId() {
        return this.user.getLastOrganizationId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
        for (String role : roles) {
            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role);
            simpleGrantedAuthorities.add(simpleGrantedAuthority);
        }
        return simpleGrantedAuthorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
