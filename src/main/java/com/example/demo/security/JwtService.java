package com.example.demo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
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
  private static final int LONGUEUR_MIN_SECRET = 32; // HS256 exige au moins 256 bits

  // Clé secrète de signature. OBLIGATOIRE — aucune valeur par défaut : un secret prévisible
  // codé en dur permettrait à quiconque lit le code source de forger des tokens JWT valides
  // pour n'importe quel utilisateur, y compris un administrateur. Voir .env.example.
  @Value("${JWT_SECRET}")
  private String secret;

  @Value("${JWT_EXPIRATION_MS:86400000}") // 24h par défaut
  private long expirationMs;

  /**
   * Vérifie au démarrage que le secret configuré est assez long pour HS256, plutôt que de découvrir
   * l'erreur (WeakKeyException, peu explicite) au moment de générer le premier token.
   */
  @PostConstruct
  void validerSecret() {
    if (secret.getBytes(StandardCharsets.UTF_8).length < LONGUEUR_MIN_SECRET) {
      throw new IllegalStateException(
          "JWT_SECRET doit contenir au moins "
              + LONGUEUR_MIN_SECRET
              + " caractères (HS256 exige une clé d'au moins 256 bits). "
              + "Génère-en un avec : openssl rand -base64 32");
    }
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
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
