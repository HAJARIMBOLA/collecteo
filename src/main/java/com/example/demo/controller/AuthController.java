package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.entity.Utilisateur;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UtilisateurRepository;
import com.example.demo.security.CollecteoUserDetails;
import com.example.demo.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentification : POST /api/auth/login -> renvoie un token JWT à utiliser dans l'en-tête
 * Authorization.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UtilisateurRepository utilisateurRepository;

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody AuthRequest requete) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              requete.getNomUtilisateur(), requete.getMotDePasse()));
    } catch (Exception e) {
      throw new BadCredentialsException("Nom d'utilisateur ou mot de passe incorrect");
    }

    Utilisateur utilisateur =
        utilisateurRepository
            .findByNomUtilisateur(requete.getNomUtilisateur())
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

    UserDetails userDetails = new CollecteoUserDetails(utilisateur);
    String token = jwtService.genererToken(userDetails);

    return new AuthResponse(
        token, utilisateur.getNomUtilisateur(), utilisateur.getNom(), utilisateur.getRole());
  }
}
