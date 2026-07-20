package hei.school.add.service;

import hei.school.add.dto.UtilisateurRequest;
import hei.school.add.entity.Utilisateur;
import hei.school.add.exception.BusinessException;
import hei.school.add.exception.ResourceNotFoundException;
import hei.school.add.repository.UtilisateurRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurService {
  private final UtilisateurRepository utilisateurRepository;
  private final PasswordEncoder passwordEncoder;

  public List<Utilisateur> listerTous() {
    return utilisateurRepository.findAll();
  }

  public Utilisateur trouverParId(Long id) {
    return utilisateurRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Utilisateur introuvable avec l'id " + id));
  }

  /**
   * Le mot de passe est haché avec BCrypt avant d'être stocké : jamais de mot de passe en clair en
   * base.
   */
  public Utilisateur creer(UtilisateurRequest requete) {
    if (utilisateurRepository.existsByNomUtilisateur(requete.getNomUtilisateur())) {
      throw new BusinessException("Ce nom d'utilisateur existe déjà");
    }
    Utilisateur utilisateur =
        Utilisateur.builder()
            .nomUtilisateur(requete.getNomUtilisateur())
            .motDePasse(passwordEncoder.encode(requete.getMotDePasse()))
            .nom(requete.getNom())
            .role(requete.getRole())
            .actif(true)
            .build();
    return utilisateurRepository.save(utilisateur);
  }

  public Utilisateur changerStatut(Long id, boolean actif) {
    Utilisateur utilisateur = trouverParId(id);
    utilisateur.setActif(actif);
    return utilisateurRepository.save(utilisateur);
  }

  public void supprimer(Long id) {
    Utilisateur utilisateur = trouverParId(id);
    utilisateurRepository.delete(utilisateur);
  }
}
