package com.master.meta.service;

import com.master.meta.controller.AuthController;

import java.util.Map;

/**
 * @author Created by 11's papa on 2025/10/11
 */
public interface AuthenticationService {
    AuthController.AuthenticationResponse authenticate(AuthController.AuthenticationRequest request);

    AuthController.AuthenticationResponse refreshToken(Map<String, String> request);

    void logout();
}
