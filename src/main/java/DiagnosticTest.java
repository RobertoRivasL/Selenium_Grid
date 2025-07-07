
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.JavascriptExecutor;

import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Script de Diagnóstico para Selenium Grid
 * Identifica qué selectores están disponibles en las páginas web
 */
public class DiagnosticTest {

    public static void main(String[] args) throws Exception {
        String hubUrl = "http://localhost:4444/wd/hub";

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage", "--no-sandbox");

        WebDriver driver = new RemoteWebDriver(new URL(hubUrl), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        try {
            System.out.println("🔍 DIAGNÓSTICO DE SELECTORES SELENIUM GRID");
            System.out.println("==========================================");

            // Diagnóstico Google
            diagnoseGoogle(driver);

            // Diagnóstico Selenium.dev
            diagnoseSeleniumDev(driver);

            // Diagnóstico DuckDuckGo
            diagnoseDuckDuckGo(driver);

        } finally {
            driver.quit();
        }
    }

    private static void diagnoseGoogle(WebDriver driver) throws InterruptedException {
        System.out.println("\n🌐 DIAGNÓSTICO GOOGLE");
        System.out.println("----------------------");

        driver.get("https://www.google.com");
        Thread.sleep(3000);

        System.out.println("📋 Título: " + driver.getTitle());
        System.out.println("🌐 URL: " + driver.getCurrentUrl());

        // Verificar cookies
        System.out.println("\n🍪 Verificando botones de cookies:");
        checkSelector(driver, "#L2AGLb", "Botón cookies principal");
        checkSelector(driver, "button[id='L2AGLb']", "Botón cookies alternativo");
        checkSelector(driver, "button:contains('Accept')", "Botón por texto 'Accept'");

        // Verificar campos de búsqueda
        System.out.println("\n🔍 Verificando campos de búsqueda:");
        checkSelector(driver, "input[name='q']", "Campo búsqueda por name");
        checkSelector(driver, "textarea[name='q']", "Campo búsqueda textarea");
        checkSelector(driver, "#APjFqb", "Campo búsqueda por ID nuevo");

        // Intentar búsqueda
        try {
            WebElement searchBox = driver.findElement(By.name("q"));
            searchBox.sendKeys("test");
            searchBox.submit();
            Thread.sleep(3000);

            System.out.println("\n📊 Verificando contenedores de resultados:");
            checkSelector(driver, "#search", "Contenedor resultados clásico");
            checkSelector(driver, "#rso", "Resultados orgánicos");
            checkSelector(driver, ".g", "Resultados individuales");
            checkSelector(driver, "#main", "Contenedor principal");
            checkSelector(driver, ".MjjYud", "Formato nuevo");

        } catch (Exception e) {
            System.out.println("❌ No se pudo realizar búsqueda: " + e.getMessage());
        }
    }

    private static void diagnoseSeleniumDev(WebDriver driver) throws InterruptedException {
        System.out.println("\n🌐 DIAGNÓSTICO SELENIUM.DEV");
        System.out.println("----------------------------");

        driver.get("https://www.selenium.dev");
        Thread.sleep(3000);

        System.out.println("📋 Título: " + driver.getTitle());
        System.out.println("🌐 URL: " + driver.getCurrentUrl());

        // Verificar presencia de contenido Selenium
        String pageSource = driver.getPageSource().toLowerCase();
        System.out.println("✅ Contiene 'selenium': " + pageSource.contains("selenium"));
        System.out.println("✅ Contiene 'webdriver': " + pageSource.contains("webdriver"));
        System.out.println("✅ Contiene 'automate': " + pageSource.contains("automate"));

        // Verificar selectores comunes
        System.out.println("\n🔍 Verificando elementos de navegación:");
        checkSelector(driver, "nav", "Elemento nav");
        checkSelector(driver, ".navbar", "Clase navbar");
        checkSelector(driver, "header", "Elemento header");
        checkSelector(driver, ".header", "Clase header");

        // Verificar logos/imágenes
        System.out.println("\n🖼️ Verificando imágenes/logos:");
        checkSelector(driver, "img[alt*='Selenium']", "Logo por alt text");
        checkSelector(driver, "img[src*='selenium']", "Logo por src");
        checkSelector(driver, ".logo", "Clase logo");

        // Ejecutar JavaScript para encontrar imágenes
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object result = js.executeScript(
                    "return Array.from(document.images).map(img => ({src: img.src, alt: img.alt}));"
            );
            System.out.println("🖼️ Imágenes encontradas: " + result);
        } catch (Exception e) {
            System.out.println("❌ Error ejecutando JavaScript: " + e.getMessage());
        }
    }

