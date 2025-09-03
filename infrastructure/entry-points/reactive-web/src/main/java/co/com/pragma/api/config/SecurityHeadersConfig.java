package co.com.pragma.api.config;

import co.com.pragma.api.handlers.AccessDeniedHandler;
import co.com.pragma.api.handlers.AuthenticationEntryPoint;
import co.com.pragma.api.security.JwtAuthenticateManager;
import co.com.pragma.api.security.RequestTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityHeadersConfig /*implements WebFilter*/ {

    private final String PATH_SWAGGER = "/swagger-ui/**";
    private final String PATH_API_DOCS = "/v3/api-docs/**";

    private final RequestTokenFilter requestTokenFilter;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticateManager jwtAuthenticateManager;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authenticationManager(jwtAuthenticateManager)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                PATH_SWAGGER,
                                PATH_API_DOCS
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(requestTokenFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
}
