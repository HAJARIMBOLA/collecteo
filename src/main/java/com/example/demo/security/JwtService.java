package com.example.demo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Génère et valide les tokens JWT utilisés pour authentifier les appels API après connexion (voir
 * AuthController.login()).
 */
@Service
public class JwtService {
  // Clé secrète de signature. À surcharger en production via la variable d'environnement JWT_SECRET
  // (doit faire au moins 32 caractères pour HS256).
  @Value("${JWT_SECRET:collecteo-secret-key-changeme-en-production-32chars}")
  private String secret;

  @Value("${JWT_EXPIRATION_MS:86400000}") // 24h par défaut
  private long expirationMs;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String genererToken(UserDetails userDetails) {
    Date maintenant = new Date();
    Date expiration = new Date(maintenant.getTime() + expirationMs);

    return Jwts.builder()
        .subject(userDetails.getUsername())
        .issuedAt(maintenant)
        .expiration(expiration)
        .signWith(getSigningKey())
        .compact();
  }

  public String extraireNomUtilisateur(String token) {
    return extraireClaim(token, io.jsonwebtoken.Claims::getSubject);
  }

  public boolean estValide(String token, UserDetails userDetails) {
    String nomUtilisateur = extraireNomUtilisateur(token);
    return nomUtilisateur.equals(userDetails.getUsername()) && !estExpire(token);
  }

  private boolean estExpire(String token) {
    return extraireClaim(token, io.jsonwebtoken.Claims::getExpiration).before(new Date());
  }

  private <T> T extraireClaim(String token, Function<io.jsonwebtoken.Claims, T> resolver) {
    io.jsonwebtoken.Claims claims =
        Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    return resolver.apply(claims);
  }
}
