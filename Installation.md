# Installation

## Pre-requis

### 1. Installation de Android Studio

Afin de simuler une montre Wear OS il est nécessaire d'installer la dernière version d'Android Studio
Disponible ici : https://developer.android.com/studio

### 2. Créer une montre dans Android Studio

- Au sein de Android Studio ouvrir le AVD Manager
- Create Virtual Device
- New Hardware Profile
    - Device type : Wear OS
    - Screen size : 1.28 inch
    - Resolution : 416px x 416px
    - Round
    - Désactiver les caméras
    - Désactiver le proximity sensor
    - No Skin
    - Finish
- Sélectionner votre Montre
- Next
- Sélectionner Android Pie (Pas la version chinoise)
- Next
- Enable Device Frame
- Finish

### 3. Installer Python 3

Windows : https://www.python.org/downloads/

MacOS :

```shell
brew install python
```

Linux :

```shell
sudo apt update
sudo apt -y upgrade
```

### 4. Installer Parrot Sphinx

https://developer.parrot.com/docs/sphinx/firststep.html#

### 5. Installer Parrot Olympe

https://developer.parrot.com/docs/olympe/installation.html

### 6. Patcher Olympe

Une fois dans l'environnement olympe

```shell
pip install --upgrade aenum==2.2.5
```
### 7. Configurer le projet
Vous devez rajouter à la racine du dossier server un fichier `server_config.json`.

Voici un exemple:
```json
{
    "ip": "172.29.228.82",
    "port": 8080,
    "images_path": "/absolute/path/to/drone_images",
    "test_images_path": "/absolute/path/to/images"
}
```
Dedans vous devez spécifier l'ip et le port sur lesquels le serveur va démarrer.

Vous devez aussi spécifier les chemin absolue vers les dossier drone_images et images respectivements pour la sauvegarde des images dans un environnement de production et dans un environnement de test.
---

### 8. Configurer mapbox
Vous devez créer un compte MapBox sur https://www.mapbox.com/ afin d'avoir accès aux fonctionnalités concernant l'affichage d'une carte de fond.

Lorsque vous avez créer un compte, cliquer sur l'icone en haut à droite pour accèder à votre compte (bouton "Account").
Dans cette interface, vous trouverez un bouton "Tokens" au niveau de la barre de navigation en haut de l'écran.

Vous aurez un token public par défaut, qu'il vous faudra copier, pour le coller dans le fichier string.xml présent dans le dossier /watch/app/src/main/res/values au niveau de la variable "mapbox_access_token".
Créez un token secret qu'il vous faudra copier, et coller dans le fichier gradle.properties dans le dossier /watch au niveau de la variable "MAPBOX_DOWNLOADS_TOKEN".

## Lancement du projet

## Lancer le serveur
### Avec sphinx
Lancez Sphinx d'abord.

Placez vous à la racine du projet puis :

```shell
cd server/Server
python3 OlympeServer.py
```
### Sans sphinx
Placez vous à la racine du projet puis :

```shell
cd server/Server
python3 BasicServer.py
```
## Lancer l'application sur la montre

1. Ouvrir Android Studio
2. Open an Existing Project
3. Sélectionner le dossier **watch** du projet
4. Vérifier que Android Studio détecte le projet comme "app" dans la barre en haut à droite
5. Si nécessaire sélectionner comme device la montre créée précedemment
6. Lancer l'application (Flèche verte)