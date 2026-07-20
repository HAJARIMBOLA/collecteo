package com.example.demo.dto;

import com.example.demo.entity.enums.RoleUtilisateur;
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
