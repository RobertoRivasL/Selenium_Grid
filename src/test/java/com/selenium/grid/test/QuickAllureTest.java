package com.selenium.grid.test;

import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

@Epic("Selenium Grid Testing")
@Feature("Cross-Browser Smoke Tests")
public class QuickAllureTest {

    private static final String HUB_URL = "http://localhost:4444/wd/hub";
    private static final String SEARCH_TERM = "Selenium Grid Docker";
    private static final Duration TIMEOUT = Duration.ofSeconds(20);
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

        WebDriver webDriver;

        try {
            switch (browserName.toLowerCase()) {
                case "chrome":
                    webDriver = crearDriverChrome();
                    break;
                case "firefox":
                    webDriver = crearDriverFirefox();
                    break;
                default:
                    throw new IllegalArgumentException("Navegador no soportado: " + browserName);
            }

            System.out.println("✅ Driver inicializado correctamente");
            Allure.addAttachment("Driver Info",
                    "Browser: " + browserName + "\nDriver initialized successfully");
            return webDriver;

        } catch (Exception e) {
            System.err.println("❌ Error inicializando driver: " + e.getMessage());
            Allure.addAttachment("Driver Error", e.getMessage());
            throw e;
        }
    }

    private WebDriver crearDriverChrome() throws MalformedURLException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--disable-dev-shm-usage",
                "--no-sandbox",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--disable-extensions",
                "--disable-blink-features=AutomationControlled",
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        );

        return new RemoteWebDriver(new URL(HUB_URL), options);
    }

    private WebDriver crearDriverFirefox() throws MalformedURLException {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=1920", "--height=1080");
        options.addPreference("general.useragent.override",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

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
                "Google no cargó correctamente. Título actual: " + initialTitle);

        // Manejar cookies si aparecen
        manejarCookiesGoogle();

        // Realizar búsqueda
        realizarBusqueda();

        // Verificar resultados con selectores actualizados
        verificarResultadosBusquedaActualizados();
    }

    @Step("Manejando cookies de Google")
    private void manejarCookiesGoogle() {
        System.out.println("🍪 Manejando cookies de Google");

        try {
            // Múltiples selectores para el botón de aceptar cookies
            WebElement acceptCookies = null;

            // Intentar diferentes selectores
            String[] cookieSelectors = {
                    "#L2AGLb",
                    "button[id='L2AGLb']",
                    "button:contains('Accept all')",
                    "button:contains('Aceptar todo')",
                    "button:contains('I agree')"
            };

            for (String selector : cookieSelectors) {
                try {
                    if (selector.startsWith("#")) {
                        acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(By.id(selector.substring(1))));
                    } else {
                        acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
                    }
                    break;
                } catch (TimeoutException e) {
                    continue;
                }
            }

            if (acceptCookies != null) {
                acceptCookies.click();
                System.out.println("✅ Cookies aceptadas");
                Allure.step("Cookies aceptadas");
            } else {
                System.out.println("ℹ️ No se encontraron cookies para aceptar");
                Allure.step("No se encontraron cookies para aceptar");
            }
        } catch (Exception e) {
            System.out.println("ℹ️ No se pudieron manejar cookies: " + e.getMessage());
            Allure.step("No se pudieron manejar cookies");
        }
    }

    @Step("Realizando búsqueda: {SEARCH_TERM}")
    private void realizarBusqueda() {
        System.out.println("🔍 Realizando búsqueda: " + SEARCH_TERM);

        try {
            // Múltiples selectores para la caja de búsqueda
            WebElement searchBox = null;

            String[] searchSelectors = {
                    "input[name='q']",
                    "input[title='Search']",
                    "input[title='Buscar']",
                    "textarea[name='q']",
                    "#APjFqb"
            };

            for (String selector : searchSelectors) {
                try {
                    searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
                    break;
                } catch (TimeoutException e) {
                    continue;
                }
            }

            if (searchBox == null) {
                throw new RuntimeException("No se pudo encontrar la caja de búsqueda");
            }

            searchBox.clear();
            searchBox.sendKeys(SEARCH_TERM);

            System.out.println("📝 Texto ingresado en búsqueda");

            // Enviar búsqueda con Enter
            searchBox.sendKeys(Keys.ENTER);

            System.out.println("✅ Búsqueda enviada exitosamente");
            Allure.step("Búsqueda enviada exitosamente");

        } catch (Exception e) {
            System.err.println("❌ Error en búsqueda: " + e.getMessage());
            Allure.addAttachment("Search Error", e.getMessage());
            Assert.fail("No se pudo realizar la búsqueda: " + e.getMessage());
        }
    }

    @Step("Verificando resultados de búsqueda con selectores actualizados")
    private void verificarResultadosBusquedaActualizados() {
        System.out.println("🔍 Verificando resultados de búsqueda con selectores actualizados");

        try {
            // Esperar que la página cargue
            Thread.sleep(3000);

            // Intentar múltiples selectores para los resultados
            WebElement results = null;
            String[] resultSelectors = {
                    "#search",
                    "#rso",
                    ".g",
                    "#main",
                    ".MjjYud",
                    "#rcnt",
                    "#center_col"
            };

            for (String selector : resultSelectors) {
                try {
                    results = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
                    System.out.println("✅ Resultados encontrados con selector: " + selector);
                    break;
                } catch (TimeoutException e) {
                    System.out.println("❌ No se encontraron resultados con selector: " + selector);
                    continue;
                }
            }

            // Si no encontramos resultados con selectores, verificar por título
            if (results == null) {
                System.out.println("⚠️ No se encontraron resultados con selectores, verificando por título...");

                String finalTitle = driver.getTitle();
                System.out.println("📋 Título después de búsqueda: " + finalTitle);
                Allure.step("Título después de búsqueda: " + finalTitle);
                Allure.addAttachment("Current Page Title", finalTitle);

                // Verificar que el título contiene los términos buscados
                String titleLower = finalTitle.toLowerCase();
                boolean containsSelenium = titleLower.contains("selenium");
                boolean containsGrid = titleLower.contains("grid");
                boolean containsDocker = titleLower.contains("docker");

                System.out.println("🔍 Contiene 'selenium': " + containsSelenium);
                System.out.println("🔍 Contiene 'grid': " + containsGrid);
                System.out.println("🔍 Contiene 'docker': " + containsDocker);

                if (containsSelenium || containsGrid || containsDocker) {
                    System.out.println("✅ Verificación exitosa por título");
                    Allure.step("Verificación exitosa por título");
                } else {
                    System.out.println("❌ No se encontraron términos de búsqueda en el título");
                    Allure.addAttachment("Page Source", driver.getPageSource());
                    Assert.fail("No se encontraron términos de búsqueda en el título: " + finalTitle);
                }
            } else {
                // Si encontramos resultados con selectores, también verificar título
                System.out.println("✅ Resultados encontrados con selectores DOM");

                String finalTitle = driver.getTitle();
                System.out.println("📋 Título después de búsqueda: " + finalTitle);
                Allure.step("Título después de búsqueda: " + finalTitle);

                String titleLower = finalTitle.toLowerCase();
                boolean containsSelenium = titleLower.contains("selenium");
                boolean containsGrid = titleLower.contains("grid");
                boolean containsDocker = titleLower.contains("docker");

                System.out.println("🔍 Contiene 'selenium': " + containsSelenium);
                System.out.println("🔍 Contiene 'grid': " + containsGrid);
                System.out.println("🔍 Contiene 'docker': " + containsDocker);

                if (containsSelenium || containsGrid || containsDocker) {
                    System.out.println("✅ Verificación completa exitosa");
                    Allure.step("Verificación completa exitosa");
                } else {
                    Assert.fail("No se encontraron términos de búsqueda en el título: " + finalTitle);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail("Test interrumpido: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error verificando resultados: " + e.getMessage());
            Allure.addAttachment("Verification Error", e.getMessage());
            Allure.addAttachment("Current URL", driver.getCurrentUrl());
            Assert.fail("Error verificando resultados: " + e.getMessage());
        }
    }

    @Step("Ejecutando prueba en Selenium.dev")
    private void ejecutarPruebaSeleniumDev() {
        System.out.println("🌐 Ejecutando prueba en Selenium.dev");

        // Navegar a Selenium.dev
        Allure.step("Navegando a Selenium.dev", () -> {
            System.out.println("🌐 Navegando a Selenium.dev...");
            driver.get("https://www.selenium.dev");
            String url = driver.getCurrentUrl();
            System.out.println("📍 URL actual: " + url);
            Allure.addAttachment("Selenium.dev URL", url);
        });

        // Verificar carga
        try {
            wait.until(ExpectedConditions.titleContains("Selenium"));

            String seleniumTitle = driver.getTitle();
            System.out.println("📋 Título Selenium.dev: " + seleniumTitle);
            Allure.step("Título Selenium.dev: " + seleniumTitle);

            Assert.assertTrue(seleniumTitle.toLowerCase().contains("selenium"),
                    "Selenium.dev no cargó correctamente. Título: " + seleniumTitle);

            System.out.println("✅ Verificación de Selenium.dev exitosa");
            Allure.step("Verificación de Selenium.dev exitosa");

        } catch (TimeoutException e) {
            System.err.println("❌ Selenium.dev no cargó correctamente");
            Assert.fail("Selenium.dev no cargó correctamente: " + e.getMessage());
        }
    }

    private String getBrowserInfo() {
        try {
            if (driver instanceof RemoteWebDriver) {
                RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
                return "Browser: " + remoteDriver.getCapabilities().getBrowserName() +
                        "\nVersion: " + remoteDriver.getCapabilities().getBrowserVersion() +
                        "\nPlatform: " + remoteDriver.getCapabilities().getPlatformName();
            }
        } catch (Exception e) {
            return "Error obteniendo información del navegador: " + e.getMessage();
        }
        return "Información del navegador no disponible";
    }
}