package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ClientRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {
  private final ClientRepository clientRepository;

  public List<Client> listerTous() {
    return clientRepository.findAll();
  }

  public Client trouverParId(Long id) {
    return clientRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'id " + id));
  }

  public List<Client> rechercher(String motCle) {
    return clientRepository.rechercherParNom(motCle);
  }

  public Client creer(Client client) {
    return clientRepository.save(client);
  }

  public Client modifier(Long id, Client donnees) {
    Client existant = trouverParId(id);
    existant.setNom(donnees.getNom());
    existant.setTelephone(donnees.getTelephone());
    existant.setAdresse(donnees.getAdresse());
    return clientRepository.save(existant);
  }

  public void supprimer(Long id) {
    Client existant = trouverParId(id);
    clientRepository.delete(existant);
  }
}
