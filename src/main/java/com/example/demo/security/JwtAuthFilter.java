package com.example.demo.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Intercepte chaque requête HTTP : si un en-tête "Authorization: Bearer <token>" valide est
 * présent, authentifie l'utilisateur correspondant pour la durée de la requête.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final CollecteoUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String entete = request.getHeader("Authorization");

    if (entete == null || !entete.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = entete.substring(7);

    try {
      String nomUtilisateur = jwtService.extraireNomUtilisateur(token);

      if (nomUtilisateur != null
          && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(nomUtilisateur);

        if (jwtService.estValide(token, userDetails)) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (JwtException | UsernameNotFoundException e) {
      // Token malformé, expiré, signature invalide, ou utilisateur supprimé depuis l'émission
      // du token : on laisse simplement la requête continuer sans authentification. Spring
      // Security se chargera de renvoyer 401/403 plus loin pour les endpoints protégés, au lieu
      // de faire planter le filtre avec une erreur 500 générique.
      log.debug("Token JWT invalide ({}) : {}", e.getClass().getSimpleName(), e.getMessage());
    }

    filterChain.doFilter(request, response);
  }
}
