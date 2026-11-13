package com.demopy;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class IPSClient {

    public JSONArray especialidades = new JSONArray();
    public JSONArray localidades = new JSONArray();
    public JSONArray turnos = new JSONArray();

    public void IPSCliente(CookieManager cookieManager, HttpCookie cookie) throws Exception {

        // Agregamos PHPSESSID que ya tenés
        HttpCookie phpSession = cookie;
        phpSession.setDomain("servicios.ips.gov.py");
        phpSession.setPath("/");
        cookieManager.getCookieStore().add(URI.create("https://servicios.ips.gov.py"), phpSession);

        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();

        // --- 2. Preparar body del POST ---
        String body = "accion=actualizarDatoAsegurado"
                + "&cod_asegurado=5389649%23TITULAR"
                + "&celular=0976972220";

        // --- 3. Crear request ---
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://servicios.ips.gov.py/miips/ajax/ajx_agendamiento_miips.php"))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Referer", "https://servicios.ips.gov.py/miips/agendamiento_cita.php")
                .header("Origin", "https://servicios.ips.gov.py")
                .header("User-Agent", "Mozilla/5.0")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        // --- 4. Enviar request ---
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // --- 5. Imprimir respuesta ---
        System.out.println("=== RESPUESTA 0: Actualizar Asegurado ===");
        System.out.println(response.body());
    }

    public void ListarEspecialidades(CookieManager cookieManager, HttpCookie cookie) throws Exception {

        System.out.println("=== RESPUESTA 1: Listar Especialidades ===");
        HttpCookie phpSession = cookie;
        phpSession.setDomain("servicios.ips.gov.py");
        phpSession.setPath("/");
        cookieManager.getCookieStore().add(URI.create("https://servicios.ips.gov.py"), phpSession);

        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();

        // --- 2. Preparar body del POST ---
        // IMPORTANTE: id_sucursal y token_sesion deben copiarse del Network del navegador
        // Por ahora ponemos ejemplo estático
        String body = "accion=ListarEspecialidades";

        // --- 3. Crear request ---
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://servicios.ips.gov.py/miips/ajax/ajx_agendamiento_miips.php"))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Referer", "https://servicios.ips.gov.py/miips/agendamiento_cita.php")
                .header("Origin", "https://servicios.ips.gov.py")
                .header("User-Agent", "Mozilla/5.0")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        // --- 4. Enviar request ---
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // --- 5. Imprimir respuesta ---
        //System.out.println(response.body());
        JSONObject resp = new JSONObject(response.body());
        especialidades = resp.getJSONArray("data");
    }

    public void ListarLocalidades(CookieManager cookieManager, HttpCookie cookie) throws Exception {

        System.out.println("=== RESPUESTA 2: Listar Localidades ===");
        for (int i = 0; i < especialidades.length(); i++) {
            JSONObject esp = especialidades.getJSONObject(i);
            int idEsp = esp.getInt("id_especialidad");
            String idEspSih = esp.getString("cod_sih");
            String nombreEsp = esp.getString("nombre");

            HttpCookie phpSession = cookie;
            phpSession.setDomain("servicios.ips.gov.py");
            phpSession.setPath("/");
            cookieManager.getCookieStore().add(URI.create("https://servicios.ips.gov.py"), phpSession);

            HttpClient client = HttpClient.newBuilder()
                    .cookieHandler(cookieManager)
                    .build();

            // --- 2. Preparar body del POST ---
            // IMPORTANTE: id_sucursal y token_sesion deben copiarse del Network del navegador
            // Por ahora ponemos ejemplo estático
            String body = "accion=ListarHospitales&id_especialidad=" + idEsp;

            // --- 3. Crear request ---
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://servicios.ips.gov.py/miips/ajax/ajx_agendamiento_miips.php"))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Referer", "https://servicios.ips.gov.py/miips/agendamiento_cita.php")
                    .header("Origin", "https://servicios.ips.gov.py")
                    .header("User-Agent", "Mozilla/5.0")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            // --- 4. Enviar request ---
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // --- 5. Imprimir respuesta ---
            //System.out.println(response.body());
            JSONObject resp = new JSONObject(response.body());
            JSONArray tlocalidades = resp.getJSONArray("data");

            // Crear objeto especialidad con array vacío de localidades
            JSONObject espObj = new JSONObject();
            espObj.put("id_especialidad", idEsp);
            espObj.put("id_especialidad_sih", idEspSih);
            espObj.put("nombre", nombreEsp);
            espObj.put("localidades", tlocalidades); // agrego el array dentro del objeto

            localidades.put(espObj); // agrego el objeto al array principal
        }
    }

    public void ListarTurnos(CookieManager cookieManager, HttpCookie cookie) throws Exception {

        System.out.println("=== RESPUESTA 3: Listar Turnos ===");
        for (int i = 0; i < localidades.length(); i++) {
            JSONObject esp = localidades.getJSONObject(i);
            int idEsp = esp.getInt("id_especialidad");
            String idEspSih = esp.getString("id_especialidad_sih");
            String nombreEsp = esp.getString("nombre");

            JSONArray loc = esp.getJSONArray("localidades");

            for (int l = 0; l < loc.length(); l++) {
                JSONObject locc = loc.getJSONObject(l);
                int idEmp = locc.getInt("id_empresa");
                String nombreEmp = locc.getString("nombre");

                HttpCookie phpSession = cookie;
                phpSession.setDomain("servicios.ips.gov.py");
                phpSession.setPath("/");
                cookieManager.getCookieStore().add(URI.create("https://servicios.ips.gov.py"), phpSession);

                HttpClient client = HttpClient.newBuilder()
                        .cookieHandler(cookieManager)
                        .build();

                // --- 2. Preparar body del POST ---
                // IMPORTANTE: id_sucursal y token_sesion deben copiarse del Network del navegador
                // Por ahora ponemos ejemplo estático
                String body = "accion=ListarTurnos&id_empresa=" + idEmp + "&id_especialidad=" + idEsp + "&especialidad_sih=" + idEspSih;

                // --- 3. Crear request ---
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://servicios.ips.gov.py/miips/ajax/ajx_agendamiento_miips.php"))
                        .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Referer", "https://servicios.ips.gov.py/miips/agendamiento_cita.php")
                        .header("Origin", "https://servicios.ips.gov.py")
                        .header("User-Agent", "Mozilla/5.0")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                // --- 4. Enviar request ---
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // --- 5. Imprimir respuesta ---
                //System.out.println(response.body());
                JSONObject resp = new JSONObject(response.body());
                if(resp.has("data")){
                    JSONObject oturnos = resp.getJSONObject("data");
                    JSONArray tturnos = oturnos.getJSONArray("turnos");

                    // Crear objeto especialidad con array vacío de localidades
                    JSONObject espObj = new JSONObject();
                    espObj.put("id_especialidad", idEsp);
                    espObj.put("nombre_esp", nombreEsp);
                    espObj.put("id_empresa", idEmp);
                    espObj.put("nombre_emp", nombreEmp);
                    espObj.put("turnos", tturnos); // agrego el array dentro del objeto

                    turnos.put(espObj); // agrego el objeto al array principal
                }
            }
        }

        System.out.println(turnos.toString());
    }
}
