# Democracia Paraguaya

Aplicación web basada en Spring Boot para consultar y analizar información pública sobre candidatos y personas postuladas a cargos públicos en Paraguay.

> **Estado del proyecto:** en desarrollo. La plataforma busca organizar información biográfica y profesional, postulaciones, propuestas, noticias, testimonios y fuentes verificables. No debe interpretarse un análisis generado por IA como un hecho comprobado.

## Tecnologías

- Java 17
- Spring Boot 3.2.5
- Maven (se incluye Maven Wrapper)
- React 18 mediante CDN y Babel Standalone
- Jackson para serialización JSON
- Jsoup para extracción HTML
- Selenium para automatización web
- Google Gemini API para análisis asistido por IA
- Cloudflare Tunnel para publicar temporalmente el servidor
- Telegram Bot API para notificar cambios de URL

## Requisitos

- JDK 17.
- Conexión a Internet para descargar dependencias y utilizar servicios externos.
- Una clave de Google Gemini para ejecutar análisis con IA.
- Cloudflared instalado y disponible en PATH si se habilita el túnel.
- Token y chat ID de Telegram si se desean notificaciones.

## Configuración

La aplicación lee la configuración desde variables de entorno y `src/main/resources/application.properties`.

1. Copia `.env.example` como referencia para tus variables.
2. Define las variables en el entorno desde el que ejecutarás Spring Boot.
3. No subas archivos `.env`, claves ni tokens al repositorio.

**Importante:** Spring Boot no carga archivos `.env` automáticamente. La plantilla sirve como referencia; debes exportar las variables o configurarlas en tu IDE.

### Variables disponibles

| Variable | Obligatoria | Descripción |
|---|---:|---|
| GEMINI_API_KEY | Para análisis con IA | Clave de Google Gemini |
| DEMOPY_STORAGE_PATH | No | Directorio donde se guardan los JSON; por defecto `./data` |
| CLOUDFLARE_ENABLED | No | Activa el túnel; por defecto false |
| CLOUDFLARE_EXECUTABLE | Si se activa Cloudflare | Ruta o nombre del ejecutable; por defecto cloudflared |
| CLOUDFLARE_ORIGIN | Si se activa Cloudflare | Servicio local publicado; por defecto http://127.0.0.1:8080 |
| TELEGRAM_BOT_TOKEN | No | Token del bot de Telegram |
| TELEGRAM_CHAT_ID | No | Chat que recibirá las notificaciones |

Las integraciones Cloudflare y Telegram son opcionales. Sin las credenciales de Telegram, la aplicación omite el envío de notificaciones. Sin Cloudflare habilitado, Spring Boot no inicia el proceso del túnel.

### Windows PowerShell

```powershell
$env:GEMINI_API_KEY = "tu_clave"
$env:DEMOPY_STORAGE_PATH = ".\data"
$env:CLOUDFLARE_ENABLED = "false"
$env:CLOUDFLARE_EXECUTABLE = "cloudflared"
$env:CLOUDFLARE_ORIGIN = "http://127.0.0.1:8080"
$env:TELEGRAM_BOT_TOKEN = ""
$env:TELEGRAM_CHAT_ID = ""
```

Para habilitar Cloudflare:

```powershell
$env:CLOUDFLARE_ENABLED = "true"
```

Si `cloudflared.exe` no está en PATH, asigna su ruta completa a `CLOUDFLARE_EXECUTABLE`.

### IntelliJ IDEA

En **Run → Edit Configurations → Environment variables**, define las mismas variables para la configuración de ejecución.

## Almacenamiento JSON

La primera versión utiliza archivos JSON independientes, sin base de datos. El directorio de datos se configura mediante `DEMOPY_STORAGE_PATH` y, por defecto, se crea como `./data` en el directorio desde el que se ejecuta la aplicación.

Estructura prevista:

```text
data/
├── personas/
│   └── jorge-schreiner/
│       ├── perfil.json
│       ├── trayectoria.json
│       ├── postulaciones.json
│       ├── propuestas.json
│       ├── testimonios.json
│       └── analisis.json
├── noticias/
│   └── 2026/11/
│       └── noticia-<id>.json
└── metadata/
    └── ultima-recoleccion.json
```

- Cada archivo contiene una entidad o colección acotada; no se acumulan todas las noticias dentro del perfil.
- Las fuentes deben conservar URL, título y fechas relevantes para rastrear el origen de los datos.
- Los testimonios deben distinguir opinión de hecho comprobado y registrar procedencia y consentimiento cuando corresponda.
- El directorio `data/` está excluido de Git porque puede contener información personal o material recolectado. Configura copias de seguridad por separado.
- En servidores, utiliza un volumen o directorio persistente. Si el almacenamiento del servidor es efímero, los archivos podrían perderse al reiniciar o redeplegar.
- `JsonFileStorageService` centraliza lectura y escritura, valida rutas relativas y escribe mediante archivo temporal antes de reemplazar el destino.

## Ejecutar

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

La aplicación escucha por defecto en http://localhost:8080.

## Pruebas

Windows:

```powershell
.\mvnw.cmd clean test
```

Linux/macOS:

```bash
./mvnw clean test
```

## Estructura principal

```text
src/
├── main/
│   ├── java/com/demopy/
│   │   ├── DemopyApplication.java
│   │   ├── controller/
│   │   ├── model/
│   │   ├── scraper/
│   │   ├── service/
│   │   └── storage/
│   │       └── JsonFileStorageService.java
│   └── resources/
│       ├── static/index.html
│       ├── application.properties
│       └── candidatos.json
└── test/
    └── java/com/demopy/
        └── storage/
            └── JsonFileStorageServiceTest.java
```

## Limitaciones y criterios

- La recolección de comentarios de Instagram, Facebook y X no está implementada.
- Los resultados de scraping pueden ser incompletos y dependen de las condiciones de acceso de cada fuente.
- La IA puede cometer errores; toda afirmación relevante debe conservar su fuente y revisarse antes de publicarse.
- No se deben publicar testimonios sin evaluar su procedencia, consentimiento, contexto y posibles riesgos para las personas mencionadas.

## Seguridad

- Nunca guardes claves API, tokens ni archivos `.env` en Git.
- Si una credencial fue publicada o compartida, revócala y genera una nueva.
- No habilites el túnel público salvo que realmente necesites exponer la aplicación.
