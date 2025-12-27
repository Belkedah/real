# Raid Mod - Mod Minecraft Forge 1.20.1

Un mod qui ajoute un système de raid basé sur des vagues avec des fonctionnalités avancées.

## Fonctionnalités

### Système de Raid
- **Raids aléatoires jour/nuit** : Les raids peuvent commencer à tout moment
- **Un seul raid actif** : Un seul raid peut être actif à la fois pour tous les joueurs
- **Système de vagues** : Les raids se composent de plusieurs vagues de mobs
- **Difficulté progressive** : La difficulté augmente avec le nombre de jours écoulés

### Comportement des Mobs
- **Spawn aléatoire** : Spawn de mobs hostiles variés (pas de boss)
- **Pathfinding intelligent** : Les mobs peuvent casser les murs pour atteindre les joueurs ou les lits
- **Création de chemins** : Les mobs créent des chemins escaladables si nécessaire
- **Priorité aux lits** : Les mobs ciblent les lits (points de respawn) en priorité
- **Évitement de pièges** : Les mobs évitent les obstacles et pièges
- **Effet lumineux** : Les mobs brillent si le raid dure trop longtemps

### Variantes de Mobs
- **Mobs normaux** : Mobs standards avec buffs de base
- **Mobs Élites** : Plus forts, plus rares (15% de chance par défaut)
- **Mobs Couronnés** : Les plus puissants, très rares (5% de chance par défaut)

### Système de Récompenses
- **Récompenses aléatoires** : Récompenses variées à la victoire
- **Échelle avec les vagues** : Plus de récompenses pour les vagues supérieures
- **Types de récompenses** : Diamants, émeraudes, or, fer, nourriture, etc.

### Autres Fonctionnalités
- **Réparation automatique** : Les murs cassés sont réparés après le raid
- **Notifications** : Messages en jeu pour les événements de raid
- **Configuration** : Configuration complète via fichier de config Forge
- **Détection de brèche** : Notification si les mobs atteignent la cible

## Installation

1. Installez **Minecraft Forge 1.20.1** (version 47.2.0)
2. Compilez le mod (voir instructions ci-dessous) ou téléchargez une version pré-compilée
3. Placez le fichier `.jar` dans le dossier `mods` de votre installation Minecraft
4. Lancez Minecraft avec le profil Forge

## Compilation

### Prérequis
- **Java 17 JDK** (téléchargez depuis [Adoptium](https://adoptium.net/))
- Java doit être dans votre PATH ou JAVA_HOME doit être défini

### Compilation automatique
Exécutez simplement :
```bash
compile.bat
```

### Compilation manuelle
```bash
.\gradlew.bat build
```

Le fichier `.jar` sera généré dans `build\libs\raidmod-1.0.0.jar`

## Configuration

Le mod peut être configuré via le fichier de configuration Forge :
**Chemin** : `.minecraft\config\raidmod-common.toml`

### Options disponibles

| Option | Description | Valeur par défaut |
|--------|-------------|-------------------|
| `enableModdedMobs` | Activer les mobs moddés dans les raids | `true` |
| `minRaidInterval` | Intervalle minimum entre les raids (ticks) | `12000` (10 min) |
| `maxRaidInterval` | Intervalle maximum entre les raids (ticks) | `24000` (20 min) |
| `baseMobCount` | Nombre de base de mobs par vague | `5` |
| `difficultyScaling` | Multiplicateur de difficulté par jour | `1.1` |
| `eliteChance` | Chance d'apparition des mobs élites (0.0-1.0) | `0.15` |
| `crownedChance` | Chance d'apparition des mobs couronnés (0.0-1.0) | `0.05` |
| `glowDuration` | Durée avant que les mobs brillent (ticks) | `6000` (5 min) |
| `mobsBreakWalls` | Autoriser les mobs à casser les murs | `true` |
| `mobsCreatePaths` | Autoriser les mobs à créer des chemins | `true` |

## Structure du Code

```
src/main/java/com/belkedouch/raidmod/
├── RaidMod.java              # Point d'entrée principal
├── config/
│   └── RaidConfig.java       # Configuration du mod
├── raid/
│   ├── RaidManager.java      # Gestionnaire de raids
│   └── RaidInstance.java     # Instance de raid individuelle
├── mobs/
│   ├── RaidMobHandler.java   # Gestion des mobs de raid
│   └── RaidMoveToTargetGoal.java  # IA de mouvement des mobs
├── rewards/
│   └── RewardManager.java    # Gestion des récompenses
└── network/
    └── NetworkHandler.java   # Gestion du réseau (pour futures fonctionnalités)
```

## Notes Techniques

- Le mod utilise le système d'événements Forge pour s'intégrer au cycle de jeu
- Les raids sont synchronisés au niveau du serveur
- Les blocs cassés sont stockés pour réparation automatique
- Le système de pathfinding est optimisé pour les performances

## Licence

MIT License - Libre d'utilisation et de modification

## Support

Pour signaler des bugs ou suggérer des fonctionnalités, créez une issue sur le dépôt du projet.


