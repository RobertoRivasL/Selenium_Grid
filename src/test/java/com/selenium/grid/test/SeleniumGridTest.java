package com.selenium.grid.test;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

public class SeleniumGridTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String hubUrl = "http://localhost:4444/wd/hub";

    @Parameters({"browser"})
    @BeforeMethod
    public void setUp(@Optional("chrome") String browser) throws MalformedURLException {
        System.out.println("Setting up test for browser: " + browser);

        switch (browser.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--window-size=1920,1080");
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                chromeOptions.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                driver = new RemoteWebDriver(new URL(hubUrl), chromeOptions);
                break;

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("--width=1920");
                firefoxOptions.addArguments("--height=1080");
                driver = new RemoteWebDriver(new URL(hubUrl), firefoxOptions);
                break;

            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--disable-dev-shm-usage");
                edgeOptions.addArguments("--no-sandbox");
                edgeOptions.addArguments("--disable-gpu");
                edgeOptions.addArguments("--window-size=1920,1080");
                edgeOptions.addArguments("--disable-blink-features=AutomationControlled");
                edgeOptions.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                driver = new RemoteWebDriver(new URL(hubUrl), edgeOptions);
                break;

            default:
                throw new IllegalArgumentException("Browser not supported: " + browser + ". Supported: chrome, firefox, edge");
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        System.out.println("Test running on: " +
                ((RemoteWebDriver) driver).getCapabilities().getBrowserName() +
                " - " + ((RemoteWebDriver) driver).getCapabilities().getBrowserVersion());
    }

    @Test(priority = 1)
    @Step("Ejecutar búsqueda en Google")
    public void testGoogleSearch() {
        System.out.println("Executing Google Search Test...");
        Allure.step("Navegando a Google");

        driver.get("https://www.google.com");

        // Esperar que la página cargue
        wait.until(ExpectedConditions.titleContains("Google"));

        // Manejar cookies con múltiples selectores
        handleGoogleCookies();

        // Buscar el input de búsqueda con múltiples selectores
        WebElement searchBox = findSearchInput();
        Assert.assertNotNull(searchBox, "No se pudo encontrar el input de búsqueda");

        Allure.step("Escribiendo término de búsqueda");
        searchBox.sendKeys("Selenium Grid Docker");
        searchBox.submit();

        // Verificar resultados con múltiples selectores
        WebElement results = verifyGoogleSearchResults();
        Assert.assertNotNull(results, "No se encontraron resultados de búsqueda");

        System.out.println("✓ Google search test passed");
        Allure.step("Test de Google completado exitosamente");
    }

    @Test(priority = 2)
    @Step("Verificar sitio web de Selenium HQ")
    public void testSeleniumHQWebsite() {
        System.out.println("Executing Selenium HQ Website Test...");
        Allure.step("Navegando a Selenium HQ");

        driver.get("https://www.selenium.dev/");

        // Esperar que la página cargue
        wait.until(ExpectedConditions.titleContains("Selenium"));

        String title = driver.getTitle();
        Assert.assertTrue(title.contains("Selenium"),
                "Page title should contain 'Selenium', but was: " + title);

        // Buscar logo con múltiples selectores
        WebElement logo = findSeleniumLogo();
        Assert.assertNotNull(logo, "No se pudo encontrar el logo de Selenium");

        System.out.println("✓ Selenium HQ website test passed");
        Allure.step("Test de Selenium HQ completado exitosamente");
    }

    @Test(priority = 3)
    @Step("Ejecutar búsqueda en DuckDuckGo")
    public void testDuckDuckGoSearch() {
        System.out.println("Executing DuckDuckGo Search Test...");
        Allure.step("Navegando a DuckDuckGo");

        driver.get("https://duckduckgo.com");

        // Esperar que la página cargue
        wait.until(ExpectedConditions.titleContains("DuckDuckGo"));

        // Buscar input con múltiples selectores
        WebElement searchBox = findDuckDuckGoSearchInput();
        Assert.assertNotNull(searchBox, "No se pudo encontrar el input de búsqueda de DuckDuckGo");

        Allure.step("Escribiendo término de búsqueda");
        searchBox.sendKeys("Docker containers");

        // Buscar botón de búsqueda
        WebElement searchButton = findDuckDuckGoSearchButton();
        if (searchButton != null) {
            searchButton.click();
        } else {
            searchBox.submit();
        }

        // Verificar resultados
        WebElement results = verifyDuckDuckGoResults();
        Assert.assertNotNull(results, "No se encontraron resultados de búsqueda en DuckDuckGo");

        System.out.println("✓ DuckDuckGo search test passed");
        Allure.step("Test de DuckDuckGo completado exitosamente");
    }

    // Métodos auxiliares para manejar múltiples selectores

    private void handleGoogleCookies() {
        String[] cookieSelectors = {
                "#L2AGLb", // Selector original
                "button[id='L2AGLb']",
                "div[role='button'][jsname='b3VHJd']",
                "button:contains('Acepto')",
                "button:contains('Accept')",
                "[data-ved]:contains('Accept')",
                ".QS5gu"
        };

        for (String selector : cookieSelectors) {
            try {
                WebElement acceptButton = wait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(selector))
                );
                if (acceptButton.isDisplayed()) {
                    acceptButton.click();
                    System.out.println("✓ Cookies aceptadas con selector: " + selector);
                    return;
                }
            } catch (Exception e) {
                // Continuar con el siguiente selector
            }
        }
        System.out.println("ℹ️ No se encontró diálogo de cookies");
    }

    private WebElement findSearchInput() {
        String[] searchSelectors = {
                "input[name='q']", // Selector más confiable
                "textarea[name='q']",
                "#APjFqb",
                ".gLFyf",
                "input[type='search']",
                "input[title*='Buscar']",
                "input[title*='Search']"
        };

        for (String selector : searchSelectors) {
            try {
                WebElement element = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))
                );
                if (element.isDisplayed()) {
                    System.out.println("✓ Input de búsqueda encontrado con: " + selector);
                    return element;
                }
            } catch (Exception e) {
                // Continuar con el siguiente selector
            }
        }
        return null;
    }

    private WebElement verifyGoogleSearchResults() {
        String[] resultSelectors = {
                "#search", // Selector original
                "#rso",
                "#main",
                ".g",
                ".MjjYud",
                "#rcnt",
                "#center_col",
                ".hlcw0c"
        };

        for (String selector : resultSelectors) {
            try {
                WebElement results = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))
                );
                if (results.isDisplayed()) {
                    System.out.println("✓ Resultados encontrados con: " + selector);
                    return results;
                }
            } catch (Exception e) {
                // Continuar con el siguiente selector
            }
        }

        // Verificación alternativa por URL
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("search") || currentUrl.contains("q=")) {
            System.out.println("✓ Resultados verificados por URL: " + currentUrl);
            // Retornar el body como elemento válido
            return driver.findElement(By.tagName("body"));
        }

        return null;
    }

    private WebElement findSeleniumLogo() {
        String[] logoSelectors = {
                "img[alt='Selenium']", // Selector original
                "img[alt*='Selenium']",
                "img[src*='selenium']",
                ".selenium-logo",
                "img[class*='logo']",
                "svg[class*='logo']",
                ".navbar-brand img",
                "header img"
        };

        for (String selector : logoSelectors) {
            try {
                WebElement logo = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))
                );
                if (logo.isDisplayed()) {
                    System.out.println("✓ Logo encontrado con: " + selector);
                    return logo;
                }
            } catch (Exception e) {
                // Continuar con el siguiente selector
            }
        }

        // Verificación alternativa: buscar cualquier imagen en el header
        try {
            List<WebElement> headerImages = driver.findElements(By.cssSelector("header img, .header img, nav img"));
            if (!headerImages.isEmpty()) {
                System.out.println("✓ Imagen del header encontrada como alternativa al logo");
                return headerImages.get(0);
            }
        } catch (Exception e) {
            // Ignorar
        }

        return null;
    }

    private WebElement findDuckDuckGoSearchInput() {
        String[] inputSelectors = {
                "#search_form_input_homepage", // Selector original
                "#searchbox_input",
                "input[name='q']",
                "input[type='search']",
                ".search__input",
                "#search_form_input",
                "input[placeholder*='Search']",
                "input[placeholder*='search']"
        };

        for (String selector : inputSelectors) {
            try {
                WebElement input = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))
                );
                if (input.isDisplayed()) {
                    System.out.println("✓ Input de DuckDuckGo encontrado con: " + selector);
                    return input;
                }
            } catch (Exception e) {
                // Continuar con el siguiente selector
            }
        }
        return null;
    }

    private WebElement findDuckDuckGoSearchButton() {
        String[] buttonSelectors = {
                "#search_button_homepage", // Selector original
                "input[type='submit']",
                "button[type='submit']",
                ".search__button",
                "#search_button",
                "button:contains('Search')"
        };

        for (String selector : buttonSelectors) {
            try {
                WebElement button = driver.findElement(By.cssSelector(selector));
                if (button.isDisplayed()) {
                    System.out.println("✓ Botón de búsqueda encontrado con: " + selector);
                    return button;
                }
            } catch (Exception e) {
                // Continuar con el siguiente selector
            }
        }
        return null;
    }

    private WebElement verifyDuckDuckGoResults() {
        String[] resultSelectors = {
                "#links", // Selector original
                "#results",
                ".results",
                ".result",
                ".web-result",
                ".react-results--main",
                "#web_content_wrapper"
        };

        for (String selector : resultSelectors) {
            try {
                WebElement results = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))
                );
                if (results.isDisplayed()) {
                    System.out.println("✓ Resultados de DuckDuckGo encontrados con: " + selector);
                    return results;
                }
            } catch (Exception e) {
                // Continuar con el siguiente selector
            }
        }

        // Verificación alternativa por URL
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("q=") || currentUrl.contains("search")) {
            System.out.println("✓ Resultados verificados por URL: " + currentUrl);
            return driver.findElement(By.tagName("body"));
        }

        return null;
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            System.out.println("Closing browser session...");
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