    private static void diagnoseDuckDuckGo(WebDriver driver) throws InterruptedException {
        System.out.println("\n🌐 DIAGNÓSTICO DUCKDUCKGO");
        System.out.println("-------------------------");

        driver.get("https://duckduckgo.com");
        Thread.sleep(3000);

        System.out.println("📋 Título: " + driver.getTitle());
        System.out.println("🌐 URL: " + driver.getCurrentUrl());

        // Verificar campos de búsqueda
        System.out.println("\n🔍 Verificando campos de búsqueda:");
        checkSelector(driver, "#search_form_input_homepage", "Campo búsqueda original");
        checkSelector(driver, "#searchbox_input", "Campo búsqueda alternativo");
        checkSelector(driver, "input[name='q']", "Campo por name");
        checkSelector(driver, "input[placeholder*='Search']", "Campo por placeholder");
        checkSelector(driver, ".js-search-input", "Campo por clase");

        // Verificar botones
        System.out.println("\n🔘 Verificando botones:");
        checkSelector(driver, "#search_button_homepage", "Botón búsqueda original");
        checkSelector(driver, "button[type='submit']", "Botón submit");
        checkSelector(driver, ".search-btn", "Botón por clase");

        // Intentar búsqueda
        try {
            List<String> searchSelectors = Arrays.asList(
                    "#search_form_input_homepage",
                    "#searchbox_input",
                    "input[name='q']",
                    "input[placeholder*='Search']"
            );

            WebElement searchBox = null;
            for (String selector : searchSelectors) {
                try {
                    searchBox = driver.findElement(By.cssSelector(selector));
                    System.out.println("✅ Campo de búsqueda encontrado: " + selector);
                    break;
                } catch (Exception e) {
                    continue;
                }
            }

            if (searchBox != null) {
                searchBox.sendKeys("test");
                searchBox.submit();
                Thread.sleep(3000);

                System.out.println("\n📊 Verificando contenedores de resultados:");
                checkSelector(driver, "#links", "Contenedor resultados clásico");
                checkSelector(driver, ".results", "Contenedor resultados");
                checkSelector(driver, "[data-testid='result']", "Resultados por data-testid");
                checkSelector(driver, ".result", "Resultados individuales");
                checkSelector(driver, "#web_content", "Contenido web");
            }

        } catch (Exception e) {
            System.out.println("❌ No se pudo realizar búsqueda: " + e.getMessage());
        }
    }

    private static void checkSelector(WebDriver driver, String selector, String description) {
        try {
            List<WebElement> elements = driver.findElements(By.cssSelector(selector));
            if (elements.size() > 0) {
                System.out.println("✅ " + description + " (" + selector + ") - " + elements.size() + " elementos");
                if (elements.size() == 1) {
                    WebElement element = elements.get(0);
                    System.out.println("   📝 Texto: '" + element.getText().substring(0, Math.min(50, element.getText().length())) + "'");
                    System.out.println("   👁️ Visible: " + element.isDisplayed());
                    System.out.println("   🖱️ Clickeable: " + element.isEnabled());
                }
            } else {
                System.out.println("❌ " + description + " (" + selector + ") - NO ENCONTRADO");
            }
        } catch (Exception e) {
            System.out.println("❌ " + description + " (" + selector + ") - ERROR: " + e.getMessage());
        }
    }
}