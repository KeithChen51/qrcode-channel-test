package com.byd.qrcode.auth;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class AuthProperties {

    @Value("${auth.admin-username:admin}")
    private String adminUsername = "admin";

    @Value("${auth.admin-initial-password:}")
    private String adminInitialPassword = "";

    @Value("${auth.token-secret:}")
    private String tokenSecret = "";

    @Value("${auth.token-ttl-minutes:720}")
    private long tokenTtlMinutes = 720;
}
