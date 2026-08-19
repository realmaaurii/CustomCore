# CustomCore

Minecraft-Plugin für Paper 1.21 mit Custom Scoreboard, Custom Rängen,
Chatcolors, Custom Tablist – alles per Command/GUI editierbar – plus einer
JavaScript-Scripting-Schicht (GraalJS) zur weiteren Anpassung ohne
Neukompilierung.

## Bauen

### Option A: Lokal (JDK 21 + Maven nötig)

```bash
mvn clean package
```

Die fertige Jar liegt danach unter `target/CustomCore.jar` und kommt in den
`plugins`-Ordner deines Paper-1.21-Servers.

### Option B: Automatisch per GitHub Actions (kein lokales Java/Maven nötig)

Im Projekt liegt bereits `.github/workflows/build.yml`. So nutzt du sie:

1. Ein neues (privates oder öffentliches) Repository auf GitHub anlegen.
2. Diesen kompletten `CustomCore`-Ordner in das Repo pushen, z. B.:
   ```bash
   cd CustomCore
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/DEIN-NAME/DEIN-REPO.git
   git push -u origin main
   ```
3. Auf GitHub unter dem Reiter **Actions** läuft der Build automatisch los
   (Symbol wird gelb, dann grün, dauert ca. 1–2 Minuten).
4. Nach erfolgreichem Lauf: auf den Workflow-Lauf klicken → ganz unten bei
   **Artifacts** liegt `CustomCore-Plugin` zum Download bereit – das ist die
   fertige JAR, die du in `plugins/` legst.

Bei jedem weiteren Push wird automatisch neu gebaut, du musst also nach
Änderungen nur committen und pushen.

## Commands & Permissions

| Command | Berechtigung | Funktion |
|---|---|---|
| `/scoreboard edit` | `customcore.scoreboard.admin` (default: op) | Öffnet den Inventory-GUI-Editor für Zeilen & Titel |
| `/scoreboard reload` | s.o. | Lädt `scoreboard.yml` neu |
| `/scoreboard toggle` | s.o. | Blendet dein eigenes Scoreboard ein/aus |
| `/scoreboard title <text>` | s.o. | Titel direkt per Command setzen |
| `/scoreboard setline <nr> <text>` | s.o. | Einzelne Zeile per Command setzen |
| `/scoreboard addline <text>` / `removeline <nr>` | s.o. | Zeile hinzufügen/entfernen |
| `/rank create <id> <name>` | `customcore.rank.admin` (default: op) | Neuen Rang anlegen |
| `/rank edit <id> <prefix\|suffix\|chatcolor\|displayname\|weight> <wert>` | s.o. | Rang bearbeiten |
| `/rank set <spieler> <rangid>` | s.o. | Spieler einen Rang zuweisen |
| `/rank delete <id>` / `list` | s.o. | Rang löschen / auflisten |
| `/tablist edit` | `customcore.tablist.admin` (default: op) | Chat-Wizard für Header/Footer |
| `/tablist reload` | s.o. | Lädt `tablist.yml` neu |
| `/chatcolor <code>` | `customcore.chatcolor.use` (default: false) | Eigene Chatfarbe wählen (muss in config.yml erlaubt sein) |
| `/ccscript reload\|list\|run <datei>\|unload <datei>` | `customcore.script.admin` (default: op) | JS-Skripte verwalten |

Alle Admin-Permissions sind standardmäßig `op` – vergib sie über
LuckPerms/Vault gezielt an Moderatoren, falls gewünscht, statt vollen OP zu geben.

## Platzhalter

Aktuell unterstützt: `%player_name%`, `%server_online%`, `%customcore_rank%`.
Für mehr Platzhalter (z. B. Balance, Playtime) kannst du PlaceholderAPI als
optionale Abhängigkeit einbinden und `PlaceholderAPI.setPlaceholders(player, line)`
in `ScoreboardManager#parsePlaceholders` und `TablistManager#render` aufrufen.

## JavaScript-Skripte

Skripte liegen in `plugins/CustomCore/scripts/*.js` und werden beim Start
automatisch geladen (abschaltbar in `config.yml`: `scripts.auto-load`).
Jedes Skript läuft in einer eigenen Sandbox ohne Zugriff auf Java-Reflection,
Dateisystem oder Prozesse – die einzige Schnittstelle ist das globale
`Server`-Objekt (siehe `ScriptAPI.java`). Neue Methoden fügst du dort mit
`@HostAccess.Export` hinzu.

Beispiel (`example.js`):

```js
Server.log("Hallo aus dem Skript!");
Server.setScoreboardTitle("&b&lSCRIPTED SERVER");
Server.broadcast("&aDas Skript läuft!");
```

## Nächste sinnvolle Ausbaustufen

- **Rank-Edit-GUI**: aktuell nur Command-basiert, ließe sich analog zum
  Scoreboard-Editor als Inventory-GUI umsetzen.
- **PlaceholderAPI-Anbindung** für mehr Variablen.
- **LuckPerms-Integration** statt eigenem Rang-System, falls du bereits
  LuckPerms nutzt (dann übernimmt LuckPerms Permissions, dieses Plugin nur
  noch Prefix/Suffix/Farbe).
- **Persistente Spielerdaten in SQLite/MySQL** statt YAML, falls dein Server
  wächst (YAML skaliert bei vielen Spielern schlecht).
