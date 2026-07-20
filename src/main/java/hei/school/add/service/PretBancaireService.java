package hei.school.add.service;

import hei.school.add.dto.PretBancaireRequest;
import hei.school.add.entity.CompteBancaire;
import hei.school.add.entity.PretBancaire;
import hei.school.add.entity.RemboursementPret;
import hei.school.add.entity.enums.StatutPret;
import hei.school.add.entity.enums.TypeTransactionCaisse;
import hei.school.add.exception.BusinessException;
import hei.school.add.exception.ResourceNotFoundException;
import hei.school.add.repository.PretBancaireRepository;
import hei.school.add.repository.RemboursementPretRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gère les prêts bancaires : calcul automatique de la mensualité (méthode des annuités constantes),
 * suivi du capital restant dû, et remboursements.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PretBancaireService {
  private final PretBancaireRepository pretBancaireRepository;
  private final RemboursementPretRepository remboursementPretRepository;
  private final BanqueService banqueService;

  public List<PretBancaire> listerTous() {
    return pretBancaireRepository.findAll();
  }

  public PretBancaire trouverParId(Long id) {
    return pretBancaireRepository
        .findByIdAvecCompte(id)
        .orElseThrow(() -> new ResourceNotFoundException("Prêt introuvable avec l'id " + id));
  }

  public BigDecimal totalEmprunte() {
    return pretBancaireRepository.calculerTotalEmprunte();
  }

  public BigDecimal totalRembourse() {
    return pretBancaireRepository.calculerTotalRembourse();
  }

  public BigDecimal capitalRestantTotal() {
    return pretBancaireRepository.calculerCapitalRestantTotal();
  }

  public PretBancaire creerPret(PretBancaireRequest requete) {
    CompteBancaire compte = banqueService.trouverCompteParId(requete.getCompteBancaireId());

    BigDecimal mensualite =
        calculerMensualite(
            requete.getMontantEmprunte(), requete.getTauxInteret(), requete.getDureeMois());

    PretBancaire pret =
        PretBancaire.builder()
            .compteBancaire(compte)
            .montantEmprunte(requete.getMontantEmprunte())
            .datePret(requete.getDatePret())
            .dureeMois(requete.getDureeMois())
            .tauxInteret(requete.getTauxInteret())
            .mensualite(mensualite)
            .capitalRestant(requete.getMontantEmprunte())
            .totalRembourse(BigDecimal.ZERO)
            .prochaineEcheance(requete.getDatePret().plusMonths(1))
            .statut(StatutPret.EN_COURS)
            .build();

    PretBancaire pretEnregistre = pretBancaireRepository.save(pret);

    // Le montant emprunté est crédité sur le compte bancaire concerné
    banqueService.appliquerMouvement(
        compte,
        TypeTransactionCaisse.ENTREE,
        requete.getMontantEmprunte(),
        "Déblocage prêt #" + pretEnregistre.getId());

    return pretEnregistre;
  }

  /** Enregistre une échéance / un remboursement et débite le compte bancaire correspondant. */
  public PretBancaire rembourser(Long pretId, BigDecimal montant) {
    PretBancaire pret = trouverParId(pretId);

    if (montant.compareTo(pret.getCapitalRestant()) > 0) {
      throw new BusinessException(
          "Le montant dépasse le capital restant dû (" + pret.getCapitalRestant() + ")");
    }

    pret.setCapitalRestant(pret.getCapitalRestant().subtract(montant));
    pret.setTotalRembourse(pret.getTotalRembourse().add(montant));
    pret.setProchaineEcheance(
        pret.getProchaineEcheance() != null
            ? pret.getProchaineEcheance().plusMonths(1)
            : LocalDate.now().plusMonths(1));

    if (pret.getCapitalRestant().compareTo(BigDecimal.ZERO) <= 0) {
      pret.setStatut(StatutPret.SOLDE);
    }

    PretBancaire pretMisAJour = pretBancaireRepository.save(pret);

    RemboursementPret remboursement =
        RemboursementPret.builder().pret(pretMisAJour).montant(montant).build();
    remboursementPretRepository.save(remboursement);

    banqueService.appliquerMouvement(
        pret.getCompteBancaire(),
        TypeTransactionCaisse.SORTIE,
        montant,
        "Remboursement prêt #" + pret.getId());

    return pretMisAJour;
  }

  public List<RemboursementPret> listerRemboursements(Long pretId) {
    return remboursementPretRepository.findByPretIdOrderByDateDesc(pretId);
  }

  /**
   * Calcule la mensualité d'un prêt à annuités constantes : M = P * r / (1 - (1 + r)^-n) où P =
   * capital emprunté, r = taux mensuel, n = durée en mois.
   */
  private BigDecimal calculerMensualite(
      BigDecimal montant, BigDecimal tauxAnnuelPourcent, int dureeMois) {
    if (tauxAnnuelPourcent.compareTo(BigDecimal.ZERO) == 0) {
      return montant.divide(BigDecimal.valueOf(dureeMois), 2, RoundingMode.HALF_UP);
    }
    BigDecimal tauxMensuel =
        tauxAnnuelPourcent.divide(BigDecimal.valueOf(1200), MathContext.DECIMAL64);
    BigDecimal unPlusR = BigDecimal.ONE.add(tauxMensuel);
    BigDecimal unPlusRPuissanceMoinsN =
        BigDecimal.ONE.divide(unPlusR.pow(dureeMois, MathContext.DECIMAL64), MathContext.DECIMAL64);
    BigDecimal denominateur = BigDecimal.ONE.subtract(unPlusRPuissanceMoinsN);

    return montant.multiply(tauxMensuel).divide(denominateur, 2, RoundingMode.HALF_UP);
  }
}
