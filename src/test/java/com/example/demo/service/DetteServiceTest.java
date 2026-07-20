package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.dto.DetteRequest;
import com.example.demo.dto.PaiementRequest;
import com.example.demo.entity.Dette;
import com.example.demo.entity.enums.StatutPaiement;
import com.example.demo.exception.BusinessException;
import com.example.demo.repository.DetteRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DetteServiceTest {
  @Mock private DetteRepository detteRepository;
  @Mock private CaisseService caisseService;

  @InjectMocks private DetteService detteService;

  @Test
  void creer_doitInitialiserLeMontantRestantEgalAuMontant() {
    DetteRequest requete = new DetteRequest();
    requete.setFournisseur("Garage Tana");
    requete.setMotif("Réparation véhicule");
    requete.setMontant(BigDecimal.valueOf(150000));

    when(detteRepository.save(any(Dette.class))).thenAnswer(i -> i.getArgument(0));

    Dette resultat = detteService.creer(requete);

    assertThat(resultat.getMontantRestant()).isEqualByComparingTo("150000");
    assertThat(resultat.getStatut()).isEqualTo(StatutPaiement.IMPAYEE);
  }

  @Test
  void payer_paiementPartiel_doitPasserEnPARTIELLEMENT_PAYEE() {
    Dette dette =
        Dette.builder()
            .id(1L)
            .fournisseur("Garage Tana")
            .montant(BigDecimal.valueOf(150000))
            .montantPaye(BigDecimal.ZERO)
            .montantRestant(BigDecimal.valueOf(150000))
            .statut(StatutPaiement.IMPAYEE)
            .build();

    PaiementRequest requete = new PaiementRequest();
    requete.setMontant(BigDecimal.valueOf(50000));

    when(detteRepository.findById(1L)).thenReturn(Optional.of(dette));
    when(detteRepository.save(any(Dette.class))).thenAnswer(i -> i.getArgument(0));

    Dette resultat = detteService.payer(1L, requete);

    assertThat(resultat.getMontantRestant()).isEqualByComparingTo("100000");
    assertThat(resultat.getStatut()).isEqualTo(StatutPaiement.PARTIELLEMENT_PAYEE);
  }

  @Test
  void payer_montantSuperieurAuRestant_doitLeverBusinessException() {
    Dette dette =
        Dette.builder()
            .id(1L)
            .fournisseur("Garage Tana")
            .montant(BigDecimal.valueOf(150000))
            .montantPaye(BigDecimal.ZERO)
            .montantRestant(BigDecimal.valueOf(150000))
            .statut(StatutPaiement.IMPAYEE)
            .build();

    PaiementRequest requete = new PaiementRequest();
    requete.setMontant(BigDecimal.valueOf(999999));

    when(detteRepository.findById(1L)).thenReturn(Optional.of(dette));

    assertThatThrownBy(() -> detteService.payer(1L, requete)).isInstanceOf(BusinessException.class);
  }
}
