package com.math.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesValidBearerTokens() throws Exception {
        when(tokenProvider.validate("good")).thenReturn(Optional.of("admin"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good");
        MockFilterChain chain = new MockFilterChain();

        new JwtAuthenticationFilter(tokenProvider).doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("admin");
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void ignoresInvalidTokens() throws Exception {
        when(tokenProvider.validate("bad")).thenReturn(Optional.empty());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad");

        new JwtAuthenticationFilter(tokenProvider).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void ignoresRequestsWithoutBearerHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc");

        new JwtAuthenticationFilter(tokenProvider).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        verifyNoInteractions(tokenProvider);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
