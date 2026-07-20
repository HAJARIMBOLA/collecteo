package hei.school.add.dto;

import hei.school.add.entity.enums.RoleUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
  private String token;
  private String nomUtilisateur;
  private String nom;
  private RoleUtilisateur role;
}
