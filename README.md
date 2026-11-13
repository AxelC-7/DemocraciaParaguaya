# Democracia Paraguaya

Aplicación web basada en Spring Boot para consultar y analizar información pública sobre figuras y candidatos.

## Requisitos

- Java 17
- Maven (o el Maven Wrapper incluido)
- Credenciales de Google Gemini si se utiliza el análisis con IA
- Credenciales de Telegram si se desean recibir notificaciones del túnel
- Cloudflared instalado y accesible desde PATH, o configurar su ruta absoluta

## Configuración

1. Copia `.env.example` a un archivo `.env` (no lo subas al repositorio).
2. Completa las variables necesarias.
3. Exporta esas variables en el entorno desde el que ejecutarás la aplicación. Spring Boot no carga automáticamente archivos `.env`.

Variables disponibles:

- `GEMINI_API_KEY`
- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_CHAT_ID`
- `CLOUDFLARE_EXECUTABLE`
- `CLOUDFLARE_ORIGIN`

En Windows PowerShell puedes definirlas para la sesión actual con `$env:NOMBRE="valor"`.

## Ejecutar

En Windows:

```powershell
.\\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```bash
./mvnw spring-boot:run
```

La aplicación escucha por defecto en el puerto `8080`.

## Pruebas

```bash
./mvnw test
```

En Windows utiliza `.\\mvnw.cmd test`.

## Estructura actual

```
src/
  main/
    java/com/demopy/
      automation/  # Utilidades de automatización con Selenium
      controller/   # Endpoints web y API
      model/        # Modelos de dominio
      scraper/      # Extracción de información
      service/      # Lógica de aplicación e integraciones
    resources/
      static/       # Interfaz web
      application.properties
      candidatos.json
  test/
    java/com/demopy/
```

El paquete raíz `com.demopy` se mantiene para evitar una migración masiva de paquetes sin necesidad funcional. Los siguientes cambios de organización deben hacerse tras revisar las referencias y responsabilidades de cada clase.

## Seguridad

No guardes tokens, claves API ni archivos `.env` en Git. Si una credencial se publicó alguna vez, revócala y genera una nueva: eliminarla del archivo no la elimina del historial de Git.
