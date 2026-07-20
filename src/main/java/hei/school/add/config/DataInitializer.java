package hei.school.add.config;

import hei.school.add.entity.Utilisateur;
import hei.school.add.entity.enums.RoleUtilisateur;
import hei.school.add.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crée un compte administrateur par défaut au tout premier démarrage, pour pouvoir se connecter et
 * créer les autres comptes via /api/auth/login.
 *
 * <p>Identifiants par défaut : admin / admin123 IMPORTANT : à changer immédiatement après le
 * premier déploiement en production.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
  private final UtilisateurRepository utilisateurRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    if (!utilisateurRepository.existsByNomUtilisateur("admin")) {
      Utilisateur admin =
          Utilisateur.builder()
              .nomUtilisateur("admin")
              .motDePasse(passwordEncoder.encode("admin123"))
              .nom("Administrateur")
              .role(RoleUtilisateur.ADMINISTRATEUR)
              .actif(true)
              .build();
      utilisateurRepository.save(admin);
      System.out.println(
          ">>> Compte admin créé par défaut (admin / admin123) - à changer en production !");
    }
  }
}
