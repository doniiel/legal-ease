package kz.legeal.ease.backend.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import kz.legeal.ease.backend.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtProperties props;

    private Key getAccessSigningKey() {
        return Keys.hmacShaKeyFor(props.getAccessSecret().getBytes());
    }

    private Key getRefreshSigningKey() {
        return Keys.hmacShaKeyFor(props.getRefreshSecret().getBytes());
    }

    public String generateAccessToken(UserDetails userDetails) {
        final var personDetails = (PersonDetails) userDetails;
        final var claims = new HashMap<String, Object>();
        claims.put("userId", personDetails.getUser().getId());
        claims.put("role", personDetails.getUserRole().getRole().getCode());
        return generateToken(claims, Duration.ofMinutes(props.getAccessExpMin()).toMillis(), getAccessSigningKey());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        final var personDetails = (PersonDetails) userDetails;
        final var claims = new HashMap<String, Object>();
        claims.put("userId", personDetails.getUser().getId());
        claims.put("type", "refresh");
        return generateToken(claims, Duration.ofMinutes(props.getRefreshExpMin()).toMillis(), getRefreshSigningKey());
    }

    private String generateToken(Map<String, Object> claims, long expiration, Key signingKey) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(props.getIssuer())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long extractUserId(String token, boolean isRefreshToken) {
        return extractClaim(token, claims -> claims.get("userId", Long.class), isRefreshToken);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class), false);
    }

    public Date extractExpiration(String token, boolean isRefreshToken) {
        return extractClaim(token, Claims::getExpiration, isRefreshToken);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, boolean isRefreshToken) {
        final Claims claims = extractAllClaims(token, isRefreshToken);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, boolean isRefreshToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(isRefreshToken ? getRefreshSigningKey() : getAccessSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT token: " + e.getMessage());
        }
    }

    private boolean isTokenExpired(String token, boolean isRefreshToken) {
        return extractExpiration(token, isRefreshToken).before(new Date());
    }

    public boolean isTokenValid(String token, PersonDetails userDetails, boolean isRefreshToken) {
        try {
            final Long userId = extractUserId(token, isRefreshToken);
            return (userId.equals(userDetails.getUser().getId()) && !isTokenExpired(token, isRefreshToken));
        } catch (JwtException e) {
            return false;
        }
    }
}
