package hei.school.add.service;

import hei.school.add.dto.DetteRequest;
import hei.school.add.dto.PaiementRequest;
import hei.school.add.entity.Dette;
import hei.school.add.entity.enums.CategorieTransactionCaisse;
import hei.school.add.entity.enums.StatutPaiement;
import hei.school.add.entity.enums.TypeTransactionCaisse;
import hei.school.add.exception.BusinessException;
import hei.school.add.exception.ResourceNotFoundException;
import hei.school.add.repository.DetteRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gère les dettes envers les fournisseurs/partenaires (hors achats de produits aux producteurs).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DetteService {
  private final DetteRepository detteRepository;
  private final CaisseService caisseService;

  public List<Dette> listerToutes() {
    return detteRepository.findAll();
  }

  public Dette trouverParId(Long id) {
    return detteRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Dette introuvable avec l'id " + id));
  }

  public List<Dette> listerNonSoldees() {
    return detteRepository.trouverDettesNonSoldees();
  }

  public BigDecimal totalDettesFournisseurs() {
    return detteRepository.calculerTotalDettesFournisseurs();
  }

  public Dette creer(DetteRequest requete) {
    Dette dette =
        Dette.builder()
            .fournisseur(requete.getFournisseur())
            .motif(requete.getMotif())
            .montant(requete.getMontant())
            .montantPaye(BigDecimal.ZERO)
            .montantRestant(requete.getMontant())
            .dateLimite(requete.getDateLimite())
            .statut(StatutPaiement.IMPAYEE)
            .build();
    return detteRepository.save(dette);
  }

  public Dette payer(Long detteId, PaiementRequest requete) {
    Dette dette = trouverParId(detteId);

    if (requete.getMontant().compareTo(dette.getMontantRestant()) > 0) {
      throw new BusinessException(
          "Le montant payé dépasse le montant restant dû (" + dette.getMontantRestant() + ")");
    }

    dette.setMontantPaye(dette.getMontantPaye().add(requete.getMontant()));
    dette.setMontantRestant(dette.getMontantRestant().subtract(requete.getMontant()));
    dette.setStatut(determinerStatut(dette.getMontantPaye(), dette.getMontant()));

    Dette detteMiseAJour = detteRepository.save(dette);

    caisseService.enregistrerMouvementAutomatique(
        TypeTransactionCaisse.SORTIE,
        CategorieTransactionCaisse.DIVERS,
        requete.getMontant(),
        "Paiement dette #" + dette.getId() + " - " + dette.getFournisseur(),
        dette.getId());

    return detteMiseAJour;
  }

  private StatutPaiement determinerStatut(BigDecimal montantPaye, BigDecimal montantTotal) {
    if (montantPaye.compareTo(BigDecimal.ZERO) == 0) return StatutPaiement.IMPAYEE;
    if (montantPaye.compareTo(montantTotal) >= 0) return StatutPaiement.PAYEE;
    return StatutPaiement.PARTIELLEMENT_PAYEE;
  }
}
