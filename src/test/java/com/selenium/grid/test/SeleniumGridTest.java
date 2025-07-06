package com.selenium.grid.test;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
                driver = new RemoteWebDriver(new URL(hubUrl), chromeOptions);
                break;

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("--width=1920");
                firefoxOptions.addArguments("--height=1080");
                driver = new RemoteWebDriver(new URL(hubUrl), firefoxOptions);
                break;

            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Info del nodo donde se ejecuta
        System.out.println("Test running on: " +
                ((RemoteWebDriver) driver).getCapabilities().getBrowserName() +
                " - " + ((RemoteWebDriver) driver).getCapabilities().getBrowserVersion());
    }

    @Test(priority = 1)
    public void testGoogleSearch() {
        System.out.println("Executing Google Search Test...");

        driver.get("https://www.google.com");

        // Aceptar cookies si aparece
        try {
            WebElement acceptCookies = driver.findElement(By.id("L2AGLb"));
            if (acceptCookies.isDisplayed()) {
                acceptCookies.click();
            }
        } catch (Exception e) {
            System.out.println("No cookies dialog found");
        }

        WebElement searchBox = driver.findElement(By.name("q"));
        searchBox.sendKeys("Selenium Grid Docker");
        searchBox.submit();

        // Verificar que aparecen resultados
        WebElement results = wait.until(driver ->
                driver.findElement(By.id("search")));

        Assert.assertTrue(results.isDisplayed(), "Search results should be displayed");
        System.out.println("✓ Google search test passed");
    }

    @Test(priority = 2)
    public void testSeleniumHQWebsite() {
        System.out.println("Executing Selenium HQ Website Test...");

        driver.get("https://www.selenium.dev/");

        // Verificar título
        String title = driver.getTitle();
        Assert.assertTrue(title.contains("Selenium"),
                "Page title should contain 'Selenium', but was: " + title);

        // Verificar que el logo está presente
        WebElement logo = driver.findElement(By.cssSelector("img[alt='Selenium']"));
        Assert.assertTrue(logo.isDisplayed(), "Selenium logo should be visible");

        System.out.println("✓ Selenium HQ website test passed");
    }

    @Test(priority = 3)
    public void testDuckDuckGoSearch() {
        System.out.println("Executing DuckDuckGo Search Test...");

        driver.get("https://duckduckgo.com");

        WebElement searchBox = driver.findElement(By.id("search_form_input_homepage"));
        searchBox.sendKeys("Docker containers");

        WebElement searchButton = driver.findElement(By.id("search_button_homepage"));
        searchButton.click();

        // Verificar que aparecen resultados
        WebElement results = wait.until(driver ->
                driver.findElement(By.id("links")));

        Assert.assertTrue(results.isDisplayed(), "Search results should be displayed");
        System.out.println("✓ DuckDuckGo search test passed");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            System.out.println("Closing browser session...");
            driver.quit();
        }
    }

    // Método para obtener info del navegador actual
    public String getBrowserInfo() {
        if (driver instanceof RemoteWebDriver) {
            RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
            return remoteDriver.getCapabilities().getBrowserName() +
                    " v" + remoteDriver.getCapabilities().getBrowserVersion();
        }
        return "Unknown browser";
    }
}