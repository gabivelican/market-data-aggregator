package unitbv.devops.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import unitbv.devops.configuration.SecretsConfiguration;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Provider pentru generare și validare JWT tokens (Compatibil JJWT 0.11.5)
 * Reads JWT secret from Docker Secrets or environment variables
 */
@Component
public class JwtTokenProvider {

    private final String jwtSecret;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret.file:/run/secrets/jwt_secret}") String jwtSecretFile,
            @Value("${jwt.secret:}") String jwtSecretEnv,
            @Value("${jwt.expiration:86400000}") long jwtExpirationMs) {

        // Read JWT secret from file first, then from env variable, then use default
        this.jwtSecret = SecretsConfiguration.readSecretFromFile(
            jwtSecretFile,
            jwtSecretEnv.isEmpty() ? "mySecretKeyForJWTTokenGenerationAndValidationShouldBeLongEnoughForHS512Algorithm" : jwtSecretEnv
        );
        this.jwtExpirationMs = jwtExpirationMs;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generează JWT token pentru un username
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrage username-ul din JWT token
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Validează JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}