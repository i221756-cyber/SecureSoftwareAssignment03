package edu.nu.owaspapivulnlab.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class JwtService {


    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.ttl-seconds}")
    private long ttlSeconds;

    // Task 7: JWT issuer and audience
    private static final String ISSUER = "owaspapivulnlab"; // Task 7: set a constant issuer
    private static final String AUDIENCE = "owaspapivulnlab-users"; // Task 7: set a constant audience

    // Task 7: Issue JWT with issuer and audience claims
    public String issue(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
    SecretKey key = new SecretKeySpec(secret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    return Jwts.builder()
        .setSubject(subject)
        .addClaims(claims)
        .setIssuer(ISSUER) // Task 7: set issuer
        .setAudience(AUDIENCE) // Task 7: set audience
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(now + ttlSeconds * 1000))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
    }

    // Task 7: Strictly validate signature, expiry, issuer, and audience
    public Claims validateAndParse(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .build()
                .parseClaimsJws(token)
                .getBody();
        // Expiry is checked by parseClaimsJws; signature is checked as well
        return claims;
    }
}
