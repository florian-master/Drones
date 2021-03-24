# Opérations clients 
### Connexion
```json
{
    "op":"connexion",
    "userId": "poe58zef-zefze54"
}

```
### Demande de controle de drone
```json
{
    "op":"requestControl",
    "userId": "poe58zef-zefze54"
}
```

### Rendre la main sur le drone (il deviendra impossible de le controller)
```json
{
    "op":"giveBackControl",
    "userId": "poe58zef-zefze54"
}
```

### Décoller
```json
{
    "op":"takeOff",
    "userId": "poe58zef-zefze54"
}
```

### Atterrir
```json
{
    "op":"landing",
    "userId": "poe58zef-zefze54"
}
```


### Envoyer localisation
```json
{
    "op":"location",
    "longitude": 0.0,
    "latitude": 0.0,
    "time": 1614697079,
    "userId": "poe58zef-zefze54"
}

```
### Envoyer une liste de localisations 
```json
{
    "op":"locations",
    "locations": [
        {
            "longitude": 0.0,
            "latitude": 0.0,
        }
    ],
    "userId": "poe58zef-zefze54"
}

```
### Envoyer une position marquée
```json
{
    "op":"mark",
    "longitude": 0.0,
    "latitude": 0.0,
    "color": "red",
    "userId": "poe58zef-zefze54"
}

```
### Envoyer une liste de positions marquées 
```json
{
    "op":"marks",
    "locations": [
        {
            "longitude": 0.0,
            "latitude": 0.0,
            "color":"red",
        }
    ],
    "userId": "poe58zef-zefze54"
}

```

### Envoyer une liste de positions marquées 
```json
{
    "op":"getMarkedLocations",
    "userId": "poe58zef-zefze54"
}

```


### Demander de faire une ou plusieurs photos
```json
{
    "op":"takePhoto",
    "locations":[
        {
            "longitude": 0.0,
            "latitude": 0.0,
            "altitude": 2,
        }
    ],
    "userId": "poe58zef-zefze54"
}

```

### Demander de récupérer les coordonnées des autres
```json
{
    "op":"getLocations",
    "userId": "poe58zef-zefze54"
}

```


### Demander de récupérer toutes les infos 
```json
{
    "op":"getInfos",
    "userId": "poe58zef-zefze54"
}

```

### Envoyer le drone à un certain point sur la map
```json
{
    "op":"flyTo",
    "longitude": 0.0,
    "latitude": 0.0,
    "altitude":2,
    "userId": "poe58zef-zefze54"
}

```

### Envoyer le drone vers une autre montre
```json
{
    "op":"flyToWatch",
    "userId":"poe58zef-zefze54",
    "dstId":"poe58zef-zefze55"
}
```

### Envoyer une demande de standby au drone 
```json
{
    "op":"standby",
    "userId": "poe58zef-zefze54"
}

```

### Envoyer une demande de partage d'informations 
```json
{
    "op":"share",
    "userId": "poe58zef-zefze54"
}

```


--------
# Opération Serveur
## Envoyer l'id au client
```json

{
    "op":"connect",
    "userId": "0a508f87-7d2a-11eb-ac57-00155de1b113"
}

```
### Envoyer l'ensemble des localisations 
```json
{
	"op": "locations",
	"clients":[
		{
			"id": 0,
			"locations": [
				{
					"longitude": 0.0,
					"latitude": 0.0
				}
			]
		}
	]
}

```

### Envoyer l'ensemble des infos
```json
{
	"op": "getInfos",
	"clients":[
		{
			"id": 0,
			"locations": [
				{
					"longitude": 0.0,
					"latitude": 0.0
				}
			]
		}
	],
    "locations": [
        {
            "longitude": 0.0,
            "latitude": 0.0,
            "color":"red",
        }
    ],
    "waiting": 1,
    "connected":1,
}

```

### Le nombre d'image que le client va recevoir 
```json
{
    "op":"picture",
    "nbPictures":2,
}
```

### Envoyer une notification (signaler qu'il va partir) 
```json
{
    "op":"notification",
    "notification": "I'm going to leave"
}

```

### Réaction en cas d'erreur ( erreur dans le json par exemple, informations incorrectes)
```json
{
    "op":"error",
    "message":"Error message"
}

```
### Message 
```json
{
    "op":"message",
    "message":"message"
}

```
### Envoyer une liste de positions marquées au client
```json
{
    "op":"marks",
    "locations": [
        {
            "longitude": 0.0,
            "latitude": 0.0,
            "color":"red",
        }
    ]
}

```
