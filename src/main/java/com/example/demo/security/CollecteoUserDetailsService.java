package com.example.demo.security;

import com.example.demo.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CollecteoUserDetailsService implements UserDetailsService {
  private final UtilisateurRepository utilisateurRepository;

  @Override
  public UserDetails loadUserByUsername(String nomUtilisateur) throws UsernameNotFoundException {
    return utilisateurRepository
        .findByNomUtilisateur(nomUtilisateur)
        .map(CollecteoUserDetails::new)
        .orElseThrow(
            () -> new UsernameNotFoundException("Utilisateur introuvable : " + nomUtilisateur));
  }
}
