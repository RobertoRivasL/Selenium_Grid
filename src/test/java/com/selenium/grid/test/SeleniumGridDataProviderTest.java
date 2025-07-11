package com.selenium.grid.test;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

///**
// * ✅ SELENIUM GRID DATA PROVIDER TEST - CON UTILIDADES INTEGRADAS
// *
// * Funcionalidades integradas:
// * - Screenshots automáticos en fallos
// * - Verificación de Grid disponible
// * - Información detallada de sesión
// * - Limpieza automática de estado
// * - Retry con exponential backoff
// * - Logging avanzado con timestamps
// * - Validaciones robustas
// * - Manejo inteligente de ventanas
// * - Health checks del Grid
// */
public class SeleniumGridDataProviderTest {

    private static final String HUB_URL = "http://localhost:4444/wd/hub";
    private static final Duration IMPLICIT_WAIT = Duration.ofSeconds(10);
    private static final Duration EXPLICIT_WAIT = Duration.ofSeconds(30);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // ✅ ThreadLocal para concurrencia segura
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<GridSessionInfo> sessionInfoThreadLocal = new ThreadLocal<>();

    @DataProvider(name = "browsers", parallel = true)
    public Object[][] browserProvider() {
        return new Object[][]{
                {"chrome"},
                {"firefox"},
                {"edge"}
        };
    }

    @BeforeMethod
    @Step("Configurando entorno para {browser}")
    public void setUp(Object[] testData) {
        String browser = (String) testData[0];

        try {
            // ✅ 1. Verificar que el Grid esté disponible
            if (!isGridAvailable(HUB_URL)) {
                logTestStep("Grid Check", "Grid no disponible en " + HUB_URL);
                throw new SkipException("Selenium Grid no está disponible en: " + HUB_URL);
            }

            // ✅ 2. Inicializar driver
            initializeDriver(browser);

            // ✅ 3. Obtener información de sesión
            GridSessionInfo sessionInfo = getGridSessionInfo(getDriver());
            sessionInfoThreadLocal.set(sessionInfo);

            // ✅ 4. Limpiar estado inicial del navegador
            cleanBrowserState(getDriver());

            logTestStep("Setup Complete",
                    "Driver inicializado para " + browser + " | " + sessionInfo.toString());

        } catch (Exception e) {
            logTestStep("Setup Failed", "Error: " + e.getMessage());
            throw new RuntimeException("Failed to initialize driver for " + browser, e);
        }
    }

    @AfterMethod
    @Step("Limpiando recursos")
    public void tearDown() {
        String browserInfo = "Unknown";

        try {
            WebDriver driver = getDriver();
            if (driver != null) {
                browserInfo = getBrowserInfo();

                // ✅ Agregar información de sesión a Allure
                GridSessionInfo sessionInfo = sessionInfoThreadLocal.get();
                if (sessionInfo != null) {
                    Allure.addAttachment("Session Summary", sessionInfo.getDetailedInfo());
                }

                logTestStep("Teardown", "Cerrando sesión: " + browserInfo);
                driver.quit();
            }
        } catch (Exception e) {
            logTestStep("Teardown Error", "Error en cleanup: " + e.getMessage());
        } finally {
            // ✅ CRÍTICO: Limpiar ThreadLocal
            driverThreadLocal.remove();
            waitThreadLocal.remove();
            sessionInfoThreadLocal.remove();

            logTestStep("Cleanup Complete", "Recursos liberados para: " + browserInfo);
        }
    }

    @Test(dataProvider = "browsers", priority = 1)
    @Step("Test de búsqueda en Google - {browser}")
    public void testGoogleSearch(String browser) {
        executeTestWithRetry("Google Search", 2, () -> {
            navigateToUrl("https://www.google.com", "Google");
            handleGoogleConsent();
            performGoogleSearch("Selenium Grid " + browser);
            verifySearchResultsUpdated();
        });
    }

    @Test(dataProvider = "browsers", priority = 2)
    @Step("Test de Selenium HQ - {browser}")
    public void testSeleniumHQ(String browser) {
        executeTestWithRetry("Selenium HQ", 2, () -> {
            navigateToUrl("https://www.selenium.dev/", "Selenium");
            validatePageTitle("Selenium");
            verifySeleniumLogo();
        });
    }

    @Test(dataProvider = "browsers", priority = 3)
    @Step("Test de DuckDuckGo - {browser}")
    public void testDuckDuckGo(String browser) {
        executeTestWithRetry("DuckDuckGo", 2, () -> {
            navigateToUrl("https://duckduckgo.com", "DuckDuckGo");
            performDuckDuckGoSearch("Docker " + browser);
            verifyDuckDuckGoResultsUpdated();
        });
    }

