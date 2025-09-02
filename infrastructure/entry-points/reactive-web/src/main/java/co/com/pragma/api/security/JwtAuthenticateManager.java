package co.com.pragma.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticateManager implements ReactiveAuthenticationManager {

    private final JwtUtilsAdapter jwtUtilsAdapter;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return jwtUtilsAdapter.getClaimsOutAutenticateToken(String.valueOf(authentication.getCredentials()))
                .map(claims -> {
                    String username = claims.getSubject();
                    Integer rolId = claims.get("rolId", Integer.class);
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + rolId)
                    );
                    return new UsernamePasswordAuthenticationToken(username, null, authorities);
                });
    }

}
