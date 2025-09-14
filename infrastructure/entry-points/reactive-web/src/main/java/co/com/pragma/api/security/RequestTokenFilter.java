package co.com.pragma.api.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestTokenFilter implements WebFilter {

    private final JwtAuthenticateManager jwtAuthenticateManager;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String authToken = authHeader.substring(7);

        log.info("Inicia validacion y autenticacion en el contexto de spring");
        return Mono.just(new UsernamePasswordAuthenticationToken(authToken, authToken))
                .flatMap(jwtAuthenticateManager::authenticate)
                .flatMap(authentication -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .onErrorResume(e -> {
                    log.error("Authentication failed: {}", e.getMessage());
                    return chain.filter(exchange);
                });
    }

}
