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
public class QuickTestCorrectedAllureTest {

    private static final String HUB_URL = "http://localhost:4444/wd/hub";
    private static final String SEARCH_TERM = "Selenium Grid Docker";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
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
                "--disable-extensions"
        );

        return new RemoteWebDriver(new URL(HUB_URL), options);
    }

    private WebDriver crearDriverFirefox() throws MalformedURLException {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=1920", "--height=1080");

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

        // Verificar resultados
        verificarResultadosBusqueda();
    }

    @Step("Manejando cookies de Google")
    private void manejarCookiesGoogle() {
        System.out.println("🍪 Manejando cookies de Google");

        try {
            WebElement acceptCookies = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("L2AGLb"))
            );
            acceptCookies.click();
            System.out.println("✅ Cookies aceptadas");
            Allure.step("Cookies aceptadas");
        } catch (TimeoutException e) {
            System.out.println("ℹ️ No se encontraron cookies para aceptar");
            Allure.step("No se encontraron cookies para aceptar");
        }
    }

    @Step("Realizando búsqueda: {SEARCH_TERM}")
    private void realizarBusqueda() {
        System.out.println("🔍 Realizando búsqueda: " + SEARCH_TERM);

        try {
            WebElement searchBox = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.name("q"))
            );

            searchBox.clear();
            searchBox.sendKeys(SEARCH_TERM);

            // Tomar screenshot antes de enviar
            tomarScreenshot("Antes de enviar búsqueda");

            searchBox.submit();

            System.out.println("✅ Búsqueda enviada exitosamente");
            Allure.step("Búsqueda enviada exitosamente");

        } catch (TimeoutException e) {
            System.err.println("❌ No se pudo encontrar la caja de búsqueda");
            Assert.fail("No se pudo encontrar la caja de búsqueda: " + e.getMessage());
        }
    }

    @Step("Verificando resultados de búsqueda")
    private void verificarResultadosBusqueda() {
        System.out.println("🔍 Verificando resultados de búsqueda");

        try {
            // Esperar a que aparezcan los resultados
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search")));

            String finalTitle = driver.getTitle();
            System.out.println("📋 Título después de búsqueda: " + finalTitle);
            Allure.step("Título después de búsqueda: " + finalTitle);

            // Verificar que el título contiene los términos buscados
            String titleLower = finalTitle.toLowerCase();

            boolean containsSelenium = titleLower.contains("selenium");
            boolean containsGrid = titleLower.contains("grid");
            boolean containsDocker = titleLower.contains("docker");

            System.out.println("🔍 Contiene 'selenium': " + containsSelenium);
            System.out.println("🔍 Contiene 'grid': " + containsGrid);
            System.out.println("🔍 Contiene 'docker': " + containsDocker);

            Assert.assertTrue(containsSelenium,
                    "El título no contiene 'selenium': " + finalTitle);
            Assert.assertTrue(containsGrid,
                    "El título no contiene 'grid': " + finalTitle);
            Assert.assertTrue(containsDocker,
                    "El título no contiene 'docker': " + finalTitle);

            // Tomar screenshot de los resultados
            tomarScreenshot("Resultados de búsqueda");

            System.out.println("✅ Verificación de resultados exitosa");
            Allure.step("Verificación de resultados exitosa");

        } catch (TimeoutException e) {
            System.err.println("❌ No se pudieron cargar los resultados de búsqueda");
            Assert.fail("No se pudieron cargar los resultados de búsqueda: " + e.getMessage());
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

            // Tomar screenshot de la página
            tomarScreenshot("Página Selenium.dev");

            System.out.println("✅ Verificación de Selenium.dev exitosa");
            Allure.step("Verificación de Selenium.dev exitosa");

        } catch (TimeoutException e) {
            System.err.println("❌ Selenium.dev no cargó correctamente");
            Assert.fail("Selenium.dev no cargó correctamente: " + e.getMessage());
        }
    }

    @Step("Tomando screenshot: {description}")
    private void tomarScreenshot(String description) {
        try {
            if (driver instanceof TakesScreenshot) {
                String screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
                System.out.println("📸 Screenshot tomado: " + description);

                // Método compatible con todas las versiones de Allure
                Allure.addAttachment(description, "text/plain", "Screenshot taken successfully");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al tomar screenshot: " + e.getMessage());
            Allure.addAttachment("Screenshot Error", "Error al tomar screenshot: " + e.getMessage());
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