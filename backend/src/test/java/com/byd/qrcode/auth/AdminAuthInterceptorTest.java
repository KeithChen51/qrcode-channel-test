package com.byd.qrcode.auth;

import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthInterceptorTest {

    @Mock
    private AdminAuthService authService;

    @AfterEach
    void tearDown() {
        AdminUserContext.clear();
    }

    @Test
    void blocksAdminApiWithoutBearerToken() throws Exception {
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(authService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/qrcodes");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void allowsPublicQrImageWithoutBearerToken() throws Exception {
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(authService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/qrcodes/1/image");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void acceptsValidBearerToken() throws Exception {
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(authService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/qrcodes");
        request.addHeader("Authorization", "Bearer token-value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authService.verifyToken("token-value"))
                .thenReturn(new AdminPrincipal("admin", false, 100L, 200L));

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        assertEquals("admin", AdminUserContext.current().username());
    }

    @Test
    void acceptsValidAuthCookie() throws Exception {
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(authService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/qrcodes");
        request.setCookies(new MockCookie("qrcode_admin_token", "cookie-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authService.verifyToken("cookie-token"))
                .thenReturn(new AdminPrincipal("admin", false, 100L, 200L));

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        assertEquals("admin", AdminUserContext.current().username());
    }

    @Test
    void blocksAdminApiWhenInitialPasswordMustBeChanged() throws Exception {
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(authService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/qrcodes");
        request.addHeader("Authorization", "Bearer token-value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authService.verifyToken("token-value"))
                .thenReturn(new AdminPrincipal("admin", true, 100L, 200L));

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }
}
