# Collecteo – Backend

Backend Spring Boot complet pour la gestion d'un collecteur agricole : producteurs,
clients, produits/stock, achats, ventes, caisse, banque, prêts bancaires, dépenses,
dettes fournisseurs, utilisateurs/rôles avec sécurité JWT, statistiques et rapports
PDF/Excel.

## Stack
- Java 17, Spring Boot 3.3.4
- Spring Web, Spring Data JPA, Spring Security, Bean Validation
- PostgreSQL (Neon ou local)
- JWT (jjwt 0.12.6)
- Apache POI (export Excel) + OpenPDF (export PDF)
- Lombok

## Création automatique des tables

Aucun script SQL à lancer. `spring.jpa.hibernate.ddl-auto=update` (dans
`application.yml`) fait créer/mettre à jour automatiquement TOUTES les tables au
démarrage à partir des entités JPA :

`producteurs`, `clients`, `produits`, `achats`, `ventes`, `caisse_transactions`,
`utilisateurs`, `comptes_bancaires`, `banque_transactions`, `prets_bancaires`,
`remboursements_prets`, `depenses`, `dettes`.

## Configuration

```bash
export DB_URL="jdbc:postgresql://ep-xxxx.eu-central-1.aws.neon.tech/collecteo?sslmode=require"
export DB_USERNAME="ton_user_neon"
export DB_PASSWORD="ton_mot_de_passe_neon"

# JWT - À CHANGER en production (32 caractères minimum)
export JWT_SECRET="une-cle-secrete-longue-et-aleatoire-a-changer"
```

## Lancer le projet

```bash
mvn spring-boot:run
```

L'API démarre sur `http://localhost:8080`.

## Authentification

Au tout premier démarrage, un compte administrateur est créé automatiquement :
- **Identifiant** : `admin`
- **Mot de passe** : `admin123`

⚠️ À changer immédiatement en production (créer un nouvel admin puis désactiver
celui-ci via `/api/utilisateurs/{id}/statut?actif=false`).

```http
POST /api/auth/login
Content-Type: application/json

{
  "nomUtilisateur": "admin",
  "motDePasse": "admin123"
}
```

Réponse :
```json
{
  "token": "eyJhbGciOi...",
  "nomUtilisateur": "admin",
  "nom": "Administrateur",
  "role": "ADMINISTRATEUR"
}
```

Pour tous les appels suivants, ajoute l'en-tête :
```
Authorization: Bearer eyJhbGciOi...
```

Toutes les routes `/api/**` nécessitent d'être connecté, sauf `/api/auth/**`.
Les routes `/api/utilisateurs/**` sont réservées au rôle `ADMINISTRATEUR`.

## Endpoints par module

| Module        | Méthode | URL                                          | Description |
|---------------|---------|-----------------------------------------------|--------------|
| Auth          | POST    | `/api/auth/login`                              | Connexion -> token JWT |
| Utilisateurs  | CRUD    | `/api/utilisateurs`                            | Réservé ADMINISTRATEUR |
| Producteurs   | CRUD    | `/api/producteurs`                             | + `/recherche?motCle=` |
| Clients       | CRUD    | `/api/clients`                                 | + `/recherche?motCle=` |
| Produits      | CRUD    | `/api/produits`                                | + `/valeur-stock`, `/rupture` |
| Achats        | GET/POST| `/api/achats`, `/api/achats/{id}/paiement`     | Augmente le stock + dette producteur |
| Ventes        | GET/POST| `/api/ventes`, `/api/ventes/{id}/paiement`     | Diminue le stock + créance client |
| Caisse        | GET/POST| `/api/caisse/transactions`, `/api/caisse/solde`| Mouvements liquides |
| Banque        | CRUD    | `/api/banque/comptes`, `/api/banque/transactions` | Comptes + dépôts/retraits |
| Prêts         | CRUD    | `/api/prets`, `/api/prets/{id}/remboursement`  | Calcul auto de la mensualité |
| Dépenses      | GET/POST| `/api/depenses`, `/api/depenses/par-categorie` | Sortie de caisse automatique |
| Dettes        | CRUD    | `/api/dettes`, `/api/dettes/{id}/paiement`     | Fournisseurs hors producteurs |
| Dashboard     | GET     | `/api/dashboard`                               | Vue financière globale complète |
| Statistiques  | GET     | `/api/statistiques`                            | Évolution mensuelle (ventes, achats, dépenses, bénéfice) |
| Rapports      | GET     | `/api/rapports/ventes/excel`                   | Export .xlsx |
| Rapports      | GET     | `/api/rapports/achats/excel`                   | Export .xlsx |
| Rapports      | GET     | `/api/rapports/financier/pdf`                  | Export .pdf (rapport complet) |

## Logique métier automatique

- **Achat créé** -> stock du produit augmenté + prix moyen pondéré recalculé +
  sortie de caisse si payé comptant + reste = dette envers le producteur.
- **Vente créée** -> stock vérifié puis diminué + entrée de caisse si payé comptant +
  reste = créance sur le client.
- **Dépense créée** -> sortie de caisse automatique.
- **Prêt créé** -> mensualité calculée (annuités constantes) + montant emprunté
  crédité sur le compte bancaire.
- **Remboursement de prêt** -> capital restant diminué + compte bancaire débité +
  statut passé à `SOLDE` une fois remboursé.
- **Dashboard** -> agrège ventes, achats, dépenses, stock, caisse, banque, créances,
  dettes (producteurs + fournisseurs) et prêts pour donner le **bénéfice net réel**.
