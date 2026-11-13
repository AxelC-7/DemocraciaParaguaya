# Democracia Paraguaya

Aplicación web basada en Spring Boot para consultar y analizar información pública sobre figuras y candidatos de Paraguay.

> **Estado del proyecto:** en desarrollo. El análisis con Gemini todavía no cuenta con un flujo completo de búsqueda y verificación de fuentes. La aplicación no realiza actualmente una extracción real de comentarios de Instagram, Facebook o X. No interpretes los resultados generados por IA como hechos verificados.

## Tecnologías

- Java 17
- Spring Boot 3.2.5
- Maven (se incluye Maven Wrapper)
- React 18 mediante CDN y Babel Standalone
- Jsoup para extracción HTML
- Selenium para automatización web
- Google Gemini API para generación de análisis
- Cloudflare Tunnel para publicar temporalmente el servidor
- Telegram Bot API para notificar cambios de URL

## Requisitos

- JDK 17.
- Conexión a Internet para descargar dependencias y utilizar servicios externos.
- Una clave de Google Gemini para ejecutar consultas de investigación con IA.
- Cloudflared instalado y disponible en PATH si se habilita el túnel.
- Token y chat ID de Telegram si se desean notificaciones.

## Configuración

La aplicación lee la configuración desde variables de entorno y src/main/resources/application.properties.

1. Copia .env.example como referencia para tus variables.
2. Define las variables en el entorno desde el que ejecutarás Spring Boot.
3. No subas archivos .env, claves ni tokens al repositorio.

**Importante:** Spring Boot no carga archivos .env automáticamente. La plantilla sirve como referencia; debes exportar las variables o configurarlas en tu IDE.

### Variables disponibles

| Variable | Obligatoria | Descripción |
|---|---:|---|
| GEMINI_API_KEY | Para investigar | Clave de Google Gemini |
| CLOUDFLARE_ENABLED | No | Activa el túnel; por defecto false |
| CLOUDFLARE_EXECUTABLE | Si se activa Cloudflare | Ruta o nombre del ejecutable; por defecto cloudflared |
| CLOUDFLARE_ORIGIN | Si se activa Cloudflare | Servicio local publicado; por defecto http://127.0.0.1:8080 |
| TELEGRAM_BOT_TOKEN | No | Token del bot de Telegram |
| TELEGRAM_CHAT_ID | No | Chat que recibirá las notificaciones |

Las integraciones Cloudflare y Telegram son opcionales. Sin las credenciales de Telegram, la aplicación omite el envío de notificaciones. Sin Cloudflare habilitado, Spring Boot no inicia el proceso del túnel.

### Windows PowerShell

Define las variables en la misma ventana donde ejecutarás la aplicación:

~~~powershell
$env:GEMINI_API_KEY = "tu_clave"
$env:CLOUDFLARE_ENABLED = "false"
$env:CLOUDFLARE_EXECUTABLE = "cloudflared"
$env:CLOUDFLARE_ORIGIN = "http://127.0.0.1:8080"
$env:TELEGRAM_BOT_TOKEN = ""
$env:TELEGRAM_CHAT_ID = ""
~~~

Para habilitar Cloudflare:

~~~powershell
$env:CLOUDFLARE_ENABLED = "true"
~~~

Si cloudflared.exe no está en PATH, asigna su ruta completa a CLOUDFLARE_EXECUTABLE.

### IntelliJ IDEA

En **Run → Edit Configurations → Environment variables**, define las mismas variables para la configuración de ejecución.

## Ejecutar

Windows:

~~~powershell
.\mvnw.cmd spring-boot:run
~~~

Linux/macOS:

~~~bash
./mvnw spring-boot:run
~~~

La aplicación escucha por defecto en http://localhost:8080.

## Pruebas

Windows:

~~~powershell
.\mvnw.cmd clean test
~~~

Linux/macOS:

~~~bash
./mvnw clean test
~~~

## Estructura actual

La siguiente estructura corresponde a los directorios y clases actualmente publicados:

~~~text
src/
├── main/
│   ├── java/com/demopy/
│   │   ├── DemopyApplication.java
│   │   ├── controller/
│   │   │   ├── CandidatoController.java
│   │   │   └── WebController.java
│   │   ├── model/
│   │   │   └── Candidato.java
│   │   ├── scraper/
│   │   │   └── GoogleNewsScraper.java
│   │   └── service/
│   │       ├── CandidatoService.java
│   │       ├── CloudflareTunnelService.java
│   │       ├── GeminiModelDiscoveryService.java
│   │       ├── ScrapingService.java
│   │       └── TelegramNotificationService.java
│   └── resources/
│       ├── static/index.html
│       ├── application.properties
│       └── candidatos.json
└── test/
    └── java/com/demopy/
        ├── DemopyApplicationTests.java
        └── service/
            └── ScrapingServiceGroundingTest.java
~~~

### Responsabilidades principales

- controller/: endpoints HTTP y entrega de la interfaz.
- model/: modelo del candidato, fuentes web y citas asociadas a fragmentos respaldados.
- scraper/: extracción HTML; el scraper actual consulta resultados HTML de DuckDuckGo, no Google News.
- service/: lógica de aplicación e integración con Gemini, Cloudflare y Telegram.
- resources/static/: interfaz web servida por Spring Boot.

## Limitaciones funcionales conocidas

- ScrapingService utiliza Gemini con Google Search Grounding. Cuando Google devuelve metadatos de fundamentación, la API conserva los enlaces y títulos de las fuentes y la interfaz los muestra.
- La interfaz muestra las afirmaciones vinculadas a sus fuentes, el listado agregado de fuentes y el bloque de sugerencias de búsqueda de Google cuando viene incluido en la respuesta.
- La presencia de fuentes depende de que Gemini devuelva groundingMetadata; una respuesta sin esos metadatos no debe considerarse respaldada por fuentes.
- No hay integración que recolecte comentarios reales de Instagram, Facebook o X. La aplicación indica que no evalúa sentimiento ni calcula porcentajes.
- GoogleNewsScraper no está conectado al flujo principal de investigación y actualmente consulta resultados HTML de DuckDuckGo.

Google Search Grounding mejora la trazabilidad, pero no garantiza que cada afirmación sea correcta. Revisa las fuentes originales, especialmente para acusaciones, procesos judiciales, cargos públicos y datos sensibles. El uso de búsqueda puede estar sujeto a cuotas y condiciones de facturación de Google.

## Seguridad

- Nunca guardes claves API, tokens ni archivos .env en Git.
- .env.example contiene únicamente nombres de variables y valores de ejemplo no sensibles.
- Si una credencial fue publicada o compartida, revócala y genera una nueva. Eliminarla del archivo no la invalida ni la elimina del historial de Git.
- No habilites el túnel público salvo que realmente necesites exponer la aplicación.
