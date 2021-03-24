# PFE Drone

## Objectifs

- Communication d'informations entre **montres connectées** via un **serveur**
- Le **serveur** est **attaché au drone** afin de servir de réseau dans des environnements qui en sont privés

---

## Utilisations possibles

## 1. Sauvetage en montagne

Une des premières utilisations auxquelles on peut penser c'est pour des expéditions de **sauvtage en montagne**. Avec un suivi des trajets des sauveteurs et des informations qu'ils partagent.

Le réseau étant très instable en haute montagne, avoir un **réseau local** fourni par le drone est un avantage. De plus le drone peut fournir une vue d'en haut d'une certaine zone ce qui peut s'avérer très utile. 

## 2. Paintball

Une autre utilisation possible serait pour des **parties de paintball** en fôret par exemple. Le drone pourrait se déplacer facilement en hauteur sans être gêné par les arbres ou des obstacles. Les joueurs pourraient suivre l'**exploration des membres** de leur équipe et même voir la position des joueurs adverses si ils sont à portée.

---

## Fonctionnalités pricipales

- Suivi du **trajet** des utilisateurs grâce au **GPS** de la montre
- Le drone **partage les trajets** des autres utilisateurs à un utilisateur connecté
- Affichage des trajets des utilisateurs sur la montre
- Affichage d'une **zone explorée** autour des trajets (ex : 10 mètres)
- Le drone **reste avec un utilisateur** et part explorer au bout d'un certain temps pour récupérer les informations des autres utilisateurs
- Si un utilisateur n'est pas connecté au drone ses informations (ex: trajet) sont **sauvegardées en local** avant de les envoyer une fois reconnecté.

---

## Organisation

Le projet se sépare en deux équipes :

- Une équipe **ASPIC** qui s'occupe de partie réseau / bas niveau
- Une équipe **GL** qui s'occupe de la partie applicative / haut niveau

Le projet s'effectue avec la méthodologie **SCRUM** :

- 4 sprints de 1 semaine
- Suivi des US, Tasks et Sprints via **Jira** (https://drones2021.atlassian.net/secure/RapidBoard.jspa?rapidView=1&projectKey=DRON)