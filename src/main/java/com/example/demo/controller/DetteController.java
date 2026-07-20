package com.example.demo.controller;

import com.example.demo.dto.DetteRequest;
import com.example.demo.dto.PaiementRequest;
import com.example.demo.entity.Dette;
import com.example.demo.service.DetteService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dettes")
@RequiredArgsConstructor
public class DetteController {
  private final DetteService detteService;

  @GetMapping
  public List<Dette> listerToutes() {
    return detteService.listerToutes();
  }

  @GetMapping("/{id}")
  public Dette trouverParId(@PathVariable Long id) {
    return detteService.trouverParId(id);
  }

  @GetMapping("/non-soldees")
  public List<Dette> listerNonSoldees() {
    return detteService.listerNonSoldees();
  }

  @GetMapping("/total")
  public BigDecimal totalDettesFournisseurs() {
    return detteService.totalDettesFournisseurs();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Dette creer(@Valid @RequestBody DetteRequest requete) {
    return detteService.creer(requete);
  }

  @PostMapping("/{id}/paiement")
  public Dette payer(@PathVariable Long id, @Valid @RequestBody PaiementRequest requete) {
    return detteService.payer(id, requete);
  }
}
