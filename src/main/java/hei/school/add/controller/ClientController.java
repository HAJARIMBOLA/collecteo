package hei.school.add.controller;

import hei.school.add.entity.Client;
import hei.school.add.service.ClientService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
  private final ClientService clientService;

  @GetMapping
  public List<Client> listerTous() {
    return clientService.listerTous();
  }

  @GetMapping("/{id}")
  public Client trouverParId(@PathVariable Long id) {
    return clientService.trouverParId(id);
  }

  @GetMapping("/recherche")
  public List<Client> rechercher(@RequestParam String motCle) {
    return clientService.rechercher(motCle);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Client creer(@Valid @RequestBody Client client) {
    return clientService.creer(client);
  }

  @PutMapping("/{id}")
  public Client modifier(@PathVariable Long id, @Valid @RequestBody Client client) {
    return clientService.modifier(id, client);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> supprimer(@PathVariable Long id) {
    clientService.supprimer(id);
    return ResponseEntity.noContent().build();
  }
}
