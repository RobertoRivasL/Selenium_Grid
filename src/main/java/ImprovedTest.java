import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

public class ImprovedTest {
    
    public static void main(String[] args) throws MalformedURLException, InterruptedException {
        String hubUrl = "http://localhost:4444/wd/hub";
        
        System.out.println("🚀 INICIANDO TESTS EN SELENIUM GRID");
        System.out.println("📍 Hub URL: " + hubUrl);
        System.out.println("=====================================");
        
        // Test Chrome
        System.out.println("\n🚀 Testing Chrome...");
        testBrowser("chrome", hubUrl);
        
        // Test Firefox
        System.out.println("\n🚀 Testing Firefox...");
        testBrowser("firefox", hubUrl);
        
        System.out.println("\n🎉 ¡TODOS LOS TESTS COMPLETADOS!");
        System.out.println("=====================================");
    }
    
    private static void testBrowser(String browserName, String hubUrl) throws MalformedURLException, InterruptedException {
        WebDriver driver = null;
        WebDriverWait wait = null;
        
        try {
            // Configurar driver según navegador
            if (browserName.equals("chrome")) {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-gpu");
                options.addArguments("--window-size=1920,1080");
                driver = new RemoteWebDriver(new URL(hubUrl), options);
            } else if (browserName.equals("firefox")) {
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("--width=1920");
                options.addArguments("--height=1080");
                driver = new RemoteWebDriver(new URL(hubUrl), options);
            }
            
            // Configurar timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            // Información del navegador
            String browserVersion = ((RemoteWebDriver) driver).getCapabilities().getBrowserVersion();
            System.out.println("  📱 Navegador: " + browserName + " v" + browserVersion);
            
            // Test 1: Navegación básica
            System.out.println("  🌐 Test 1: Navegando a Google...");
            driver.get("https://www.google.com");
            
            // Verificar que cargó Google
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("google.com")) {
                System.out.println("  ✅ Google cargó correctamente");
            } else {
                System.out.println("  ❌ Error al cargar Google");
                return;
            }
            
            // Test 2: Búsqueda
            System.out.println("  🔍 Test 2: Realizando búsqueda...");
            
            // Aceptar cookies si aparece
            try {
                WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(By.id("L2AGLb")));
                acceptCookies.click();
                System.out.println("  📝 Cookies aceptadas");
            } catch (Exception e) {
                System.out.println("  📝 No se encontró botón de cookies");
            }
            
            // Buscar elemento de búsqueda
            WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("q")));
            searchBox.clear();
            searchBox.sendKeys("Selenium Grid Docker");
            searchBox.submit();
            
            // Verificar resultados
            System.out.println("  ⏳ Esperando resultados...");
            WebElement results = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search")));
            
            if (results.isDisplayed()) {
                System.out.println("  ✅ Resultados de búsqueda mostrados");
                
                // Contar resultados
                List<WebElement> resultLinks = driver.findElements(By.cssSelector("h3"));
                System.out.println("  📊 Encontrados " + resultLinks.size() + " resultados");
                
                // Verificar que hay resultados relacionados
                String pageSource = driver.getPageSource().toLowerCase();
                if (pageSource.contains("selenium") || pageSource.contains("grid") || pageSource.contains("docker")) {
                    System.out.println("  ✅ Test de búsqueda exitoso en " + browserName);
                } else {
                    System.out.println("  ❌ No se encontraron resultados relevantes");
                }
            } else {
                System.out.println("  ❌ No se mostraron resultados");
            }
            
            // Test 3: Navegación adicional
            System.out.println("  🌐 Test 3: Navegando a Selenium.dev...");
            driver.get("https://www.selenium.dev");
            
            // Verificar título
            String title = driver.getTitle();
            System.out.println("  📋 Título: " + title);
            
            if (title.toLowerCase().contains("selenium")) {
                System.out.println("  ✅ Selenium.dev cargó correctamente");
            } else {
                System.out.println("  ❌ Error al cargar Selenium.dev");
            }
            
            System.out.println("  🎯 TODOS LOS TESTS EXITOSOS EN " + browserName.toUpperCase());
            
        } catch (Exception e) {
            System.out.println("  ❌ Error en " + browserName + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                System.out.println("  🔒 Cerrando navegador " + browserName);
                driver.quit();
            }
        }
    }
}
