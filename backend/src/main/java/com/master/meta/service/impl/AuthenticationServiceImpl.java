package com.master.meta.service.impl;

import com.master.meta.controller.AuthController;
import com.master.meta.handle.exception.RefreshTokenExpiredException;
import com.master.meta.handle.security.JwtProperties;
import com.master.meta.handle.security.JwtTokenProvider;
import com.master.meta.service.AuthenticationService;
import com.master.meta.utils.RedisService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final JwtProperties jwtProperties;

    public AuthenticationServiceImpl(AuthenticationManager authenticationManager,
                                     JwtTokenProvider jwtTokenProvider,
                                     RedisService redisService,
                                     JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisService = redisService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public AuthController.AuthenticationResponse authenticate(AuthController.AuthenticationRequest request) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authenticate);
        String accessToken = jwtTokenProvider.generateToken(request.username(), "access_token");
        String refreshToken = jwtTokenProvider.generateToken(request.username(), "refresh_token");
        redisService.store(request.username(), refreshToken, jwtProperties.getRefreshTokenValidity() / 1000);
        return new AuthController.AuthenticationResponse(accessToken, refreshToken);
    }

    @Override
    public AuthController.AuthenticationResponse refreshToken(Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String username = jwtTokenProvider.extractUsername(refreshToken);
        if (!validateRefreshToken(username, refreshToken)) {
            throw new RefreshTokenExpiredException("REFRESH_TOKEN_EXPIRED");
        }
        String accessToken = jwtTokenProvider.generateToken(username, "access_token");
        return new AuthController.AuthenticationResponse(accessToken, refreshToken);
    }

    private boolean validateRefreshToken(String username, String token) {
        String storedToken = redisService.get(username);
        return storedToken != null && storedToken.equals(token) && !jwtTokenProvider.isTokenExpired(token);
    }

    @Override
    public void logout() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        redisService.delete(name);
    }
}
