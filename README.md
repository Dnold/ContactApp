# ContactApp - Android Projekt

## Projektbeschreibung
Android-Anwendung für Kontaktverwaltung mit QR-Code-Scanner und Kamera-Integration.

## Systemvoraussetzungen
- **Android Studio**: Version 2023.1.1 (Hedgehog) oder neuer
- **Java Development Kit (JDK)**: Version 11 oder höher
- **Android SDK**: API Level 24-35
- **Gradle**: Version wird automatisch von Android Studio verwaltet
- **Kotlin**: Version 1.9.x (wird über Gradle verwaltet)

## Projekterstellung und Einrichtung

### 1. Projekt importieren
1. Android Studio öffnen
2. "Open an existing Android Studio project" wählen
3. Den entpackten Projektordner auswählen
4. Android Studio lädt das Projekt und synchronisiert Gradle automatisch

### 2. Abhängigkeiten
Das Projekt verwendet folgende externe Abhängigkeiten, die automatisch über Gradle heruntergeladen werden:

#### Compose & UI
- Jetpack Compose BOM
- Material Design 3
- Navigation Compose
- Coil (Bildladen)

#### Kamera & QR-Code
- CameraX (androidx.camera:*)
- ML Kit Barcode Scanning
- ZXing QR-Code Scanner
- Accompanist Permissions

#### Datenbank & Netzwerk
- Room Database mit KSP Compiler
- Retrofit mit Gson Converter

### 3. Gradle Sync
Falls die automatische Synchronisation fehlschlägt:
1. In Android Studio: `File` → `Sync Project with Gradle Files`
2. Oder Terminal verwenden: `./gradlew build`

### 4. Kompilieren und Ausführen

#### Option A: Über Android Studio IDE
1. Gerät/Emulator anschließen
2. "Run" Button (grüner Pfeil) klicken
3. Zielgerät auswählen

#### Option B: Über Kommandozeile
```bash
# Debug-Build erstellen
./gradlew assembleDebug

# App auf angeschlossenem Gerät installieren
./gradlew installDebug

# Tests ausführen
./gradlew test
```

## Besonderheiten

### KSP (Kotlin Symbol Processing)
Das Projekt verwendet KSP für Room Database Code-Generierung. Falls Probleme auftreten:
```bash
./gradlew clean
./gradlew build
```

### Berechtigungen
Die App benötigt folgende Berechtigungen (werden automatisch angefordert):
- `CAMERA`: Für QR-Code-Scanner und Foto-Aufnahme
- `READ_EXTERNAL_STORAGE`: Für Bildauswahl (falls implementiert)

### Minimum SDK
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 35 (Android 15)
- **Compile SDK**: API 35

## Fehlerbehebung

### Gradle Sync Probleme
```bash
# Gradle Cache leeren
./gradlew clean

# Gradle Wrapper neu herunterladen
./gradlew wrapper --gradle-version=8.2
```

### Build Fehler
1. Stellen Sie sicher, dass alle Abhängigkeiten verfügbar sind
2. Prüfen Sie die Internetverbindung für Gradle Downloads
3. Bei Room-Fehlern: Clean + Rebuild Project

### Emulator Setup
Falls kein physisches Gerät verfügbar:
1. `Tools` → `AVD Manager`
2. Neuen Virtual Device erstellen (API 24+)
3. Device starten und App ausführen

## Projektstruktur
```
app/
├── src/main/
│   ├── java/com/example/contactapp/
│   │   ├── MainActivity.kt
│   │   ├── data/          # Room Database, Entities
│   │   ├── ui/            # Compose UI Components
│   │   ├── camera/        # Kamera & QR-Scanner
│   │   └── network/       # Retrofit API Calls
│   ├── res/               # Resources (layouts, strings, etc.)
│   └── AndroidManifest.xml
├── build.gradle.kts       # App-Level Build Config
└── proguard-rules.pro     # ProGuard Regeln
```

## Hinweise zur Abgabe
- Vollständige Projektmappe in ZIP-Datei verpacken
- Alle Gradle-Dateien einschließen
- `build/` und `.gradle/` Ordner können ausgeschlossen werden
- Diese README.md in das Root-Verzeichnis des Projekts legen

## Support
Bei Problemen mit dem Projekt:
1. Gradle Clean & Rebuild versuchen
2. Android Studio neustarten
3. Gradle Sync Force durchführen