package com.master.meta.handle.security;

import com.master.meta.entity.SystemUser;
import com.mybatisflex.core.query.QueryChain;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return QueryChain.of(SystemUser.class).where(SystemUser::getName).eq(username).oneOpt()
                .map(user -> new CustomUserDetails(user, new ArrayList<>()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
