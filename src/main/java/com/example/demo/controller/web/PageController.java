package com.example.demo.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sert les pages HTML (Thymeleaf) du frontend intégré.
 *
 * <p>Ces routes sont volontairement séparées de /api/** (voir SecurityConfig : anyRequest() en
 * dehors de /api/** est déjà permitAll()). L'authentification réelle reste gérée par le JWT
 * stateless existant : chaque page charge son contenu via des appels fetch() vers /api/** avec le
 * token stocké côté navigateur (voir /js/api.js). Ce contrôleur ne fait que renvoyer le squelette
 * HTML de chaque écran.
 */
@Controller
public class PageController {

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping({"/", "/dashboard"})
  public String dashboard() {
    return "dashboard";
  }

  @GetMapping("/producteurs")
  public String producteurs() {
    return "producteurs";
  }

  @GetMapping("/clients")
  public String clients() {
    return "clients";
  }

  @GetMapping("/produits")
  public String produits() {
    return "produits";
  }

  @GetMapping("/ventes")
  public String ventes() {
    return "ventes";
  }

  @GetMapping("/achats")
  public String achats() {
    return "achats";
  }

  @GetMapping("/dettes")
  public String dettes() {
    return "dettes";
  }

  @GetMapping("/caisse")
  public String caisse() {
    return "caisse";
  }

  @GetMapping("/banque")
  public String banque() {
    return "banque";
  }

  @GetMapping("/prets")
  public String prets() {
    return "prets";
  }

  @GetMapping("/depenses")
  public String depenses() {
    return "depenses";
  }

  @GetMapping("/utilisateurs")
  public String utilisateurs() {
    return "utilisateurs";
  }

  @GetMapping("/statistiques")
  public String statistiques() {
    return "statistiques";
  }

  @GetMapping("/rapports")
  public String rapports() {
    return "rapports";
  }
}
