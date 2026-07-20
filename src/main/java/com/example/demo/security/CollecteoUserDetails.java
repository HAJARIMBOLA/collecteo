package com.example.demo.security;

import com.example.demo.entity.Utilisateur;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Adapte l'entité Utilisateur au contrat Spring Security UserDetails. */
@Getter
public class CollecteoUserDetails implements UserDetails {
  private final Utilisateur utilisateur;

  public CollecteoUserDetails(Utilisateur utilisateur) {
    this.utilisateur = utilisateur;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Le rôle est exposé avec le préfixe ROLE_ requis par Spring Security (ex: ROLE_ADMINISTRATEUR)
    return List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name()));
  }

  @Override
  public String getPassword() {
    return utilisateur.getMotDePasse();
  }

  @Override
  public String getUsername() {
    return utilisateur.getNomUtilisateur();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return utilisateur.isActif();
  }
}
