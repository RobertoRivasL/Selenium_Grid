import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

public class ImprovedTest {

    public static void main(String[] args) throws MalformedURLException {
        String hubUrl = "http://localhost:4444/wd/hub";
        System.out.println("🚀 INICIANDO TESTS EN SELENIUM GRID");
        System.out.println("📍 Hub URL: " + hubUrl);
        System.out.println("=====================================");

        boolean chromeOk = runTest("chrome", hubUrl);
        boolean firefoxOk = runTest("firefox", hubUrl);

        System.out.println("\n=====================================");
        if (chromeOk && firefoxOk) {
            System.out.println("✅ ¡TODOS LOS TESTS PASARON EXITOSAMENTE!");
        } else {
            System.out.println("❌ ALGUNOS TESTS FALLARON.");
        }
        System.out.println("=====================================");
    }

    private static boolean runTest(String browserName, String hubUrl) throws MalformedURLException {
        WebDriver driver = null;
        WebDriverWait wait = null;
        boolean allPassed = true;

        try {
            driver = createDriver(browserName, hubUrl);
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            String browserVersion = ((RemoteWebDriver) driver).getCapabilities().getBrowserVersion();
            System.out.println("\n🚀 Testing " + browserName + " v" + browserVersion);

            // Test 1: Navegación a Google
            if (!navigateToGoogle(driver)) {
                allPassed = false;
            }

            // Test 2: Búsqueda en Google
            if (!searchGoogle(driver, wait, browserName)) {
                allPassed = false;
            }

            // Test 3: Navegación a Selenium.dev
            if (!navigateToSeleniumDev(driver)) {
                allPassed = false;
            }

        } catch (Exception e) {
            System.out.println("  ❌ Error en " + browserName + ": " + e.getMessage());
            allPassed = false;
        } finally {
            if (driver != null) {
                System.out.println("  🔒 Cerrando navegador " + browserName);
                driver.quit();
            }
        }
        return allPassed;
    }

    private static WebDriver createDriver(String browserName, String hubUrl) throws MalformedURLException {
        if (browserName.equals("chrome")) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-dev-shm-usage", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");
            return new RemoteWebDriver(new URL(hubUrl), options);
        } else if (browserName.equals("firefox")) {
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("--width=1920", "--height=1080");
            return new RemoteWebDriver(new URL(hubUrl), options);
        }
        throw new IllegalArgumentException("Navegador no soportado: " + browserName);
    }

    private static boolean navigateToGoogle(WebDriver driver) {
        System.out.println("  🌐 Test 1: Navegando a Google...");
        driver.get("https://www.google.com");
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("google.com")) {
            System.out.println("  ✅ Google cargó correctamente");
            return true;
        } else {
            System.out.println("  ❌ Error al cargar Google");
            return false;
        }
    }

    private static boolean searchGoogle(WebDriver driver, WebDriverWait wait, String browserName) {
        System.out.println("  🔍 Test 2: Realizando búsqueda...");
        try {
            // Aceptar cookies si aparece
            try {
                WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(By.id("L2AGLb")));
                acceptCookies.click();
                System.out.println("  📝 Cookies aceptadas");
            } catch (Exception e) {
                System.out.println("  📝 No se encontró botón de cookies");
            }

            WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("q")));
            searchBox.clear();
            searchBox.sendKeys("Selenium Grid Docker");
            String oldUrl = driver.getCurrentUrl();
            searchBox.submit();

            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(oldUrl)));
            System.out.println("  ⏳ Esperando resultados...");

            try {
                WebElement firstResult = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3")));
                if (firstResult.isDisplayed()) {
                    System.out.println("  ✅ Resultados de búsqueda mostrados");
                    List<WebElement> resultLinks = driver.findElements(By.cssSelector("h3"));
                    System.out.println("  📊 Encontrados " + resultLinks.size() + " resultados");
                    String pageSource = driver.getPageSource().toLowerCase();
                    if (pageSource.contains("selenium") || pageSource.contains("grid") || pageSource.contains("docker")) {
                        System.out.println("  ✅ Test de búsqueda exitoso en " + browserName);
                        return true;
                    } else {
                        System.out.println("  ❌ No se encontraron resultados relevantes");
                        return false;
                    }
                } else {
                    System.out.println("  ❌ No se mostraron resultados");
                    return false;
                }
            } catch (Exception e) {
                String pageSource = driver.getPageSource().toLowerCase();
                if (pageSource.contains("captcha") || pageSource.contains("no soy un robot")) {
                    System.out.println("  ⚠️ Google muestra un captcha o bloqueo anti-bot");
                } else {
                    System.out.println("  ❌ No se encontraron resultados ni captcha");
                }
                return false;
            }
        } catch (Exception e) {
            System.out.println("  ❌ Error durante la búsqueda: " + e.getMessage());
            return false;
        }
    }

    private static boolean navigateToSeleniumDev(WebDriver driver) {
        System.out.println("  🌐 Test 3: Navegando a Selenium.dev...");
        driver.get("https://www.selenium.dev");
        String title = driver.getTitle();
        System.out.println("  📋 Título: " + title);
        if (title.toLowerCase().contains("selenium")) {
            System.out.println("  ✅ Selenium.dev cargó correctamente");
            return true;
        } else {
            System.out.println("  ❌ Error al cargar Selenium.dev");
            return false;
        }
    }
}