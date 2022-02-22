# Multi agent Treasure hunter

## Phase I – Exploration

**Objectif :** Explorer le plus rapidement possible la plus grande partie du terrain possible. \
**Contraintes :** Minimiser les données échangées. (Cela implique une augmentation de la complexité & de la quantité de données stockées).\
**Exemple :** 
1. Échanger uniquement les informations non-encore transmise à cet agent.
2. Échanger uniquement les donnés récoltés par l’agent.

**Critères de transition :** Pas d’apport massif de transition. Chaque agent a rencontré tous les autres / une grande majorité (~95%)\
Une fois le critère de transition atteint, on définit un palier de remplissage ( Ratio entre : capacité du sac / capacité totale des sacs / quantité de ressource) & un type de ressource (Trésor / Diamant).


## Phase II – Récolte

**Objectif :** Récolter des trésors jusqu’à atteindre le palier de l’agent\
Chaque agent se rend vers le trésor qui a été sélectionné par son algorithme (critères de distance, capacité de sac (comparé à celle des autres), cluster).\
**Communications :** réduire les communications aux modifications mineurs du terrain. Redirection des agents lorsqu’ils se rendent sur le même trésor / cluster.\
**Critère de transition :** Un agent passe à la phase altruiste lorsque son sac à dos atteint le palier défini lors de la phase d’exploration. L’agent a terminer son graphe et connaît l’entièreté du terrain.\
Calcul du meilleur cluster de nœuds (extremum local en terme de nombre de transitions).

## Phase III – Altruisme / Finitions

**Objectif :** Indiquer aux autres agents l’emplacement des trésors / diamants restants. Leur indiquer que le travail est fini.\
**Critère de fin :** L’agent initiale a croiser tous les autres agents. Il envoie le dernier agent prévenir l’agent qu’il avait envoyé au second cluster ainsi de suite.
