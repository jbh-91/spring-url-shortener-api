# 🔗 URL Shortener API

Eine performante und persistente REST-API zum Kürzen von URLs, entwickelt mit **Java 25** und **Spring Boot 4**.
Das Projekt nutzt einen **Base62-Algorithmus**, um kurze, url-freundliche Strings zu generieren, und speichert die Zuordnungen dauerhaft in einer dateibasierten H2-Datenbank.

Die Anwendung ist für nutzt atomare Datenbank-Updates für Statistiken und unterstützt **PWA-Features** (App-Icons).

## 🚀 Technologien

* **Java SDK:** 25.0.1
* **Framework:** Spring Boot 4.0.1
* **Build Tool:** Maven
* **Datenbank:** H2 Database (File-based Persistence)
* **Validierung:** Hibernate Validator
* **Tools:** Lombok
* **Frontend-Assets:** Webmanifest & Favicons (PWA support)

## ⚙️ Setup & Konfiguration

Die Anwendung ist so konfiguriert, dass sie "Out of the Box" läuft. Die Konfiguration befindet sich in `src/main/resources/application.properties`.

### Wichtige Einstellungen
| Property | Wert (Standard) | Beschreibung |
| :--- | :--- | :--- |
| `server.port` | `8080` | Der Port, auf dem die API läuft. |
| `spring.datasource.url` | `jdbc:h2:file:./data/shortener` | Der Pfad, wo die Datenbankdatei erstellt und verwendet werden soll. |
| `spring.datasource.username` | `username` | Benutzername für die H2 Datenbank und Adminkonsole. |
| `spring.datasource.password` | `password` | Passwort für die H2 Datenbank und Adminkonsole. |
| `spring.jpa.hibernate.ddl-auto` | `update` | Erstellt das Datenbankschema bei Änderungen automatisch neu, behält die Daten aber bei. |
| `app.baseurl` | `http://localhost` | Die Basis-URL, die dem Short-Code vorangestellt wird. _(z.B. http://mydomain.de)_ |
| `app.default-ttl-hours` | `0` | Die Default TTL für die Erstellung der Short-URLs in Stunden. _(0=unendlich)_ |
| `app.cleanup.cron` | `0 0 3 * * *` | Cron-Ausdruck für den automatischen Bereinigungs-Job abgelaufener URLs. (_Standard: Täglich 03:00 Uhr)_ |

## 🛠️ Installation & Start

1.  **Repository klonen:**
    ```bash
    git clone "https://github.com/jbh-91/spring-url-shortener-api"
    ```
2.  **Bauen und Starten (via Maven-Wrapper):**
    ```bash
    .\mvnw spring-boot:run
    ```

Nach dem Start ist die API unter `http://localhost:8080` erreichbar.
Die Datenbank-Datei wird automatisch im Ordner `./data/` angelegt.

## 📡 API Endpoints

### 1. URL kürzen
Erstellt einen neuen Short-Link für eine lange URL.

* **URL:** `POST /`
* **Content-Type:** `application/json` 
* **Body Parameter:**
    * `url` (String, Pflicht): Die zu kürzende URL (muss mit `http://` oder `https://` beginnen).
    * `hoursTTL` (Integer, Optional): Die Gültigkeitsdauer in Stunden. Wenn weggelassen oder `0`, ist der Link **unbegrenzt** gültig.

#### Beispiel-Anfragen
**Beispiel (Curl) - Unbegrenzt gültig:**
```bash
curl -X POST -H "Content-Type: application/json" -d '{"url": "https://www.github.com"}' http://localhost:8080/
```
**Beispiel (PowerShell) - Unbegrenzt gültig:**
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/" -Body '{"url": "https://www.github.com"}' -ContentType "application/json"
```

**Beispiel (Curl) - Gültig für 24 Stunden:**
```bash
curl -X POST -H "Content-Type: application/json" -d '{"url": "https://www.github.com", "hoursTTL": 24}' http://localhost:8080/
```

**Beispiel (PowerShell) - Gültig für 24 Stunden:**
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/" -Body '{"url": "https://www.github.com", "hoursTTL": 24}' -ContentType "application/json"
```
#### Beispiel-Antwort
**Antwort (201 Created):**
```json
{
  "shortUrl": "http://localhost:8080/aX",
  "originalUrl": "https://www.github.com",
  "expiresAt": "2025-12-31T23:59:59" 
}
```
*(Hinweis: `expiresAt` ist null, wenn keine TTL gesetzt wurde.)*

---

### 2. URL auflösen (Redirect)

Leitet den Browser zur originalen URL weiter.

* **URL:** `GET /{shortCode}`
    * **Constraint:** shortCode darf nur alphanumerische Zeichen enthalten ([a-zA-Z0-9]+).

**Beispiel:**
Aufruf im Browser: `http://localhost:8080/aX`

**Antwortverhalten:**
* **302 Found:** Weiterleitung zur Original-URL.
* **400 Bad Request:** Ungültige URL oder Formatfehler (z.B. bei "Spam" statt einer gültigen URL).
* **404 Not Found:** Der Short-Code existiert nicht.
* **410 Gone:** Der Short-Code existiert, ist aber abgelaufen (TTL expired).

### 3. Statistiken abrufen

Liefert Metadaten und Zugriffszahlen zu einem Link.

* **URL:** `GET /stats/{shortCode}`
    * **Constraint:** shortCode darf nur alphanumerische Zeichen enthalten ([a-zA-Z0-9]+).

#### Beispiel-Antwort
**Antwort (200 OK):**
```json
{
    "originalUrl": "https://www.github.com",
    "accessCount": 42,
    "lastAccessed": "2026-01-04T12:00:00",
    "expiresAt": null,
    "isExpired": false
}
```

### 4. Link löschen

Entfernt eine Verknüpfung manuell aus der Datenbank.

* **URL:** `DELETE /{shortCode}`
    * **Constraint:** shortCode darf nur alphanumerische Zeichen enthalten ([a-zA-Z0-9]+).

**Antwortverhalten:**

* **204 No Content**
---

## 🧹 Automatische Bereinigung
Damit die Datenbank nicht unbegrenzt mit "toten" Einträgen wächst, verfügt die Anwendung über einen integrierten **Cleanup-Job.**
- Dieser läuft im Hintergrund und löscht alle URLs aus der Datenbank, deren Haltbarkeitsdatum (`expiresAt`) überschritten ist.
- Der Zeitplan ist über die Property `app.cleanup.cron` konfigurierbar. _(Standard: Täglich um 03:00 Uhr nachts)_

## 🗄️ Datenbank-Zugriff (H2 Console)

Um direkt in die Datenbank zu schauen, ist die H2-Konsole aktiviert.

1. Rufe im Browser auf: `http://localhost:8080/h2-console`
2. **JDBC URL:** `jdbc:h2:file:./data/shortener`
3. **User Name:** `username`
4. **Password:** `password`
