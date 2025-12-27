# Instructions de compilation du mod Raid Mod

## Prérequis

1. **Java 17** (JDK 17) - Téléchargez depuis [Adoptium](https://adoptium.net/) ou [Oracle](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
2. **Gradle** (inclus via wrapper)

## Installation de Java

### Windows
1. Téléchargez Java 17 JDK
2. Installez-le
3. Ajoutez Java au PATH ou définissez JAVA_HOME :
   - Ouvrez les Variables d'environnement
   - Ajoutez `JAVA_HOME` pointant vers le dossier d'installation (ex: `C:\Program Files\Java\jdk-17`)
   - Ajoutez `%JAVA_HOME%\bin` au PATH

## Compilation

Une fois Java 17 installé, exécutez :

```bash
.\gradlew.bat build
```

Le fichier .jar sera généré dans : `build\libs\raidmod-1.0.0.jar`

## Installation du mod

1. Installez Minecraft Forge 1.20.1 (version 47.2.0)
2. Placez le fichier .jar dans le dossier `mods` de votre installation Minecraft
3. Lancez Minecraft avec le profil Forge

## Configuration

Le mod peut être configuré via le fichier de configuration Forge :
- Chemin : `.minecraft\config\raidmod-common.toml`

Options disponibles :
- `enableModdedMobs` : Activer les mobs moddés
- `minRaidInterval` : Intervalle minimum entre les raids (en ticks)
- `maxRaidInterval` : Intervalle maximum entre les raids (en ticks)
- `baseMobCount` : Nombre de base de mobs par vague
- `difficultyScaling` : Multiplicateur de difficulté par jour
- `eliteChance` : Chance d'apparition des mobs élites
- `crownedChance` : Chance d'apparition des mobs couronnés
- `glowDuration` : Durée avant que les mobs brillent (en ticks)
- `mobsBreakWalls` : Autoriser les mobs à casser les murs
- `mobsCreatePaths` : Autoriser les mobs à créer des chemins


