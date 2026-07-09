package com.gestproj.backend.config;

import com.gestproj.backend.common.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitInterceptorTest {

    @Test
    void preHandleShouldThrowRateLimitExceptionAfterLimit() {
        RateLimitInterceptor interceptor = new RateLimitInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 100; i++) {
            assertTrue(interceptor.preHandle(request, response, new Object()));
        }

        assertThrows(
                RateLimitExceededException.class,
                () -> interceptor.preHandle(request, response, new Object())
        );
    }
}
