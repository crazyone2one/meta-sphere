package com.master.meta.handle.filter;

import com.master.meta.handle.security.CustomUserDetailsService;
import com.master.meta.handle.security.JwtTokenProvider;
import com.master.meta.handle.security.RestAuthenticationEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                                   HandlerExceptionResolver handlerExceptionResolver,
                                   JwtTokenProvider jwtTokenProvider,
                                   CustomUserDetailsService customUserDetailsService) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer")) {
                filterChain.doFilter(request, response);
                return;
            }
            final String jwt = authHeader.substring(7);
            String username = jwtTokenProvider.extractUsername(jwt);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (StringUtils.isNoneBlank(username) && Objects.isNull(authentication)) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            filterChain.doFilter(request, response);
        } catch (io.jsonwebtoken.JwtException exception) {
            // JWT 相关异常应该导致认证失败，而不是服务器错误
            AuthenticationException authException = new BadCredentialsException("Token is invalid", exception);
            SecurityContextHolder.clearContext();
            restAuthenticationEntryPoint.commence(request, response, authException);
        } catch (Exception exception) {
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().equals("/auth/refresh-token");
    }
}
