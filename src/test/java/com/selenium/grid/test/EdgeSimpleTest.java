package com.selenium.grid.test;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Test simple SOLO para verificar que Edge funciona
 * Usa página estática para evitar problemas de selectores
 */
public class EdgeSimpleTest {

    private WebDriver driver;
    private String hubUrl = "http://localhost:4444/wd/hub";

    @BeforeMethod
    public void setUp() throws MalformedURLException {
        System.out.println("🚀 Configurando Edge para test simple...");

        EdgeOptions edgeOptions = new EdgeOptions();
        edgeOptions.addArguments("--disable-dev-shm-usage");
        edgeOptions.addArguments("--no-sandbox");
        edgeOptions.addArguments("--disable-gpu");
        edgeOptions.addArguments("--window-size=1920,1080");
        edgeOptions.addArguments("--disable-blink-features=AutomationControlled");

        try {
            driver = new RemoteWebDriver(new URL(hubUrl), edgeOptions);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().window().maximize();

            System.out.println("✅ Edge conectado exitosamente");
            System.out.println("📋 Browser: " +
                    ((RemoteWebDriver) driver).getCapabilities().getBrowserName() +
                    " v" + ((RemoteWebDriver) driver).getCapabilities().getBrowserVersion());
        } catch (Exception e) {
            System.err.println("❌ Error conectando Edge: " + e.getMessage());
            throw e;
        }
    }

    @Test(priority = 1)
    public void testEdgeBasicConnection() {
        System.out.println("🧪 Test 1: Verificando conexión básica de Edge");

        // Cambia a una página muy estable
        driver.get("https://example.com");

        String title = driver.getTitle();
        System.out.println("📋 Título de la página: " + title);

        Assert.assertNotNull(title, "El título no debería ser null");
        Assert.assertFalse(title.isEmpty(), "El título no debería estar vacío");

        System.out.println("✅ Test 1 completado - Edge puede cargar páginas");
    }

    @Test(priority = 2)
    public void testEdgeNavigation() {
        System.out.println("🧪 Test 2: Verificando navegación de Edge");

        // Navegar a una página de estado HTTP
        driver.get("https://httpbin.org/status/200");

        String currentUrl = driver.getCurrentUrl();
        System.out.println("🔗 URL actual: " + currentUrl);

        Assert.assertTrue(currentUrl.contains("httpbin.org"),
                "URL debería contener httpbin.org");

        System.out.println("✅ Test 2 completado - Edge puede navegar correctamente");
    }

    @Test(priority = 3)
    public void testEdgeCapabilities() {
        System.out.println("🧪 Test 3: Verificando capacidades de Edge");

        RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;

        String browserName = remoteDriver.getCapabilities().getBrowserName();
        String browserVersion = remoteDriver.getCapabilities().getBrowserVersion();
        String platformName = remoteDriver.getCapabilities().getPlatformName().toString();

        System.out.println("🌐 Navegador: " + browserName);
        System.out.println("📊 Versión: " + browserVersion);
        System.out.println("💻 Plataforma: " + platformName);

        Assert.assertNotNull(browserName, "Browser name no debería ser null");
        Assert.assertTrue(browserName.toLowerCase().contains("edge") ||
                        browserName.toLowerCase().contains("microsoftedge"),
                "Debería ser Edge: " + browserName);

        System.out.println("✅ Test 3 completado - Capacidades de Edge verificadas");
    }

    @Test(priority = 4)
    public void testEdgeExample() {
        System.out.println("🧪 Test 4: Verificando example.com (página súper estable)");

        driver.get("https://example.com");

        String title = driver.getTitle();
        String pageSource = driver.getPageSource();

        System.out.println("📋 Título: " + title);

        Assert.assertTrue(title.contains("Example Domain"),
                "Título debería contener 'Example Domain'");
        Assert.assertTrue(pageSource.contains("Example Domain"),
                "Página debería contener texto 'Example Domain'");

        System.out.println("✅ Test 4 completado - Edge funciona con example.com");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            try {
                System.out.println("🧹 Cerrando Edge...");
                driver.quit();
                System.out.println("✅ Edge cerrado correctamente");
            } catch (Exception e) {
                System.err.println("⚠️ Error cerrando Edge: " + e.getMessage());
            }
        }
    }

    public String getBrowserInfo() {
        if (driver instanceof RemoteWebDriver) {
            RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
            return remoteDriver.getCapabilities().getBrowserName() +
                    " v" + remoteDriver.getCapabilities().getBrowserVersion();
        }
        return "Unknown browser";
    }
}