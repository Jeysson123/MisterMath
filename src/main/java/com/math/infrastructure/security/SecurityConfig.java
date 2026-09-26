package com.math.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Reglas de seguridad de la API.
 *
 * <pre>
 *  POST /api/v1/auth/login      → público (aquí se obtiene el token)
 *  GET  /actuator/health        → público (healthcheck de Docker)
 *  todo lo demás                → requiere Authorization: Bearer &lt;token&gt;
 * </pre>
 *
 * <p>Es <b>stateless</b>: no hay sesión ni cookies, así que CSRF se desactiva. Cada request
 * se autentica solo con su token.</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Cadena de filtros de Spring Security.
     *
     * @param http          configurador de Spring Security
     * @param tokenProvider validador de tokens para el filtro JWT
     * @param errorHandler  escribe los 401/403 en formato {@code ApiResponse}
     * @return la cadena configurada
     * @throws Exception si la configuración falla
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenProvider tokenProvider,
                                                   JsonSecurityErrorHandler errorHandler) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * @return codificador BCrypt para las contraseñas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Usuario en memoria tomado de {@link UserProperties}. Suficiente para aprender JWT sin
     * montar una tabla de usuarios.
     *
     * @param properties     usuario y contraseña configurados
     * @param passwordEncoder codificador de contraseñas
     * @return el servicio de usuarios
     */
    @Bean
    public UserDetailsService userDetailsService(UserProperties properties, PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(User.withUsername(properties.username())
                .password(passwordEncoder.encode(properties.password()))
                .roles("USER")
                .build());
    }

    /**
     * Expone el {@link AuthenticationManager} que Spring arma con el {@code UserDetailsService}
     * y el {@code PasswordEncoder} anteriores; lo usa el login.
     *
     * @param configuration configuración de autenticación de Spring
     * @return el manager
     * @throws Exception si no se puede construir
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
