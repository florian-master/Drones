# Fonctionnalité : afficher l’historique des positions sur la carte

## Pré-requis / set-up de l'app et de la position :
Opérations à effectuer dans une majorité de scénarios pour setup l'application

* Lancer l’émulateur
* Assigner comme position à l’émulateur (44.8079, -0.6005) (4 Rue Marc Sangnier, 33600 Pessac, France)
* Lancer le serveur avec la commande python3 BasicServer.py <adresse ip> 8080 
* Lancer l’application
* Autoriser la récolte des positions GPS en cliquant sur les boutons ACCESS_COARSE_LOC et ACCESS_FINE_LOCAT 

---

## Scénario : Route en ligne droite

* Appliquer le setup
* Se rendre sur la 3ème page de l’application : “Carte”
* Lancer une route (en mode piéton) allant de 211 Avenue de la Vieille Tour, 33400 Talence, France à 195 Avenue de la Vieille Tour, 33400 Talence, France

### Résultat : 

* La liste des zones explorées doit avoir suivi le chemin emprunté par l’utilisateur en étant bien superposée à la route affichée par la carte en fond.


## Scénario : Route avec un angle

* Lancer l’émulateur
* Assigner comme position à l’émulateur (44.8076, -0.5973) (A28, 33400 Talence, France)
* Lancer le serveur avec la commande python3 BasicServer.py <adresse ip> 8080 
* Lancer l’application
* Autoriser la récolte des positions GPS en cliquant sur les boutons ACCESS_COARSE_LOC et ACCESS_FINE_LOCAT 
* Se rendre sur la 3ème page de l’application : “Carte”
* Lancer une route (en mode piéton) allant de A28, 33400 Talence, France à A12, 33400 Talence, France

### Résultat : 

* La liste des zones explorées doit avoir suivi le chemin emprunté par l’utilisateur en étant bien superposée à la route affichée par la carte en fond.

---

# Fonctionnalité : zoomer et dézoomer sur la carte

## Scénario : Zoomer sur la carte
* Appliquer le setup
* Se rendre sur la 3ème page de l’application : “Carte”
* Appuyer sur la bouton + de l’interface

### Résultat : 
* La zone affichée par la map de fond, la taille du cercle de la zone explorée ainsi que la taille du triangle de position sont recalculées et proportionnels à ce qu’affiche la scalebar. 

## Scénario : Dézoomer sur la carte
* Appliquer le setup
* Se rendre sur la 3ème page de l’application : “Carte”
* Appuyer sur la bouton - de l’interface

### Résultat : 
* La zone affichée par la map de fond, la taille du cercle de la zone explorée ainsi que la taille du triangle de position sont recalculées et proportionnels à ce qu’affiche la scalebar. 

---

# Fonctionnalité : Utiliser un marqueur rouge sur la carte

## Scénario : Ajouter un marqueur sur la carte
* Appliquer le setup
* Se rendre sur la 3ème page de l’application : “Carte”
* Appuyer sur longuement sur l’écran 
* Appuyer sur “placer un point de repère”
* Sélectionner un marqueur de couleur “rouge”

### Résultat : 
* L’utilisateur est redirigé sur la carte
* Un rond rouge apparaît sur la carte à l’endroit où l’on a appuyé  
* Un toast apparaît pour nous signaler que l’action a bien été réalisée

## Scénario : Supprimer un marqueur sur la carte
* Appliquer le setup
* Se rendre sur la 3ème page de l’application : “Carte”
* Appuyer sur longuement sur l’écran 
* Appuyer sur “placer un point de repère”
* Sélectionner un marqueur de couleur “rouge”
* Cliquer longuement sur le point que l’on vient d’ajouter
* Cliquer sur dans le menu sur “supprimer un point de repère”

### Résultat : 
* L’utilisateur est redirigé sur la carte 
* Le point a été supprimé à l’endroit où l’on a appuyé 
* Un toast apparaît pour nous signaler que l’action a bien été réalisée

## Scénario : Vérifier la position d’un marqueur lorsque l’on zoome
* Appliquer le setup
* Se rendre sur la 3ème page de l’application : “Carte”
* Appuyer sur longuement sur l’écran au milieu du carrefour sur la gauche de l’écran
* Appuyer sur “placer un point de repère”
* Sélectionner un marqueur de couleur “rouge”
* Appuyer sur le bouton + de l’interface

### Résultat :  
* Le point rouge reste au milieu du carrefour après le zoom

## Scénario : Vérifier la position d’un marqueur lorsque l’on dézoome
* Appliquer le setup
* Se rendre sur la 3ème page de l’application : “Carte”
* Appuyer sur longuement sur l’écran au milieu du carrefour sur la gauche de l’écran
* Appuyer sur “placer un point de repère”
* Sélectionner un marqueur de couleur “rouge”
* Appuyer sur la bouton - de l’interface

### Résultat :  
* Le point rouge reste au milieu du carrefour après le dézoom

## Scénario : Vérifier la position d’un marqueur lorsque l’utilisateur se déplace
* Appliquer le setup
* Se rendre sur la 3ème page de l’application : “Carte”
* Appuyer sur longuement sur l’écran au milieu du carrefour sur la gauche de l’écran
* Appuyer sur “placer un point de repère”
* Sélectionner un marqueur de couleur “rouge”
* Lancer une route (en mode piéton) allant de 211 Avenue de la Vieille Tour, 33400 Talence, France à 195 Avenue de la Vieille Tour, 33400 Talence, France

### Résultat :  
* Le point rouge reste à sa localisation originale malgré le déplacement de l'utilisateur

## Scénario : Supprimer un marqueur
* Appliquer le setup
* Se rendre sur la 3ème page de l’application : “Carte”
* Appuyer sur longuement sur l’écran au milieu du carrefour sur la gauche de l’écran
* Appuyer sur “placer un point de repère”
* Sélectionner un marqueur de couleur “rouge”
* Appuyer longuement sur le point rouge que l'on vient de créer
* Appuyer sur "supprimer un point de repère"

### Résultat :  
* Le point rouge reste a été supprimé
### Résultat :  
* Le point rouge reste à sa localisation originale malgré le déplacement de l'utilisateur

