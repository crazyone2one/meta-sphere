package com.master.meta.handle.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private long accessTokenValidity;
    private long refreshTokenValidity;
    private String secret;
}
