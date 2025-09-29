package co.com.pragma.api.security;

import co.com.pragma.errores.ErrorJwtExpirado;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtilsAdapter {

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration.millis}")
    private Long jwtExpirationMillis;

    public SecretKey jwtSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Mono<String> getUsernameFromToken(String token) {
        log.info("Obteniendo usernme del token");
        return Mono.fromCallable(() -> Jwts.parser()
                .verifyWith(jwtSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
    }

    public Mono<Claims> getClaimsOutAutenticateToken(String token){
        return Mono.just(token)
                .flatMap(t -> Mono.just(Jwts.parser()
                        .setSigningKey(jwtSecretKey())
                        .build()
                        .parseClaimsJws(t)
                        .getBody()))
                .onErrorResume(e-> Mono.error(new ErrorJwtExpirado("Token expirado", Set.of(e.getMessage()))));
    }

    public Claims getClaimsFromToken(String token) {
        log.info("Obteniendo username del token");
        return Jwts.parser()
                .verifyWith(jwtSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaimsFromToken(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException ex) {
            log.error("The JWT token has expired: {}", ex.getMessage());
            return true;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            return true;
        }
    }

}
