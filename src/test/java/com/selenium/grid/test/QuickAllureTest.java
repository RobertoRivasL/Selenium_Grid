package com.selenium.grid.test;

import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

@Epic("Selenium Grid Testing")
@Feature("Cross-Browser Smoke Tests - Chrome, Firefox y Edge")
public class QuickAllureTest {

    private static final String HUB_URL = "http://localhost:4444/wd/hub";
    private static final String SEARCH_TERM = "Selenium Grid Docker";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final Duration IMPLICIT_WAIT = Duration.ofSeconds(10);

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeMethod
    @Step("Configurando el entorno de pruebas")
    public void setUp() {
        System.out.println("🔧 Configurando el entorno de pruebas");
        Allure.addAttachment("Test Configuration",
                "Hub URL: " + HUB_URL + "\nTimeout: " + TIMEOUT + "\nSearch Term: " + SEARCH_TERM);
    }

    @AfterMethod
    @Step("Limpiando recursos")
    public void tearDown() {
        if (driver != null) {
            try {
                System.out.println("🧹 Limpiando recursos");
                Allure.addAttachment("Browser Info", getBrowserInfo());
                driver.quit();
            } catch (Exception e) {
                System.err.println("❌ Error en cleanup: " + e.getMessage());
                Allure.addAttachment("Cleanup Error", e.getMessage());
            }
        }
    }

    @Test(priority = 1)
    @Story("Pruebas en Chrome")
    @Description("Ejecuta pruebas completas de navegación y búsqueda en Chrome")
    @Severity(SeverityLevel.CRITICAL)
    public void testChrome() throws MalformedURLException {
        System.out.println("🚀 Iniciando test en Chrome");
        ejecutarTestCompleto("chrome");
    }

    @Test(priority = 2)
    @Story("Pruebas en Firefox")
    @Description("Ejecuta pruebas completas de navegación y búsqueda en Firefox")
    @Severity(SeverityLevel.CRITICAL)
    public void testFirefox() throws MalformedURLException {
        System.out.println("🚀 Iniciando test en Firefox");
        ejecutarTestCompleto("firefox");
    }

    @Test(priority = 3)
    @Story("Pruebas en Edge")
    @Description("Ejecuta pruebas completas de navegación y búsqueda en Edge")
    @Severity(SeverityLevel.CRITICAL)
    public void testEdge() throws MalformedURLException {
        System.out.println("🚀 Iniciando test en Edge");
        ejecutarTestCompleto("edge");
    }

    @Step("Ejecutando test completo en navegador: {browserName}")
    private void ejecutarTestCompleto(String browserName) throws MalformedURLException {
        System.out.println("📱 Ejecutando test en: " + browserName);

        // Inicializar driver
        driver = inicializarDriver(browserName);
        wait = new WebDriverWait(driver, TIMEOUT);

        // Configurar navegador
        configurarNavegador();

        // Ejecutar pruebas
        ejecutarPruebaGoogle();
        ejecutarPruebaSeleniumDev();

        // Agregar información final
        System.out.println("✅ Test completado exitosamente en " + browserName);
        Allure.addAttachment("Test Results",
                "Browser: " + browserName + "\nAll tests passed successfully");
    }

    @Step("Inicializando driver para {browserName}")
    private WebDriver inicializarDriver(String browserName) throws MalformedURLException {
        System.out.println("🔧 Inicializando driver para: " + browserName);

        switch (browserName.toLowerCase()) {
            case "chrome":
                return crearDriverChrome();
            case "firefox":
                return crearDriverFirefox();
            case "edge":
                return crearDriverEdge();
            default:
                throw new IllegalArgumentException("Navegador no soportado: " + browserName);
        }
    }

