package com.byd.qrcode.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@Component
public class PublicUrlResolver {

    private final String configuredBaseUrl;

    public PublicUrlResolver(@Value("${app.base-url:}") String configuredBaseUrl) {
        this.configuredBaseUrl = normalizeBaseUrl(configuredBaseUrl);
    }

    public String buildJumpPageUrl(HttpServletRequest request, Integer qrcodeId) {
        return resolveBaseUrl(request) + "/jump?qid=" + qrcodeId;
    }

    public String buildQrcodeImageUrl(HttpServletRequest request, Integer qrcodeId) {
        return resolveBaseUrl(request) + "/api/qrcodes/" + qrcodeId + "/image";
    }

    public String resolveBaseUrl(HttpServletRequest request) {
        String requestBaseUrl = buildRequestBaseUrl(request);

        if (StringUtils.hasText(configuredBaseUrl) && (!isLocalBaseUrl(configuredBaseUrl) || !StringUtils.hasText(requestBaseUrl))) {
            return configuredBaseUrl;
        }
        if (StringUtils.hasText(requestBaseUrl)) {
            return requestBaseUrl;
        }
        if (StringUtils.hasText(configuredBaseUrl)) {
            return configuredBaseUrl;
        }
        return "";
    }

    private String buildRequestBaseUrl(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String forwarded = firstHeaderValue(request.getHeader("Forwarded"));
        String proto = firstNonBlank(
                forwardedValue(forwarded, "proto"),
                firstHeaderValue(request.getHeader("X-Forwarded-Proto")),
                request.getScheme());
        String forwardedHost = forwardedValue(forwarded, "host");
        String xForwardedHost = firstHeaderValue(request.getHeader("X-Forwarded-Host"));
        String hostHeader = firstHeaderValue(request.getHeader("Host"));
        String host = firstNonBlank(
                forwardedHost,
                xForwardedHost,
                hostHeader,
                request.getServerName());
        boolean hostFromRequest = !StringUtils.hasText(forwardedHost)
                && !StringUtils.hasText(xForwardedHost)
                && !StringUtils.hasText(hostHeader);

        if (!StringUtils.hasText(host)) {
            return "";
        }

        String port = firstHeaderValue(request.getHeader("X-Forwarded-Port"));
        if (!hostContainsPort(host) && StringUtils.hasText(port) && !isDefaultPort(proto, port)) {
            host = host + ":" + port;
        } else if (hostFromRequest && !hostContainsPort(host) && shouldAppendRequestPort(request, proto)) {
            host = host + ":" + request.getServerPort();
        }

        String contextPath = normalizeContextPath(request.getContextPath());
        return normalizeBaseUrl(proto + "://" + host + contextPath);
    }

    private String firstHeaderValue(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.split(",")[0].trim();
    }

    private String forwardedValue(String forwarded, String key) {
        if (!StringUtils.hasText(forwarded)) {
            return "";
        }
        for (String part : forwarded.split(";")) {
            String[] pair = part.trim().split("=", 2);
            if (pair.length == 2 && key.equalsIgnoreCase(pair[0].trim())) {
                return stripQuotes(pair[1].trim());
            }
        }
        return "";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean hostContainsPort(String host) {
        if (!StringUtils.hasText(host)) {
            return false;
        }
        if (host.startsWith("[")) {
            return host.contains("]:");
        }
        return host.indexOf(':') >= 0;
    }

    private boolean shouldAppendRequestPort(HttpServletRequest request, String proto) {
        int port = request.getServerPort();
        if (port <= 0) {
            return false;
        }
        return !isDefaultPort(proto, String.valueOf(port));
    }

    private boolean isDefaultPort(String proto, String port) {
        return ("http".equalsIgnoreCase(proto) && "80".equals(port))
                || ("https".equalsIgnoreCase(proto) && "443".equals(port));
    }

    private String normalizeContextPath(String contextPath) {
        if (!StringUtils.hasText(contextPath) || "/".equals(contextPath)) {
            return "";
        }
        return contextPath.startsWith("/") ? contextPath : "/" + contextPath;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "";
        }
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private boolean isLocalBaseUrl(String baseUrl) {
        try {
            String host = URI.create(baseUrl).getHost();
            return isLocalHost(host);
        } catch (Exception ignored) {
            return baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1");
        }
    }

    private boolean isLocalHost(String host) {
        if (!StringUtils.hasText(host)) {
            return false;
        }
        String normalized = host.trim().toLowerCase();
        return "localhost".equals(normalized)
                || "0.0.0.0".equals(normalized)
                || "127.0.0.1".equals(normalized)
                || "::1".equals(normalized)
                || normalized.startsWith("127.");
    }

    private String stripQuotes(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
