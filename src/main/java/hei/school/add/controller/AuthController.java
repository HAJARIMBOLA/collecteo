package hei.school.add.controller;

import hei.school.add.dto.AuthRequest;
import hei.school.add.dto.AuthResponse;
import hei.school.add.entity.Utilisateur;
import hei.school.add.exception.ResourceNotFoundException;
import hei.school.add.repository.UtilisateurRepository;
import hei.school.add.security.CollecteoUserDetails;
import hei.school.add.security.JwtService;
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