    private WebDriver crearDriverChrome() throws MalformedURLException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage", "--no-sandbox", "--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        return new RemoteWebDriver(new URL(HUB_URL), options);
    }

    private WebDriver crearDriverFirefox() throws MalformedURLException {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=1920", "--height=1080");
        options.addPreference("general.useragent.override",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        return new RemoteWebDriver(new URL(HUB_URL), options);
    }

    private WebDriver crearDriverEdge() throws MalformedURLException {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--disable-dev-shm-usage", "--no-sandbox", "--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        return new RemoteWebDriver(new URL(HUB_URL), options);
    }

    @Step("Configurando navegador")
    private void configurarNavegador() {
        System.out.println("⚙️ Configurando navegador");

        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT);
        driver.manage().window().maximize();

        String browserInfo = getBrowserInfo();
        System.out.println("📋 Info del navegador: " + browserInfo);
        Allure.addAttachment("Browser Configuration", browserInfo);
    }

    @Step("Ejecutando prueba en Google")
    private void ejecutarPruebaGoogle() {
        System.out.println("🌐 Ejecutando prueba en Google");

        // Navegar a Google
        Allure.step("Navegando a Google", () -> {
            System.out.println("🌐 Navegando a Google...");
            driver.get("https://www.google.com");
            String url = driver.getCurrentUrl();
            System.out.println("📍 URL actual: " + url);
            Allure.addAttachment("Google URL", url);
        });

        // Verificar carga inicial
        String initialTitle = driver.getTitle();
        System.out.println("📋 Título inicial: " + initialTitle);
        Allure.step("Verificando carga inicial: " + initialTitle);
        Assert.assertTrue(initialTitle.toLowerCase().contains("google"),
                "Google no cargó correctamente. Título: " + initialTitle);

        // Manejar cookies
        manejarCookiesGoogle();

        // Buscar input de búsqueda
        WebElement searchInput = buscarInputBusqueda();
        Assert.assertNotNull(searchInput, "No se encontró el input de búsqueda");

        // Realizar búsqueda
        Allure.step("Realizando búsqueda", () -> {
            System.out.println("🔍 Realizando búsqueda: " + SEARCH_TERM);
            searchInput.sendKeys(SEARCH_TERM);
            searchInput.submit();
        });

        // Verificar resultados
        verificarResultadosBusqueda();
    }

    @Step("Ejecutando prueba en Selenium Dev")
    private void ejecutarPruebaSeleniumDev() {
        System.out.println("🚀 Ejecutando prueba en Selenium Dev");

        Allure.step("Navegando a Selenium.dev", () -> {
            driver.get("https://www.selenium.dev/");
            System.out.println("📍 Navegando a: https://www.selenium.dev/");
        });

        // Verificar título
        String title = driver.getTitle();
        System.out.println("📋 Título de Selenium.dev: " + title);
        Allure.step("Verificando título: " + title);
        Assert.assertTrue(title.toLowerCase().contains("selenium"),
                "El título debería contener 'selenium'. Título actual: " + title);

        // Buscar logo o imagen representativa
        WebElement logo = buscarLogoSelenium();
        Assert.assertNotNull(logo, "No se encontró el logo o imagen de Selenium");

        System.out.println("✅ Prueba en Selenium.dev completada exitosamente");
        Allure.step("Prueba en Selenium.dev completada");
    }

    private void manejarCookiesGoogle() {
        String[] cookieSelectors = {
                "#L2AGLb",
                "button[id='L2AGLb']",
                ".QS5gu",
                "button:contains('Acepto')",
                "button:contains('Accept all')"
        };

        for (String selector : cookieSelectors) {
            try {
                WebElement acceptButton = wait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(selector))
                );
                if (acceptButton.isDisplayed()) {
                    acceptButton.click();
                    System.out.println("✅ Cookies aceptadas");
                    return;
                }
            } catch (Exception e) {
                // Continuar con el siguiente selector
            }
        }
        System.out.println("ℹ️ No se encontró diálogo de cookies");
    }

    private WebElement buscarInputBusqueda() {
        String[] inputSelectors = {
                "input[name='q']",
                "textarea[name='q']",
                "#APjFqb",
                ".gLFyf",
                "input[type='search']"
        };

        for (String selector : inputSelectors) {
            try {
                WebElement input = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))
                );
                if (input.isDisplayed()) {
                    System.out.println("✅ Input de búsqueda encontrado");
                    return input;
                }
            } catch (Exception e) {
                // Continuar con el siguiente selector
            }
        }
        return null;
    }

    private void verificarResultadosBusqueda() {
        System.out.println("🔍 Verificando resultados de búsqueda");

        String[] resultSelectors = {
                "#search",
                "#rso",
                "#main",
                ".g",
                ".MjjYud"
        };

        for (String selector : resultSelectors) {
            try {
                WebElement results = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))
                );
                if (results.isDisplayed()) {
                    System.out.println("✅ Resultados encontrados");
                    Allure.step("Resultados de búsqueda verificados");
                    return;
                }
            } catch (Exception e) {
                // Continuar con el siguiente selector
            }
        }

        // Verificación alternativa por URL
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("search") || currentUrl.contains("q=")) {
            System.out.println("✅ Resultados verificados por URL");
            Allure.step("Resultados verificados por URL de búsqueda");
        } else {
            Assert.fail("No se pudieron verificar los resultados de búsqueda");
        }
    }

    private WebElement buscarLogoSelenium() {
        String[] logoSelectors = {
                "img[alt*='Selenium']",
                "img[src*='selenium']",
                ".selenium-logo",
                "img[class*='logo']",
                "header img",
                ".navbar-brand img"
        };

        for (String selector : logoSelectors) {
            try {
                WebElement logo = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))
                );
                if (logo.isDisplayed()) {
                    System.out.println("✅ Logo/imagen encontrada");
                    return logo;
                }
            } catch (Exception e) {
                // Continuar con el siguiente selector
            }
        }

        // Verificación alternativa: cualquier imagen en el header
        try {
            WebElement headerImage = driver.findElement(By.cssSelector("header img, .header img"));
            if (headerImage.isDisplayed()) {
                System.out.println("✅ Imagen del header encontrada como alternativa");
                return headerImage;
            }
        } catch (Exception e) {
            // Ignorar
        }

        return null;
    }

    private String getBrowserInfo() {
        if (driver instanceof RemoteWebDriver) {
            RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
            return remoteDriver.getCapabilities().getBrowserName() +
                    " v" + remoteDriver.getCapabilities().getBrowserVersion();
        }
        return "Unknown browser";
    }
}