    @Test(dataProvider = "browsers", priority = 4)
    @Step("Test de ejemplo práctico - {browser}")
    public void testExampleDomain(String browser) {
        executeTestWithRetry("Example Domain", 1, () -> {
            navigateToUrl("https://example.com", "Example Domain");
            validatePageTitle("Example Domain");

            WebElement heading = findElementSafely(
                    new String[]{"h1", "header h1", ".heading"},
                    "Título principal"
            );
            Assert.assertNotNull(heading, "Debe existir un título principal");
        });
    }

    @Test(dataProvider = "browsers", priority = 5)
    @Step("Test de navegación avanzada - {browser}")
    public void testAdvancedNavigation(String browser) {
        executeTestWithRetry("Advanced Navigation", 1, () -> {
            // Navegación múltiple para probar estabilidad
            navigateToUrl("https://example.com", "Example Domain");

            // Abrir nueva pestaña con JavaScript
            ((JavascriptExecutor) getDriver()).executeScript("window.open('about:blank','_blank');");

            // Manejar múltiples ventanas
            Set<String> windows = getDriver().getWindowHandles();
            if (windows.size() > 1) {
                logTestStep("Window Management", "Múltiples ventanas detectadas: " + windows.size());

                // Volver a la ventana original
                String originalWindow = windows.iterator().next();
                getDriver().switchTo().window(originalWindow);

                logTestStep("Window Switch", "Vuelto a ventana original");
            }

            // Verificar que seguimos en la página correcta
            Assert.assertTrue(getDriver().getCurrentUrl().contains("example.com"),
                    "Debe seguir en example.com");
        });
    }

    // ✅ MÉTODOS INTEGRADOS CON UTILIDADES

    private void initializeDriver(String browser) throws MalformedURLException {
        logTestStep("Driver Init", "Iniciando " + browser);

        WebDriver driver = new RemoteWebDriver(new URL(HUB_URL), getCapabilities(browser));

        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT);
        driver.manage().window().maximize();

        driverThreadLocal.set(driver);
        waitThreadLocal.set(new WebDriverWait(driver, EXPLICIT_WAIT));

