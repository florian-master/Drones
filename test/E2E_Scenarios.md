# Scénarios de Test E2E

## Pré-requis à tous les scénarios

- Lancer le simulateur de drone Olympe
- Lancer le server Python OlympeServer.py
- Lancer l'application Wear OS avec le même couple IP / Port que le serveur
- Autoriser la récupération de localisation sur l'application Wear OS

---

## Faire décoller le drone

- Aller sur le deuxième onglet de la montre
- Cliquer sur "Connect"
- Cliquer sur "Take Off"

### Résultat attendu

- Le drone décolle dans la simulation Olympe
- Un pop-up apparaît sur la montre pour indiquer que le drone a bien décollé
- Impossible de cliquer sur "Connect"
- Impossible de cliquer sur "Take Off"

---

## Faire atterrir le drone

- Exécuter avec succès le scénario "Faire décoller le drone"
- Cliquer sur "Landing"
- Cliquer sur "Disconnect"

### Résultat attendu

- Le drone atterri dans la simulation Olympe
- Un pop-up apparaît sur la montre pour indiquer que le drone a bien atterri
- Impossible de cliquer sur "Disconnect"
- Impossible de cliquer sur "Take Off" et "Landing"

---

## Envoyer le drone à des coordonnées spécifiques

- Exécuter avec succès le scénario "Faire décoller le drone"
- Aller au troisième onglet de la montre (affichage de la carte)
- Faire un appui long sur la carte à proximité de votre position
- Cliquer sur "Envoyer le drone"

### Résultat attendu

- Le drone se déplace vers la position sélectionnée dans la simulation Olympe
- Une fois arrivé la montre confirme l'arrivée du drone à la position

---

## Afficher les positions des utilisateurs

- Aller au troisième onglet de la montre (affichage de la carte)
- Définir une localisation
- Lancer une deuxième instance de l'app Wear OS
- Autoriser la récupération de la position
- Aller au 3ème onglet (carte)
- Définir la même localisation que la 1ère application
- Définir une route à emprunter (même point de départ : position actuelle, point d'arrivée différent)
- Éxécuter les routes sur chaque application

### Résultat attendu

- Les positions d'une montre sont affichées sur l'autre
- Les positions correspondent à la route empruntée

---