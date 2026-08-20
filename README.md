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
| `/crate give <spieler> <crateid> [anzahl]` | `customcore.crate.admin` (default: op) | Crate-Key an Spieler geben |
| `/crate list` / `reload` | s.o. | Crates auflisten / `crates.yml` neu laden |
| `/credits [spieler]` | jeder | Eigenes oder fremdes Guthaben anzeigen |
| `/credits give\|set\|take <spieler> <betrag>` | `customcore.credits.admin` (default: op) | Credits verwalten |
| `/pay <spieler> <betrag>` | jeder | Credits an einen anderen Spieler senden |
| `/store` | jeder | Öffnet den Credits-Shop |

## Credits-Wirtschaft & Store

Ein eigenständiges Guthabensystem (keine Abhängigkeit von Vault nötig) -
Guthaben wird in `plugins/CustomCore/economy.yml` gespeichert. Spieler
prüfen ihr Guthaben mit `/credits`, überweisen sich gegenseitig Geld mit
`/pay <spieler> <betrag>`.

`/store` öffnet ein Inventory-Menü mit kaufbaren Einträgen, konfiguriert in
`plugins/CustomCore/store.yml`:

```yaml
items:
  - id: crate_common
    icon: ENDER_CHEST
    display-name: "&aCommon Crate Key"
    description: "&7Öffnet eine Common Crate"
    price: 500
    action: CRATE_KEY
    crate-id: common
    crate-amount: 1

  - id: rank_vip
    icon: DIAMOND
    display-name: "&b&lVIP Rang"
    description: "&7Schaltet den VIP-Rang frei"
    price: 5000
    action: RANK
    rank-id: vip
```

`action` kann sein:
- `ITEM` - gibt ein Item (`item-material`, `item-amount`)
- `RANK` - setzt den Rang des Spielers (`rank-id`, muss in `ranks.yml` existieren)
- `CRATE_KEY` - gibt einen Crate-Key (`crate-id`, `crate-amount`)
- `COMMAND` - führt einen Befehl als Konsole aus (`command`, `%player%` wird ersetzt)

Crates können außerdem direkt Credits als Belohnung ausschütten
(`type: CREDITS` in `crates.yml`, siehe unten) - so lässt sich ein
kompletter Kreislauf aus Spielen → Credits verdienen → im Store ausgeben
bauen.

## Crate-System ("Wähle 6 von 8")

Ein Spieler mit einem Crate-Key (standardmäßig eine benannte Enderchest,
per `/crate give <spieler> common` erhältlich) rechtsklickt damit auf den
Boden. Um ihn herum erscheinen 8 normale Kisten im Kreis. Klickt er eine
an, verschwindet sie sofort und er bekommt zufällig eine Belohnung aus dem
Pool (gewichtete Zufallsauswahl) – kein Kisten-Inventar zum Durchsuchen,
die Belohnung wird direkt ausgezahlt. Nach 6 angeklickten Kisten
verschwinden die restlichen 2 automatisch und der Boden wird wieder frei.
Falls eine Crate 45 Sekunden lang nicht abgeschlossen wird, wird sie
automatisch zurückgesetzt (falls der Spieler offline geht o. Ä.).

Belohnungen werden in `plugins/CustomCore/crates.yml` konfiguriert:

```yaml
crates:
  common:
    display-name: "&aCommon Crate"
    key-material: ENDER_CHEST
    rewards:
      - id: diamonds
        type: ITEM
        display-name: "&b5 Diamanten"
        weight: 10
        category: "&5&lEPIC ITEMS"
        material: DIAMOND
        amount: 5
      - id: xp
        type: COMMAND
        display-name: "&d150 XP"
        weight: 6
        category: "&5&lEPIC ITEMS"
        command: "xp add %player% 150"
```

`weight` bestimmt die Chance relativ zu den anderen Einträgen (höher =
wahrscheinlicher) - die Prozentangabe im Key-Tooltip wird automatisch
daraus berechnet. `category` ist optional und gruppiert die Anzeige im
Tooltip des Crate-Keys (z. B. "LEGENDARY ITEMS", "EPIC ITEMS", "COMMON
ITEMS") - alle Belohnungen mit derselben Kategorie werden zusammen unter
einer farbigen Überschrift angezeigt, ähnlich wie bei bekannten
Crate-Plugins. Ohne `category` landen Einträge in einer Sammelgruppe
"WEITERE BELOHNUNGEN". `type` kann `ITEM`, `CREDITS` (mit `credits: <zahl>`)
oder `COMMAND` sein - bei `COMMAND` wird der Befehl als Konsole ausgeführt,
`%player%` wird durch den Spielernamen ersetzt.

Die 8 Kisten erscheinen als Enderchests in 4 Paaren um den Spieler herum
(Norden/Osten/Süden/Westen), mit einer sichtbaren Lücke zwischen den
beiden Kisten pro Richtung. Dafür wird auf jeder Seite ca. 3-4 Blöcke
freier Platz benötigt.

Nach Änderungen an der Datei reicht `/crate reload` (bereits ausgeteilte
Keys zeigen die alte Lore weiter an - neu vergebene Keys per `/crate give`
zeigen den aktuellen Stand).

Du kannst beliebig viele eigene Crate-Typen unter `crates.<id>` in der
Datei hinzufügen (z. B. `crates.legendary`) – jeder erhält automatisch
sein eigenes Key-Item über `/crate give <spieler> <id>`.

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
