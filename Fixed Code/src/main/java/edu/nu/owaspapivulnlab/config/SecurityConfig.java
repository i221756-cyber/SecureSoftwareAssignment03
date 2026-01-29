package edu.nu.owaspapivulnlab.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;
import io.jsonwebtoken.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

@Configuration
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String secret;

    // Task 1: Password Security - BCrypt Password Encoder Bean
    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    // VULNERABILITY(API7 Security Misconfiguration): overly permissive CORS/CSRF and antMatchers order
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()); // APIs typically stateless; but add CSRF for state-changing in real apps
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.authorizeHttpRequests(reg -> reg
        .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
        // Task 2: Access Control - tighten access to API endpoints
        // Allow unauthenticated signup (POST /api/users) and auth endpoints only
        .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // Task 2: allow signup
        // Enforce role-based access for admin and user-management operations
        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Task 2: admin-only
        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN") // Task 2: only admins may delete users
        // Require authentication for all other API endpoints
        .requestMatchers("/api/**").authenticated() // Task 2: protect API surface
        .anyRequest().authenticated()
    );

        http.headers(h -> h.frameOptions(f -> f.disable())); // allow H2 console

        http.addFilterBefore(new JwtFilter(secret), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        // Task 5: Rate Limiting - apply rate limiting filter after authentication filter so we can rate-limit per-user when possible
        http.addFilterAfter(new RateLimitFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Minimal JWT filter (VULNERABILITY: weak validation - no audience, issuer checks; long TTL)
    static class JwtFilter extends OncePerRequestFilter {
    private final String secret;
    JwtFilter(String secret) { this.secret = secret; }
    // Task 7: Use same issuer/audience as JwtService
    private static final String ISSUER = "owaspapivulnlab";
    private static final String AUDIENCE = "owaspapivulnlab-users";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
        throws ServletException, IOException {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                try {
                    // Task 7: Strictly validate signature, expiry, issuer, and audience
                    Claims c = Jwts.parserBuilder()
                            .setSigningKey(secret.getBytes())
                            .requireIssuer(ISSUER)
                            .requireAudience(AUDIENCE)
                            .build()
                            .parseClaimsJws(token).getBody();
                    String user = c.getSubject();
                    String role = (String) c.get("role");
                    UsernamePasswordAuthenticationToken authn = new UsernamePasswordAuthenticationToken(user, null,
                            role != null ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)) : Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authn);
                } catch (JwtException e) {
                    // Task 7: Invalid JWT (signature, expiry, issuer, audience)
                    // Do not authenticate, continue as anonymous
                }
            }
            chain.doFilter(request, response);
        }
    }

    // Task 5: Rate Limiting - simple in-memory Bucket4j filter
    static class RateLimitFilter extends OncePerRequestFilter {
        // key -> bucket
        private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

        private Bucket createNewBucket() {
            // Task 5: Rate Limiting - tighter limit for authenticated users: 3 requests per minute
            Refill refill = Refill.greedy(3, Duration.ofMinutes(1));
            Bandwidth limit = Bandwidth.classic(3, refill);
            return Bucket4j.builder().addLimit(limit).build();
        }


        // Enforce rate limiting for every request (Task 5): per-JWT subject when available, otherwise per-IP
        private boolean isRateLimited(HttpServletRequest request) {
            return true; // always enforce
        }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
        throws ServletException, IOException {
            if (!isRateLimited(request)) {
                chain.doFilter(request, response);
                return;
            }

            // Prefer per-JWT/user key when authenticated, otherwise fallback to client IP
            String key;
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
                // Task 5: Use the authenticated subject (from JWT) as the key so limits are enforced per token/user
                key = "user:" + auth.getName();
            } else {
                // No JWT present (e.g., login attempts) - fallback to IP-based limiting
                key = "ip:" + request.getRemoteAddr();
            }

            Bucket bucket = buckets.computeIfAbsent(key, k -> createNewBucket());
            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                response.setStatus(429);
                response.getWriter().write("Too many requests");
            }
        }
    }
}
