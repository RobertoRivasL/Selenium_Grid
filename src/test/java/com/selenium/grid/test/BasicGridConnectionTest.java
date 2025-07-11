package com.selenium.grid.test;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class BasicGridConnectionTest {

    private WebDriver driver;
    private static final String HUB_URL = "http://localhost:4444/wd/hub";
    private static final String SIMPLE_PAGE_URL = "https://example.com";
    private static final String STATUS_PAGE_URL = "https://httpbin.org/status/200";

    @Parameters({"browser"})
    @BeforeMethod
    public void setUp(@Optional("chrome") String browser) throws MalformedURLException {
        driver = createWebDriver(browser);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();

        RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
        System.out.printf("✅ Conectado a Grid - Navegador: %s v%s%n",
                remoteDriver.getCapabilities().getBrowserName(),
                remoteDriver.getCapabilities().getBrowserVersion());
    }

    private WebDriver createWebDriver(String browser) throws MalformedURLException {
        switch (browser.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--disable-dev-shm-usage", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
                return new RemoteWebDriver(new URL(HUB_URL), chromeOptions);
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("--width=1920", "--height=1080");
                return new RemoteWebDriver(new URL(HUB_URL), firefoxOptions);
            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--disable-dev-shm-usage", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
                return new RemoteWebDriver(new URL(HUB_URL), edgeOptions);
            default:
                throw new IllegalArgumentException("Navegador no soportado: " + browser + ". Soportados: chrome, firefox, edge");
        }
    }

    @Test(priority = 1)
    public void testBasicPageLoad() {
        System.out.println("📄 Probando carga básica de página...");
        driver.get(SIMPLE_PAGE_URL);

        String title = driver.getTitle();
        System.out.println("📋 Título de la página: " + title);

        Assert.assertNotNull(title, "El título de la página no debe ser null");
        Assert.assertFalse(title.isEmpty(), "El título de la página no debe estar vacío");

        System.out.println("✅ Prueba de carga básica pasada");
    }

    @Test(priority = 2)
    public void testSimpleNavigation() {
        System.out.println("🧭 Probando navegación simple...");
        driver.get(STATUS_PAGE_URL);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("🔗 URL actual: " + currentUrl);

        Assert.assertTrue(currentUrl.contains("httpbin.org"), "La URL debe contener httpbin.org");

        System.out.println("✅ Prueba de navegación simple pasada");
    }

    @Test(priority = 3)
    public void testBrowserCapabilities() {
        System.out.println("🔍 Probando capacidades del navegador...");

        RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
        String browserName = remoteDriver.getCapabilities().getBrowserName();
        String browserVersion = remoteDriver.getCapabilities().getBrowserVersion();
        String platformName = remoteDriver.getCapabilities().getPlatformName().toString();

        System.out.println("🌐 Navegador: " + browserName);
        System.out.println("📊 Versión: " + browserVersion);
        System.out.println("💻 Plataforma: " + platformName);

        Assert.assertNotNull(browserName, "El nombre del navegador no debe ser null");
        Assert.assertNotNull(browserVersion, "La versión del navegador no debe ser null");

        System.out.println("✅ Prueba de capacidades pasada");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            System.out.println("🔚 Cerrando sesión del navegador...");
            driver.quit();
        }
    }
}