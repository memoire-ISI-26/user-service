# user-service

Ce microservice est responsable de la **gestion des profils utilisateurs** (clients et administrateurs) au sein du prototype Max It.

## ⚙️ Rôle et Fonctionnalités

- **Enregistrement des Clients** (`/users/client/register`) :
  - Création du profil utilisateur dans la base de données MySQL.
  - Hachage sécurisé du mot de passe avec BCrypt.
  - Appel distant via `WalletProxy` pour instancier un compte portefeuille associé (devise par défaut : `XOF`) dans le `wallet-service`.
- **Mise à jour du mot de passe** : Permet aux clients de modifier leur mot de passe en validant l'ancien mot de passe.
- **Gestion Cache** : Intégration de l'annotation `@Cacheable` de Spring (sur les méthodes de recherche par ID et numéro) et `@CacheEvict` (lors d'une suppression ou modification) pour maximiser les performances de lecture.
- **Audit & Sécurité** : Envoi périodique d'événements de tracking (`REGISTER`, `PASSWORD_UPDATE`) au `tracking-service`.

---

## 🔌 Configuration et Endpoints

- **Port par défaut** : `8101`
- **Base de données** : MySQL (`user_service_db`), configurée via Hibernate (création automatique des tables).
- **Technologie** : Spring Boot, JPA/Hibernate, Spring Cache, OpenFeign Client

### Endpoints exposés :

#### 1. Inscription d'un nouveau client (Public)
* **URL** : `POST /users/client/register`
* **Corps de la requête (JSON)** :
  ```json
  {
    "firstName": "Ibrahim",
    "lastName": "Diallo",
    "number": "771234567",
    "password": "mot_de_passe",
    "birthdate": "2000-01-01"
  }
  ```
* **Réponse (201 Created)** : Renvoie le profil du client créé avec son ID unique et le port du service ayant traité la demande.

#### 2. Récupération des informations d'un client
* **URL** : `GET /users/client/{id}` ou `GET /users/client/number/{number}`
* **Rôles autorisés** : `CLIENT` (uniquement son propre profil), `ADMINISTRATOR` ou `INTERNAL` (appel inter-service).

#### 3. Modification de mot de passe
* **URL** : `PUT /users/client/password`
* **Corps de la requête (JSON)** :
  ```json
  {
    "oldPassword": "mot_de_passe",
    "newPassword": "nouveau_mot_de_passe"
  }
  ```

---

## 🔗 Liens Feign Clients

Ce service délègue des actions à d'autres microservices :
- **`WalletProxy`** (cible `wallet-service`) : Appelé lors de la création d'un client pour lui ouvrir un compte portefeuille initialisé à 0 XOF.
- **`TrackingProxy`** (cible `tracking-service`) : Appelé pour enregistrer les événements liés au cycle de vie du profil utilisateur.
