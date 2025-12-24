# 🔗 URL Shortener API

Eine performante und persistente REST-API zum Kürzen von URLs, entwickelt mit **Java 25** und **Spring Boot 4**.
Das Projekt nutzt einen **Base62-Algorithmus**, um kurze, url-freundliche Strings zu generieren, und speichert die Zuordnungen dauerhaft in einer dateibasierten H2-Datenbank.

## 🚀 Technologien

* **Java SDK:** 25.0.1
* **Framework:** Spring Boot 4.0.1
* **Build Tool:** Maven
* **Datenbank:** H2 Database (File-based Persistence)
* **Validierung:** Hibernate Validator
* **Tools:** Lombok

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
* **Body:** Die zu kürzende URL (muss mit `http://` oder `https://` beginnen).

**Beispiel (Curl):**
```bash
curl -X POST -H "Content-Type: application/json" -d '{"url": "https://www.github.com"}' http://localhost:8080/
```

**Beispiel (PowerShell):**

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/" -Body '{"url": "https://www.github.com"}' -ContentType "application/json"
```

**Antwort (201 Created):**
```powershell
{
  "shortUrl": "http://localhost:8080/aX",
  "originalUrl": "https://www.github.com"
}
```

---

### 2. URL auflösen (Redirect)

Leitet den Browser zur originalen URL weiter.

* **URL:** `GET /{shortCode}`

**Beispiel:**
Aufruf im Browser: `http://localhost:8080/aX`

**Ergebnis:**
Weiterleitung (302 Found) zu `https://www.google.de`.

---

### 3. Fehlerbehandlung

Die API liefert saubere HTTP-Statuscodes zurück:

* **400 Bad Request:** Wenn keine gültige URL übergeben wurde (z.B. "Banane").
* **404 Not Found:** Wenn der Short-Code nicht existiert.

## 🗄️ Datenbank-Zugriff (H2 Console)

Um direkt in die Datenbank zu schauen, ist die H2-Konsole aktiviert.

1. Rufe im Browser auf: `http://localhost:8080/h2-console`
2. **JDBC URL:** `jdbc:h2:file:./data/shortener`
3. **User Name:** `username`
4. **Password:** `password`