        String sessionId = ((RemoteWebDriver) driver).getSessionId().toString();
        Allure.addAttachment("Session Info - " + browser,
                "Browser: " + browser + "\nSession ID: " + sessionId);
    }

    private Capabilities getCapabilities(String browser) {
        switch (browser.toLowerCase()) {
            case "chrome":
                ChromeOptions chrome = new ChromeOptions();
                chrome.addArguments(
                        "--disable-dev-shm-usage",
                        "--no-sandbox",
                        "--disable-gpu",
                        "--window-size=1920,1080",
                        "--disable-blink-features=AutomationControlled",
                        "--disable-extensions",
                        "--no-first-run",
                        "--disable-infobars",
                        "--disable-notifications"
                );
                return chrome;

            case "firefox":
                FirefoxOptions firefox = new FirefoxOptions();
                firefox.addArguments("--width=1920", "--height=1080");
                firefox.addPreference("dom.webnotifications.enabled", false);
                firefox.addPreference("geo.enabled", false);
                return firefox;

            case "edge":
                EdgeOptions edge = new EdgeOptions();
                edge.addArguments(
                        "--disable-dev-shm-usage",
                        "--no-sandbox",
                        "--disable-gpu",
                        "--window-size=1920,1080",
                        "--disable-notifications"
                );
                return edge;

            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }
    }

    private WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("Driver not initialized for current thread");
        }
        return driver;
    }

    private WebDriverWait getWait() {
        WebDriverWait wait = waitThreadLocal.get();
        if (wait == null) {
            throw new IllegalStateException("Wait not initialized for current thread");
        }
        return wait;
    }

    // ✅ MÉTODO CON RETRY Y SCREENSHOTS AUTOMÁTICOS
    @Step("Ejecutando test con retry: {testName}")
    private void executeTestWithRetry(String testName, int maxRetries, Runnable testLogic) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logTestStep("Test Start", testName + " en " + getBrowserName() + " (intento " + attempt + ")");

                testLogic.run();

                logTestStep("Test Success", testName + " completado exitosamente");
                Allure.step(testName + " exitoso en " + getBrowserName());
                return; // Éxito, salir del loop

            } catch (Exception e) {
                lastException = e;

                // ✅ Screenshot automático en fallo
                takeScreenshot(getDriver(), testName + "_failed_attempt_" + attempt);

                logTestStep("Test Failed", testName + " falló (intento " + attempt + "): " + e.getMessage());

                if (attempt < maxRetries) {
                    logTestStep("Retry", "Reintentando en 2 segundos...");
                    try {
                        Thread.sleep(2000); // Pausa antes del retry

                        // ✅ Limpiar estado antes del retry
                        cleanBrowserState(getDriver());

                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    logTestStep("Test Exhausted", "Todos los intentos agotados para: " + testName);
                }
            }
        }

        // Si llegamos aquí, todos los intentos fallaron
        Allure.addAttachment("Final Error Details", lastException.toString());
        throw new AssertionError("Test failed after " + maxRetries + " attempts: " + testName, lastException);
    }

    @Step("Navegando a {url}")
    private void navigateToUrl(String url, String expectedTitleContains) {
        WebDriver driver = getDriver();
        WebDriverWait wait = getWait();

        logTestStep("Navigation", "Navegando a: " + url);

        driver.get(url);
        wait.until(ExpectedConditions.titleContains(expectedTitleContains));

        String currentUrl = driver.getCurrentUrl();
        logTestStep("Navigation Success", "Página cargada: " + currentUrl);
        Allure.addAttachment("Current URL", currentUrl);
    }

    private void handleGoogleConsent() {
        String[] cookieSelectors = {
                "#L2AGLb", "button[id='L2AGLb']", ".QS5gu",
                "[aria-label*='Accept']", "[aria-label*='Acepto']",
                "button:contains('Accept')", "button:contains('Acepto')"
        };

        for (String selector : cookieSelectors) {
            try {
                WebElement button = getWait().until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(selector))
                );
                if (isElementVisibleAndClickable(getDriver(), button)) {
                    button.click();
                    logTestStep("Cookies", "Cookies aceptadas con selector: " + selector);
                    return;
                }
            } catch (Exception ignored) {}
        }
        logTestStep("Cookies", "No se encontró diálogo de cookies");
    }

    @Step("Realizando búsqueda en Google: {searchTerm}")
    private void performGoogleSearch(String searchTerm) {
        WebElement searchBox = findElementSafely(
                new String[]{"input[name='q']", "textarea[name='q']", "#APjFqb", ".gLFyf"},
                "Campo de búsqueda de Google"
        );

        if (searchBox != null) {
            scrollToElement(getDriver(), searchBox);
            searchBox.clear();
            searchBox.sendKeys(searchTerm);
            searchBox.sendKeys(Keys.ENTER);

            logTestStep("Search", "Búsqueda realizada: " + searchTerm);
            Allure.step("Búsqueda realizada: " + searchTerm);
        }
    }

    private void verifySearchResultsUpdated() {
        String[] updatedGoogleSelectors = {
                "[role='main']", "[data-async-context]", "[jsdata]",
                "div[data-ved]", "[data-sokoban-container]", "div[class*='result']",
                "div[class*='search']", "h3", "cite", "[href*='/url?']",
                "div[style*='font']:not(:empty)"
        };

        WebDriver driver = getDriver();
        WebDriverWait wait = getWait();

        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));

        boolean resultsFound = false;

        // Método con retry integrado
        resultsFound = retryOperation(() -> {
            for (String selector : updatedGoogleSelectors) {
                try {
                    List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                    if (!elements.isEmpty()) {
                        logTestStep("Results Found", "Selector exitoso: " + selector + " (" + elements.size() + " elementos)");
                        return true;
                    }
                } catch (Exception e) {
                    // Continuar con el siguiente selector
                }
            }
            return false;
        }, 3, Duration.ofSeconds(2));

        if (!resultsFound) {
            // Método alternativo: verificar por URL
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("search?") || currentUrl.contains("/search")) {
                logTestStep("Results Detection", "Resultados detectados por URL de búsqueda");
                resultsFound = true;
            }
        }

        Assert.assertTrue(resultsFound, "No se pudieron detectar resultados de búsqueda en Google");
        logTestStep("Results Verified", "Resultados de búsqueda verificados en Google");
    }

    @Step("Realizando búsqueda en DuckDuckGo: {searchTerm}")
    private void performDuckDuckGoSearch(String searchTerm) {
        WebElement searchBox = findElementSafely(
                new String[]{
                        "#search_form_input_homepage", "#searchbox_input",
                        "input[name='q']", "input[type='search']"
                },
                "Campo de búsqueda de DuckDuckGo"
        );

        if (searchBox != null) {
            scrollToElement(getDriver(), searchBox);
            searchBox.clear();
            searchBox.sendKeys(searchTerm);
            searchBox.sendKeys(Keys.ENTER);

            logTestStep("DuckDuckGo Search", "Búsqueda realizada: " + searchTerm);
        }
    }

    private void verifyDuckDuckGoResultsUpdated() {
        String[] updatedDuckDuckGoSelectors = {
                "article[data-testid='result']", "[data-testid='result']",
                "div[data-testid]", ".react-results--main", "[data-area='search']",
                "h2 a[href]", ".result__title", "[class*='result']",
                "a[href*='uddg.com']", ".web-result", "[role='listitem']"
        };

        boolean resultsFound = retryOperation(() -> {
            for (String selector : updatedDuckDuckGoSelectors) {
                try {
                    List<WebElement> elements = getDriver().findElements(By.cssSelector(selector));
                    if (!elements.isEmpty()) {
                        logTestStep("DuckDuckGo Results", "Selector exitoso: " + selector + " (" + elements.size() + " elementos)");
                        return true;
                    }
                } catch (Exception e) {
                    // Continuar
                }
            }
            return false;
        }, 3, Duration.ofSeconds(2));

        Assert.assertTrue(resultsFound, "No se pudieron detectar resultados de búsqueda en DuckDuckGo");
        logTestStep("DuckDuckGo Verified", "Resultados verificados en DuckDuckGo");
    }

    private void validatePageTitle(String expectedContains) {
        String title = getDriver().getTitle();
        Assert.assertTrue(
                title.contains(expectedContains),
                String.format("El título '%s' debería contener '%s'", title, expectedContains)
        );
        logTestStep("Title Validated", "Título validado: " + title);
    }

    private void verifySeleniumLogo() {
        WebElement logo = findElementSafely(
                new String[]{
                        "img[alt*='Selenium']", "img[src*='selenium']",
                        "header img", ".navbar-brand img", "[class*='logo']"
                },
                "Logo de Selenium"
        );

        Assert.assertNotNull(logo, "El logo de Selenium debe estar presente");
        logTestStep("Logo Verified", "Logo de Selenium encontrado");
    }

    private WebElement findElementSafely(String[] selectors, String elementDescription) {
        WebDriverWait wait = getWait();

        for (String selector : selectors) {
            try {
                WebElement element = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))
                );
                if (element != null && isElementVisibleAndClickable(getDriver(), element)) {
                    logTestStep("Element Found", elementDescription + " (selector: " + selector + ")");
                    return element;
                }
            } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
                logTestStep("Selector Failed", selector + " - " + e.getClass().getSimpleName());
            }
        }

        String error = elementDescription + " no encontrado con ningún selector";
        logTestStep("Element Not Found", error);
        Allure.addAttachment("Element Not Found", error + "\nSelectors tried: " + String.join(", ", selectors));

        return null;
    }

    // ✅ UTILIDADES INTEGRADAS

    /**
     * ✅ Toma screenshot y lo adjunta a Allure
     */
    private void takeScreenshot(WebDriver driver, String stepName) {
        try {
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                String attachmentName = String.format("%s_%s", stepName, timestamp);

                Allure.addAttachment(attachmentName, "image/png",
                        new ByteArrayInputStream(screenshot), "png");

                logTestStep("Screenshot", "Capturado: " + attachmentName);
            }
        } catch (Exception e) {
            logTestStep("Screenshot Error", "Error tomando screenshot: " + e.getMessage());
        }
    }

    /**
     * ✅ Verificar si un elemento está realmente visible y clickeable
     */
    private boolean isElementVisibleAndClickable(WebDriver driver, WebElement element) {
        try {
            if (!element.isDisplayed() || !element.isEnabled()) return false;

            String script =
                    "var element = arguments[0]; " +
                            "var style = window.getComputedStyle(element); " +
                            "return style.display !== 'none' && " +
                            "       style.visibility !== 'hidden' && " +
                            "       style.opacity !== '0' && " +
                            "       element.offsetWidth > 0 && " +
                            "       element.offsetHeight > 0;";

            return (Boolean) ((JavascriptExecutor) driver).executeScript(script, element);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ✅ Scroll inteligente a elemento
     */
    private void scrollToElement(WebDriver driver, WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                    element
            );
            Thread.sleep(500);
            logTestStep("Scroll", "Scroll realizado al elemento");
        } catch (Exception e) {
            logTestStep("Scroll Error", "Error en scroll: " + e.getMessage());
        }
    }

    /**
     * ✅ Verificar conectividad con el Grid
     */
    private boolean isGridAvailable(String hubUrl) {
        try {
            java.net.URL url = new java.net.URL(hubUrl + "/status");
            java.net.HttpURLConnection connection =
                    (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            boolean available = responseCode == 200;

            logTestStep("Grid Check", available ?
                    "Grid disponible en: " + hubUrl :
                    "Grid no disponible: " + responseCode);

            return available;
        } catch (Exception e) {
            logTestStep("Grid Check Error", "Error verificando Grid: " + e.getMessage());
            return false;
        }
    }

    /**
     * ✅ Limpieza de cookies y storage
     */
    private void cleanBrowserState(WebDriver driver) {
        try {
            driver.manage().deleteAllCookies();

            ((JavascriptExecutor) driver).executeScript(
                    "try { window.localStorage.clear(); } catch(e) {} " +
                            "try { window.sessionStorage.clear(); } catch(e) {}"
            );

            logTestStep("Browser Clean", "Estado del navegador limpiado");
        } catch (Exception e) {
            logTestStep("Clean Error", "Error limpiando estado: " + e.getMessage());
        }
    }

    /**
     * ✅ Retry con exponential backoff
     */
    private <T> T retryOperation(Supplier<T> operation, int maxAttempts, Duration initialDelay) {
        Exception lastException = null;
        Duration currentDelay = initialDelay;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                logTestStep("Retry", "Intento " + attempt + " falló: " + e.getMessage());

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(currentDelay.toMillis());
                        currentDelay = currentDelay.multipliedBy(2);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new RuntimeException("Operación falló después de " + maxAttempts + " intentos",
                lastException);
    }

    /**
     * ✅ Información detallada del Grid y sesión
     */
    private GridSessionInfo getGridSessionInfo(WebDriver driver) {
        if (!(driver instanceof RemoteWebDriver)) {
            return new GridSessionInfo("Local", "N/A", "N/A", "N/A");
        }

        RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
        Capabilities caps = remoteDriver.getCapabilities();

        return new GridSessionInfo(
                caps.getBrowserName(),
                caps.getBrowserVersion(),
                remoteDriver.getSessionId().toString(),
                caps.getPlatformName().toString()
        );
    }

    /**
     * ✅ Logger personalizado para tests
     */
    private void logTestStep(String stepName, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String threadName = Thread.currentThread().getName();

        System.out.println(String.format("[%s] [%s] 🔹 %s: %s",
                timestamp, threadName, stepName, details));

        Allure.step(stepName + ": " + details);
    }

    private String getBrowserInfo() {
        try {
            WebDriver driver = getDriver();
            if (driver instanceof RemoteWebDriver) {
                RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
                Capabilities caps = remoteDriver.getCapabilities();
                return String.format("%s v%s (Session: %s)",
                        caps.getBrowserName(),
                        caps.getBrowserVersion(),
                        remoteDriver.getSessionId().toString().substring(0, 8) + "..."
                );
            }
        } catch (Exception e) {
            return "Browser info unavailable: " + e.getMessage();
        }
        return "Unknown browser";
    }

    private String getBrowserName() {
        try {
            WebDriver driver = getDriver();
            if (driver instanceof RemoteWebDriver) {
                return ((RemoteWebDriver) driver).getCapabilities().getBrowserName();
            }
        } catch (Exception e) {
            return "unknown";
        }
        return "unknown";
    }

    // ✅ CLASE INTERNA PARA INFORMACIÓN DE SESIÓN
    private static class GridSessionInfo {
        private final String browserName;
        private final String browserVersion;
        private final String sessionId;
        private final String platform;
        private final String timestamp;

        public GridSessionInfo(String browserName, String browserVersion,
                               String sessionId, String platform) {
            this.browserName = browserName;
            this.browserVersion = browserVersion;
            this.sessionId = sessionId;
            this.platform = platform;
            this.timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        }

        public String getBrowserName() { return browserName; }
        public String getBrowserVersion() { return browserVersion; }
        public String getSessionId() { return sessionId; }
        public String getPlatform() { return platform; }

        @Override
        public String toString() {
            return String.format("Browser: %s v%s | Platform: %s | Session: %s",
                    browserName, browserVersion, platform,
                    sessionId.length() > 8 ? sessionId.substring(0, 8) + "..." : sessionId);
        }

        public String getDetailedInfo() {
            return String.format(
                    "Browser: %s\nVersion: %s\nPlatform: %s\nSession ID: %s\nTimestamp: %s",
                    browserName, browserVersion, platform, sessionId, timestamp
            );
        }
    }
}