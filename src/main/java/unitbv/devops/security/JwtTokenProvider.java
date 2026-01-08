package unitbv.devops.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Provider pentru generare și validare JWT tokens (Compatibil JJWT 0.11.5)
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationAndValidation12345678}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generează JWT token pentru un username
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)       // MODIFICAT: subject -> setSubject
                .setIssuedAt(new Date())    // MODIFICAT: issuedAt -> setIssuedAt
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // MODIFICAT: expiration -> setExpiration
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrage username-ul din JWT token
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // MODIFICAT: verifyingKey -> setSigningKey
                    .build()
                    .parseClaimsJws(token)
                    .getBody(); // MODIFICAT: getPayload -> getBody (specific ver 0.11.x)
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
                    .setSigningKey(getSigningKey()) // MODIFICAT: verifyingKey -> setSigningKey
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}