package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.demo.dto.AchatRequest;
import com.example.demo.dto.PaiementRequest;
import com.example.demo.entity.Achat;
import com.example.demo.entity.Producteur;
import com.example.demo.entity.Produit;
import com.example.demo.entity.enums.StatutPaiement;
import com.example.demo.exception.BusinessException;
import com.example.demo.repository.AchatRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires PURS du service Achat : aucun contexte Spring, aucune base de données. Toutes les
 * dépendances (repository, autres services) sont simulées avec Mockito. Objectif : vérifier la
 * logique métier (calcul du montant restant, statut, stock, caisse) en isolation totale et en
 * quelques millisecondes.
 */
@ExtendWith(MockitoExtension.class)
class AchatServiceTest {
  @Mock private AchatRepository achatRepository;
  @Mock private ProducteurService producteurService;
  @Mock private ProduitService produitService;
  @Mock private CaisseService caisseService;

  @InjectMocks private AchatService achatService;

  private Producteur producteur;
  private Produit produit;

  @BeforeEach
  void setUp() {
    producteur = Producteur.builder().id(1L).nom("Rakoto").build();
    produit =
        Produit.builder()
            .id(1L)
            .nom("Vanille")
            .quantiteStock(BigDecimal.ZERO)
            .prixMoyenAchat(BigDecimal.ZERO)
            .unite("kg")
            .build();
  }

  @Test
  void creerAchat_complet_aucunReste_doitEtrePAYEE() {
    AchatRequest requete = new AchatRequest();
    requete.setProducteurId(1L);
    requete.setProduitId(1L);
    requete.setQuantite(BigDecimal.valueOf(10));
    requete.setPrixUnitaire(BigDecimal.valueOf(2500));
    requete.setMontantPaye(BigDecimal.valueOf(25000)); // payé intégralement

    when(producteurService.trouverParId(1L)).thenReturn(producteur);
    when(produitService.trouverParId(1L)).thenReturn(produit);
    when(achatRepository.save(any(Achat.class)))
        .thenAnswer(
            invocation -> {
              Achat a = invocation.getArgument(0);
              a.setId(99L);
              return a;
            });

    Achat resultat = achatService.creerAchat(requete);

    assertThat(resultat.getMontantTotal()).isEqualByComparingTo("25000");
    assertThat(resultat.getMontantRestant()).isEqualByComparingTo("0");
    assertThat(resultat.getStatut()).isEqualTo(StatutPaiement.PAYEE);

    // Le stock doit avoir été mis à jour
    verify(produitService)
        .enregistrerEntreeStock(produit, requete.getQuantite(), requete.getPrixUnitaire());
    // Une sortie de caisse doit avoir été tracée car paiement comptant
    verify(caisseService)
        .enregistrerMouvementAutomatique(
            any(), any(), eq(BigDecimal.valueOf(25000)), any(), eq(99L));
  }

  @Test
  void creerAchat_aCredit_sansPaiement_doitEtreIMPAYEE() {
    AchatRequest requete = new AchatRequest();
    requete.setProducteurId(1L);
    requete.setProduitId(1L);
    requete.setQuantite(BigDecimal.valueOf(10));
    requete.setPrixUnitaire(BigDecimal.valueOf(2500));
    requete.setMontantPaye(BigDecimal.ZERO);

    when(producteurService.trouverParId(1L)).thenReturn(producteur);
    when(produitService.trouverParId(1L)).thenReturn(produit);
    when(achatRepository.save(any(Achat.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Achat resultat = achatService.creerAchat(requete);

    assertThat(resultat.getMontantRestant()).isEqualByComparingTo("25000");
    assertThat(resultat.getStatut()).isEqualTo(StatutPaiement.IMPAYEE);

    // Aucune transaction de caisse ne doit être créée si rien n'est payé
    verify(caisseService, never())
        .enregistrerMouvementAutomatique(any(), any(), any(), any(), any());
  }

  @Test
  void creerAchat_montantPayeSuperieurAuTotal_doitLeverBusinessException() {
    AchatRequest requete = new AchatRequest();
    requete.setProducteurId(1L);
    requete.setProduitId(1L);
    requete.setQuantite(BigDecimal.valueOf(10));
    requete.setPrixUnitaire(BigDecimal.valueOf(2500));
    requete.setMontantPaye(BigDecimal.valueOf(99999)); // > 25000

    when(producteurService.trouverParId(1L)).thenReturn(producteur);
    when(produitService.trouverParId(1L)).thenReturn(produit);

    assertThatThrownBy(() -> achatService.creerAchat(requete))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("dépasse");
  }

  @Test
  void payerAchat_paiementComplementaire_doitReduireLeRestant() {
    Achat achatExistant =
        Achat.builder()
            .id(5L)
            .producteur(producteur)
            .produit(produit)
            .quantite(BigDecimal.TEN)
            .prixUnitaire(BigDecimal.valueOf(2500))
            .montantTotal(BigDecimal.valueOf(25000))
            .montantPaye(BigDecimal.valueOf(10000))
            .montantRestant(BigDecimal.valueOf(15000))
            .statut(StatutPaiement.PARTIELLEMENT_PAYEE)
            .build();

    PaiementRequest requete = new PaiementRequest();
    requete.setMontant(BigDecimal.valueOf(15000)); // solde le reste

    when(achatRepository.findByIdAvecRelations(5L))
        .thenReturn(java.util.Optional.of(achatExistant));
    when(achatRepository.save(any(Achat.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Achat resultat = achatService.payerAchat(5L, requete);

    assertThat(resultat.getMontantRestant()).isEqualByComparingTo("0");
    assertThat(resultat.getStatut()).isEqualTo(StatutPaiement.PAYEE);
  }

  @Test
  void payerAchat_montantSuperieurAuRestant_doitLeverBusinessException() {
    Achat achatExistant =
        Achat.builder()
            .id(5L)
            .producteur(producteur)
            .produit(produit)
            .montantTotal(BigDecimal.valueOf(25000))
            .montantPaye(BigDecimal.valueOf(10000))
            .montantRestant(BigDecimal.valueOf(15000))
            .statut(StatutPaiement.PARTIELLEMENT_PAYEE)
            .build();

    PaiementRequest requete = new PaiementRequest();
    requete.setMontant(BigDecimal.valueOf(20000)); // > 15000 restant

    when(achatRepository.findByIdAvecRelations(5L))
        .thenReturn(java.util.Optional.of(achatExistant));

    assertThatThrownBy(() -> achatService.payerAchat(5L, requete))
        .isInstanceOf(BusinessException.class);
  }
}
