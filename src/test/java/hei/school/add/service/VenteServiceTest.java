package hei.school.add.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import hei.school.add.dto.VenteRequest;
import hei.school.add.entity.Client;
import hei.school.add.entity.Produit;
import hei.school.add.entity.Vente;
import hei.school.add.entity.enums.StatutPaiement;
import hei.school.add.exception.BusinessException;
import hei.school.add.repository.VenteRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires purs du service Vente, en particulier la règle métier la plus importante : on ne
 * doit jamais pouvoir vendre plus que le stock disponible.
 */
@ExtendWith(MockitoExtension.class)
class VenteServiceTest {
  @Mock private VenteRepository venteRepository;
  @Mock private ClientService clientService;
  @Mock private ProduitService produitService;
  @Mock private CaisseService caisseService;

  @InjectMocks private VenteService venteService;

  private Client client;
  private Produit produit;

  @BeforeEach
  void setUp() {
    client = Client.builder().id(1L).nom("Client Test").build();
    produit =
        Produit.builder()
            .id(1L)
            .nom("Café")
            .quantiteStock(BigDecimal.valueOf(50))
            .prixMoyenAchat(BigDecimal.valueOf(2000))
            .unite("kg")
            .build();
  }

  @Test
  void creerVente_stockSuffisant_doitReussir() {
    VenteRequest requete = new VenteRequest();
    requete.setClientId(1L);
    requete.setProduitId(1L);
    requete.setQuantite(BigDecimal.valueOf(20));
    requete.setPrixUnitaire(BigDecimal.valueOf(3000));
    requete.setMontantPaye(BigDecimal.valueOf(60000));

    when(clientService.trouverParId(1L)).thenReturn(client);
    when(produitService.trouverParId(1L)).thenReturn(produit);
    when(venteRepository.save(any(Vente.class)))
        .thenAnswer(
            invocation -> {
              Vente v = invocation.getArgument(0);
              v.setId(1L);
              return v;
            });

    Vente resultat = venteService.creerVente(requete);

    assertThat(resultat.getMontantTotal()).isEqualByComparingTo("60000");
    assertThat(resultat.getStatut()).isEqualTo(StatutPaiement.PAYEE);
    verify(produitService)
        .enregistrerSortieStock(produit, requete.getQuantite(), requete.getPrixUnitaire());
  }

  @Test
  void creerVente_stockInsuffisant_doitLeverBusinessException() {
    VenteRequest requete = new VenteRequest();
    requete.setClientId(1L);
    requete.setProduitId(1L);
    requete.setQuantite(BigDecimal.valueOf(999)); // > 50 en stock
    requete.setPrixUnitaire(BigDecimal.valueOf(3000));

    when(clientService.trouverParId(1L)).thenReturn(client);
    when(produitService.trouverParId(1L)).thenReturn(produit);

    assertThatThrownBy(() -> venteService.creerVente(requete))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Stock insuffisant");

    // Le stock ne doit JAMAIS être touché si la vente est refusée
    verify(produitService, never()).enregistrerSortieStock(any(), any(), any());
    verify(venteRepository, never()).save(any());
  }

  @Test
  void creerVente_aCredit_doitCreerUneCreance() {
    VenteRequest requete = new VenteRequest();
    requete.setClientId(1L);
    requete.setProduitId(1L);
    requete.setQuantite(BigDecimal.valueOf(10));
    requete.setPrixUnitaire(BigDecimal.valueOf(3000));
    requete.setMontantPaye(BigDecimal.ZERO); // vente entièrement à crédit

    when(clientService.trouverParId(1L)).thenReturn(client);
    when(produitService.trouverParId(1L)).thenReturn(produit);
    when(venteRepository.save(any(Vente.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Vente resultat = venteService.creerVente(requete);

    assertThat(resultat.getMontantRestant()).isEqualByComparingTo("30000");
    assertThat(resultat.getStatut()).isEqualTo(StatutPaiement.IMPAYEE);
    verify(caisseService, never())
        .enregistrerMouvementAutomatique(any(), any(), any(), any(), any());
  }
}
