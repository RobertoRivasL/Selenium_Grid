package com.selenium.grid.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Selenium Grid Test - Versión Simplificada y Robusta
 * Elimina todos los selectores problemáticos y usa solo CSS válido
 */
public class SeleniumGridTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String hubUrl = "http://localhost:4444/wd/hub";
    private final Duration TIMEOUT = Duration.ofSeconds(20);
    private final Duration IMPLICIT_WAIT = Duration.ofSeconds(10);

    @Parameters({"browser"})
    @BeforeMethod
    public void setUp(@Optional("chrome") String browser) throws MalformedURLException {
        System.out.println("🔧 Setting up test for browser: " + browser);

        switch (browser.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments(
                        "--disable-dev-shm-usage",
                        "--no-sandbox",
                        "--disable-gpu",
                        "--window-size=1920,1080",
                        "--disable-extensions"
                );
                driver = new RemoteWebDriver(new URL(hubUrl), chromeOptions);
                break;

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("--width=1920", "--height=1080");
                driver = new RemoteWebDriver(new URL(hubUrl), firefoxOptions);
                break;

            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }

        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, TIMEOUT);

        String browserInfo = ((RemoteWebDriver) driver).getCapabilities().getBrowserName() +
                " v" + ((RemoteWebDriver) driver).getCapabilities().getBrowserVersion();
        System.out.println("✅ Test running on: " + browserInfo);
    }

    @Test(priority = 1)
    public void testGoogleSearch() {
        System.out.println("🌐 Executing Google Search Test...");

        driver.get("https://www.google.com");

        // Verificar que Google cargó
        wait.until(ExpectedConditions.titleContains("Google"));
        System.out.println("✅ Google loaded successfully");

        // Manejar cookies de forma simple
        handleGoogleCookiesSimple();

        // Realizar búsqueda
        performGoogleSearchSimple("Selenium Grid Docker");

        // Verificar resultados de forma simple
        verifyGoogleSearchResultsSimple();

        System.out.println("✅ Google search test passed");
    }

    @Test(priority = 2)
    public void testSeleniumHQWebsite() {
        System.out.println("🌐 Executing Selenium HQ Website Test...");

        driver.get("https://www.selenium.dev/");

        // Verificar que la página cargó
        wait.until(ExpectedConditions.titleContains("Selenium"));
        String title = driver.getTitle();
        Assert.assertTrue(title.toLowerCase().contains("selenium"),
                "Page title should contain 'Selenium', but was: " + title);

        System.out.println("📋 Page title: " + title);

        // Verificar contenido de forma simple
        String pageSource = driver.getPageSource().toLowerCase();
        boolean hasSeleniumContent = pageSource.contains("selenium") &&
                (pageSource.contains("webdriver") ||
                        pageSource.contains("automate") ||
                        pageSource.contains("testing"));

        Assert.assertTrue(hasSeleniumContent, "Selenium content should be present on the page");
        System.out.println("✅ Selenium content verified by page source");

        System.out.println("✅ Selenium HQ website test passed");
    }

    @Test(priority = 3)
    public void testDuckDuckGoSearch() {
        System.out.println("🌐 Executing DuckDuckGo Search Test...");

        driver.get("https://duckduckgo.com");

        // Verificar que DuckDuckGo cargó
        wait.until(ExpectedConditions.titleContains("DuckDuckGo"));
        System.out.println("✅ DuckDuckGo loaded successfully");

        // Buscar campo de búsqueda de forma simple
        WebElement searchBox = findDuckDuckGoSearchBoxSimple();
        Assert.assertNotNull(searchBox, "Search box should be found");

        // Realizar búsqueda
        searchBox.clear();
        searchBox.sendKeys("Docker containers");
        searchBox.submit();

        // Verificar resultados de forma simple
        verifyDuckDuckGoResultsSimple();

        System.out.println("✅ DuckDuckGo search test passed");
    }

    // === MÉTODOS AUXILIARES SIMPLIFICADOS ===

    private void handleGoogleCookiesSimple() {
        System.out.println("🍪 Handling Google cookies...");

        try {
            // Intentar el selector más común primero
            WebElement cookieButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("L2AGLb"))
            );
            cookieButton.click();
            System.out.println("✅ Cookies accepted with main selector");
            return;
        } catch (TimeoutException e) {
            System.out.println("⚠️ Main cookie selector failed, trying alternatives...");
        }

        // Intentar selectores alternativos
        String[] alternativeSelectors = {
                "button[id='L2AGLb']",
                "button[aria-label*='Accept']",
                "button[aria-label*='Acepto']"
        };

        for (String selector : alternativeSelectors) {
            try {
                WebElement button = driver.findElement(By.cssSelector(selector));
                if (button.isDisplayed() && button.isEnabled()) {
                    button.click();
                    System.out.println("✅ Cookies accepted with selector: " + selector);
                    return;
                }
            } catch (Exception e) {
                continue;
            }
        }

        // Si no encuentra botones de cookies, continuar (puede ser que no aparezcan)
        System.out.println("ℹ️ No cookie dialog found or already accepted");
    }

    private void performGoogleSearchSimple(String searchTerm) {
        System.out.println("🔍 Performing search for: " + searchTerm);

        // Buscar campo de búsqueda
        WebElement searchBox = null;

        // Intentar los selectores más comunes
        String[] searchSelectors = {
                "input[name='q']",
                "textarea[name='q']",
                "#APjFqb"
        };

        for (String selector : searchSelectors) {
            try {
                searchBox = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))
                );
                System.out.println("✅ Search box found with selector: " + selector);
                break;
            } catch (TimeoutException e) {
                continue;
            }
        }

        Assert.assertNotNull(searchBox, "Search box should be found");

        searchBox.clear();
        searchBox.sendKeys(searchTerm);
        searchBox.submit();

        System.out.println("✅ Search submitted successfully");
    }

    private void verifyGoogleSearchResultsSimple() {
        System.out.println("🔍 Verifying Google search results...");

        // Esperar a que la página cargue
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verificar por título (más confiable)
        String currentTitle = driver.getTitle();
        System.out.println("📋 Results page title: " + currentTitle);

        boolean titleContainsSearchTerms = currentTitle.toLowerCase().contains("selenium") ||
                currentTitle.toLowerCase().contains("grid") ||
                currentTitle.toLowerCase().contains("docker");

        if (titleContainsSearchTerms) {
            System.out.println("✅ Search results verified by title");
            return;
        }

        // Verificar por URL como alternativa
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("search") && currentUrl.contains("q=")) {
            System.out.println("✅ Search results verified by URL pattern");
            return;
        }

        Assert.fail("Could not verify search results");
    }

    private WebElement findDuckDuckGoSearchBoxSimple() {
        System.out.println("🔍 Finding DuckDuckGo search box...");

        String[] searchSelectors = {
                "#searchbox_input",              // Más común actualmente
                "#search_form_input_homepage",   // Selector original
                "input[name='q']",               // Por name
                "input[placeholder*='Search']"   // Por placeholder
        };

        for (String selector : searchSelectors) {
            try {
                WebElement searchBox = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))
                );
                System.out.println("✅ Search box found with selector: " + selector);
                return searchBox;
            } catch (TimeoutException e) {
                System.out.println("⚠️ Selector failed: " + selector);
                continue;
            }
        }

        return null;
    }

    private void verifyDuckDuckGoResultsSimple() {
        System.out.println("🔍 Verifying DuckDuckGo search results...");

        // Esperar a que cambie la URL
        try {
            wait.until(driver -> driver.getCurrentUrl().contains("q="));
        } catch (TimeoutException e) {
            System.out.println("⚠️ URL didn't change, but continuing verification...");
        }

        // Verificar por título primero
        String title = driver.getTitle();
        if (title.toLowerCase().contains("docker")) {
            System.out.println("✅ DuckDuckGo results verified by title");
            return;
        }

        // Intentar encontrar elementos de resultados
        String[] resultSelectors = {
                "[data-testid='result']",
                "#links",
                ".results",
                ".result"
        };

        for (String selector : resultSelectors) {
            try {
                WebElement results = driver.findElement(By.cssSelector(selector));
                if (results.isDisplayed()) {
                    System.out.println("✅ DuckDuckGo results verified by DOM elements: " + selector);
                    return;
                }
            } catch (Exception e) {
                continue;
            }
        }

        // Si llegamos aquí, al menos verificar que la URL cambió
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("q="), "URL should contain search parameter");
        System.out.println("✅ DuckDuckGo results verified by URL change");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            System.out.println("🧹 Closing browser session...");
            driver.quit();
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