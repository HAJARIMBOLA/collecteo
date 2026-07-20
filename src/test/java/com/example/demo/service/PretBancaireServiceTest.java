package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.dto.PretBancaireRequest;
import com.example.demo.entity.CompteBancaire;
import com.example.demo.entity.PretBancaire;
import com.example.demo.entity.enums.StatutPret;
import com.example.demo.exception.BusinessException;
import com.example.demo.repository.PretBancaireRepository;
import com.example.demo.repository.RemboursementPretRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires du calcul de mensualité (méthode des annuités constantes) et du suivi du capital
 * restant dû.
 */
@ExtendWith(MockitoExtension.class)
class PretBancaireServiceTest {
  @Mock private PretBancaireRepository pretBancaireRepository;
  @Mock private RemboursementPretRepository remboursementPretRepository;
  @Mock private BanqueService banqueService;

  @InjectMocks private PretBancaireService pretBancaireService;

  private CompteBancaire compte;

  @BeforeEach
  void setUp() {
    compte =
        CompteBancaire.builder()
            .id(1L)
            .nomBanque("BNI")
            .solde(BigDecimal.valueOf(1_000_000))
            .build();
  }

  @Test
  void creerPret_doitCalculerLaMensualiteSelonAnnuitesConstantes() {
    // Référence connue : 1 200 000 Ar emprunté, 12% annuel, 12 mois
    // -> mensualité théorique ≈ 106 589 Ar (calculée avec la formule des annuités constantes)
    PretBancaireRequest requete = new PretBancaireRequest();
    requete.setCompteBancaireId(1L);
    requete.setMontantEmprunte(BigDecimal.valueOf(1_200_000));
    requete.setDatePret(LocalDate.of(2026, 1, 1));
    requete.setDureeMois(12);
    requete.setTauxInteret(BigDecimal.valueOf(12));

    when(banqueService.trouverCompteParId(1L)).thenReturn(compte);
    when(pretBancaireRepository.save(any(PretBancaire.class)))
        .thenAnswer(
            i -> {
              PretBancaire p = i.getArgument(0);
              p.setId(1L);
              return p;
            });

    PretBancaire resultat = pretBancaireService.creerPret(requete);

    assertThat(resultat.getMensualite().doubleValue()).isCloseTo(106589.0, within(500.0));
    assertThat(resultat.getCapitalRestant()).isEqualByComparingTo("1200000");
    assertThat(resultat.getStatut()).isEqualTo(StatutPret.EN_COURS);
    assertThat(resultat.getProchaineEcheance()).isEqualTo(LocalDate.of(2026, 2, 1));
  }

  @Test
  void creerPret_tauxZero_doitRepartirLeCapitalLineairement() {
    PretBancaireRequest requete = new PretBancaireRequest();
    requete.setCompteBancaireId(1L);
    requete.setMontantEmprunte(BigDecimal.valueOf(1_200_000));
    requete.setDatePret(LocalDate.of(2026, 1, 1));
    requete.setDureeMois(12);
    requete.setTauxInteret(BigDecimal.ZERO);

    when(banqueService.trouverCompteParId(1L)).thenReturn(compte);
    when(pretBancaireRepository.save(any(PretBancaire.class))).thenAnswer(i -> i.getArgument(0));

    PretBancaire resultat = pretBancaireService.creerPret(requete);

    assertThat(resultat.getMensualite()).isEqualByComparingTo("100000.00");
  }

  @Test
  void rembourser_capitalIntegralementRembourse_doitPasserAuStatutSOLDE() {
    PretBancaire pret =
        PretBancaire.builder()
            .id(1L)
            .compteBancaire(compte)
            .montantEmprunte(BigDecimal.valueOf(100_000))
            .capitalRestant(BigDecimal.valueOf(100_000))
            .totalRembourse(BigDecimal.ZERO)
            .prochaineEcheance(LocalDate.of(2026, 2, 1))
            .statut(StatutPret.EN_COURS)
            .build();

    when(pretBancaireRepository.findByIdAvecCompte(1L)).thenReturn(java.util.Optional.of(pret));
    when(pretBancaireRepository.save(any(PretBancaire.class))).thenAnswer(i -> i.getArgument(0));

    PretBancaire resultat = pretBancaireService.rembourser(1L, BigDecimal.valueOf(100_000));

    assertThat(resultat.getCapitalRestant()).isEqualByComparingTo("0");
    assertThat(resultat.getStatut()).isEqualTo(StatutPret.SOLDE);
  }

  @Test
  void rembourser_montantSuperieurAuCapitalRestant_doitLeverBusinessException() {
    PretBancaire pret =
        PretBancaire.builder()
            .id(1L)
            .compteBancaire(compte)
            .capitalRestant(BigDecimal.valueOf(50_000))
            .totalRembourse(BigDecimal.ZERO)
            .statut(StatutPret.EN_COURS)
            .build();

    when(pretBancaireRepository.findByIdAvecCompte(1L)).thenReturn(java.util.Optional.of(pret));

    assertThatThrownBy(() -> pretBancaireService.rembourser(1L, BigDecimal.valueOf(99_999)))
        .isInstanceOf(BusinessException.class);
  }
}
