package hei.school.add.repository;

import hei.school.add.entity.Utilisateur;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
  Optional<Utilisateur> findByNomUtilisateur(String nomUtilisateur);

  boolean existsByNomUtilisateur(String nomUtilisateur);
}
