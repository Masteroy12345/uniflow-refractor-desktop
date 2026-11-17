# UniFlow Desktop — application campus

Client Java 17 / JavaFX. L’application s’authentifie par Firebase Authentication avec email/mot de passe et appelle Firestore REST avec le jeton ID de l’utilisateur connecté. Elle n’embarque aucune clé Admin SDK, clé privée, donnée seed ou compte de démonstration.

## Contrat et sécurité

Le client suit le schéma `FIREBASE_CAMPUS_CONTRACT.md` et les règles Firestore de référence du dépôt Web (fichiers de référence en lecture seule). L’ID projet par défaut est `uniflow-d79e0`. La configuration client est fournie à l’exécution via `UNIFLOW_FIREBASE_API_KEY` ou `-Duniflow.firebase.apiKey=...`; l’identifiant projet peut être remplacé par `UNIFLOW_FIREBASE_PROJECT_ID` ou `-Duniflow.firebase.projectId=...`. La clé API est une configuration publique du client, pas une clé privée. Aucun `.env` réel ou secret privilégié ne doit être ajouté au dépôt.

Le profil `users/{uid}` n’écrit que les champs canoniques `uid,email,displayName,firstName,lastName,phone,universityId,facultyId,programId,level,studentNumber,role,status,photoUrl,createdAt,updatedAt`. L’inscription publique crée exclusivement un profil `student` / `pending`; la connexion n’accepte que les profils `active` avec rôle `student`, `delegate`, `teacher` ou `admin`. L’application ne promeut aucun rôle, ne valide aucun statut et ne dépend pas d’un custom claim pour les rôles campus.

Toutes les listes utilisent `documents:runQuery` avec une limite de 100 et des filtres de sécurité explicites; aucun `documents.list` sans filtre n’est utilisé. Le rôle est déterminé par le profil connecté, mais l’autorisation reste imposée par Firestore Rules.

## Modules et filtres

| Module | Collection canonique | Filtre de la requête |
| --- | --- | --- |
| Tableau de bord — cours | `academic_courses` | `universityId`; étudiants filtrés aussi par `isPublished == true` |
| Emploi du temps | `academic_schedules` | `universityId`; étudiants filtrés aussi par `isPublished == true` |
| Devoirs | `academic_assignments` | `universityId`; étudiants filtrés aussi par `isPublished == true` |
| Dépôts | `academic_submissions` | étudiant: `studentUid`; personnel campus: `universityId` |
| Notes | `academic_grades` | étudiant: `studentUid`; personnel campus: `universityId` |
| Présences étudiant | `attendance_records` | étudiant: `studentUid`; personnel campus: `universityId` |
| Registre des séances | `attendance_sessions` | `universityId` |
| Salles | `classrooms` | `universityId` |
| Annuaire | `users` | réservé à l’admin, `universityId` |
| Notifications | `notifications` | `recipientUid` égal à l’UID connecté |
| Bibliothèque académique | `academic_library` | `universityId` et `isPublished == true` |
| Frais de scolarité | `tuition_payments` | étudiant: `studentUid`; admin: `universityId`; autres rôles refusés côté client |
| Bourses | `scholarships` | `universityId` et `isPublished == true` |

Les filtres d’inscription, de rendu, de notes et d’émargement sont alignés sur `studentUid`; les notifications sur `recipientUid`. Les collections non prévues (`courses`, `schedules`, `attendance`, `grades`, `tuition`, `announcements`, etc.) ne sont pas interrogées ni créées. Les écritures desktop sont bornées à la création de métadonnées de dépôt dans `academic_submissions`, par un étudiant actif avec `studentUid` et `universityId`; le fichier lui-même n’est pas téléversé. Les publications d’annonces/notifications restent hors du client.

## Limites et validation opérationnelle

La connexion Firebase et les listes dépendent d’un projet correctement configuré, d’un profil actif contenant le bon `universityId` et des règles déployées par l’administrateur du projet. Les erreurs de configuration et de règles sont affichées; aucune donnée fictive ne remplace une réponse vide ou une erreur. Les identifiants institutionnels saisis à l’inscription doivent correspondre aux vrais IDs Firestore. Les fonctions de messagerie, visioconférence et IoT ne font pas partie du contrat Firebase campus et ne prétendent pas être connectées.

L’identité Microsoft Store est conservée exactement : `KERNELFORGE.Uniflowwork`, éditeur `CN=200A91F7-4B15-441F-9332-83F216885B09`, Publisher Display Name `KERNEL FORGE`, PFN `KERNELFORGE.Uniflowwork_3wyzhk1x8xr0a`, Store ID `9NGZKFGK98K6`.

Commandes de validation hors réseau :

```bash
mvn -o test
mvn -DskipTests package
git diff --check
```
