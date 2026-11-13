package com.demopy.automation;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumWebInteraction {

    public static void main(String[] args) {
        try {
            // NOTA IMPORTANTE: Debes cambiar esta ruta por la ubicación real de tu ChromeDriver.
            // En versiones recientes de Selenium, a veces no es necesario establecer esta propiedad
            // ya que gestiona los drivers automáticamente, pero es buena práctica indicarla si tienes problemas.
            //System.setProperty("webdriver.chrome.driver", "C:\\Users\\AXEL\\Downloads\\chromedriver-win64\\chromedriver.exe");
            //ChromeOptions options = new ChromeOptions();
            // Ejecutar Chrome en segundo plano sin abrir ventana (comenta esta línea si quieres ver el navegador)
            //options.addArguments("--headless=new");
            /*options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});*/

            // Nota: Para el Modo IA es recomendable iniciar sesión o usar un perfil existente de Chrome
            //options.addArguments("user-data-dir=C:/Users/AXEL/AppData/Local/Google/Chrome/User Data");


            WebDriver driver = new ChromeDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            // 2. Navegar a la página web
            String consulta = "explica la historia de la inteligencia artificial en 3 puntos";
            String encodedQuery = URLEncoder.encode(consulta, StandardCharsets.UTF_8);

            // 1. Abrir directamente el Modo IA mediante el parámetro udm=50
            driver.get("https://www.google.com/search?q=paraguay&udm=50");
            // Imprimimos la URL para confirmar
            System.out.println("Página actual: " + driver.getCurrentUrl());
            // 3. Opcional: Aceptar la política de cookies si aparece (ejemplo para Google)
            // Esto es muy común y esencial para la automatización.
            try {
                WebElement acceptButton = driver.findElement(By.className("plR5qb"));
                acceptButton.click();
            } catch (Exception e) {
                System.out.println("No se encontró el botón de cookies o ya fue aceptado.");
            }
            //Escribir campo
            /*WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("gLFyf")));
            searchBox.sendKeys("Paraguay");*/
            //((JavascriptExecutor) driver).executeScript("arguments[0].click();", botonModoIA);
            //searchBox.submit();
/*
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("leftFrame"));
*/
            // By linkSelector = By.xpath("//a[normalize-space()='Agenda']");
            /*WebElement linkNuevaCita = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.linkText("Agenda"))
            );
            linkNuevaCita.click();*/

            // By linkSelector = By.xpath("//a[normalize-space()='Agenda']");
            /*WebElement nuevaCita = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.linkText("Nueva Cita"))
            );
            nuevaCita.click();*/

            //wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("mainFrame"));
            //Seleccionar un checkbox
            /*WebElement idAseg = driver.findElement(By.id("id_asegurado"));
            Select SidAseg = new Select(idAseg);
            SidAseg.selectByValue("5389649#TITULAR");*/
            System.out.println("Búsqueda completada y esperando...");

            //Cookie
            /*Set<org.openqa.selenium.Cookie> seleniumCookies = driver.manage().getCookies();

            CookieManager cookieManager = new CookieManager();
            HttpCookie cookie = null;
            for (org.openqa.selenium.Cookie c : seleniumCookies) {
                if (c.getName().equals("PHPSESSID")) {
                    cookie = new HttpCookie(c.getName(), c.getValue());
                    cookie.setDomain("servicios.ips.gov.py");
                    cookie.setPath("/");
                    cookie.setVersion(0);
                    cookieManager.getCookieStore().add(
                            URI.create("https://servicios.ips.gov.py"),
                            cookie
                    );
                }
            }*/

            //IPSClient c = new IPSClient();
            //c.IPSCliente(cookieManager, cookie);
            //c.ListarEspecialidades(cookieManager, cookie);
            //c.ListarLocalidades(cookieManager, cookie);
            //c.ListarTurnos(cookieManager, cookie);

        } catch (Exception ex) {
            Logger.getLogger(SeleniumWebInteraction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
