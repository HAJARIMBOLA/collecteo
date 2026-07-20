package hei.school.add.controller;

import hei.school.add.dto.PaiementRequest;
import hei.school.add.dto.VenteRequest;
import hei.school.add.entity.Vente;
import hei.school.add.service.VenteService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventes")
@RequiredArgsConstructor
public class VenteController {
  private final VenteService venteService;

  @GetMapping
  public List<Vente> listerToutes() {
    return venteService.listerToutes();
  }

  @GetMapping("/{id}")
  public Vente trouverParId(@PathVariable Long id) {
    return venteService.trouverParId(id);
  }

  @GetMapping("/client/{clientId}")
  public List<Vente> listerParClient(@PathVariable Long clientId) {
    return venteService.listerParClient(clientId);
  }

  @GetMapping("/client/{clientId}/creances")
  public List<Vente> listerCreancesParClient(@PathVariable Long clientId) {
    return venteService.listerCreancesParClient(clientId);
  }

  @GetMapping("/total-creances")
  public BigDecimal totalCreances() {
    return venteService.totalCreances();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Vente creer(@Valid @RequestBody VenteRequest requete) {
    return venteService.creerVente(requete);
  }

  @PostMapping("/{id}/paiement")
  public Vente payer(@PathVariable Long id, @Valid @RequestBody PaiementRequest requete) {
    return venteService.payerVente(id, requete);
  }
}
