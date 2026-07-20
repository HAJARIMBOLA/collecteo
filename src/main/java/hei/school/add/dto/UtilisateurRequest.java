package hei.school.add.dto;

import hei.school.add.entity.enums.RoleUtilisateur;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UtilisateurRequest {
  @NotBlank private String nomUtilisateur;

  @NotBlank
  @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
  private String motDePasse;

  @NotBlank private String nom;

  @NotNull private RoleUtilisateur role;
}
