import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class QuickTest {
    
    public static void main(String[] args) throws MalformedURLException, InterruptedException {
        String hubUrl = "http://localhost:4444/wd/hub";
        
        // Test Chrome
        System.out.println("🚀 Testing Chrome...");
        testBrowser("chrome", hubUrl);
        
        // Test Firefox
        System.out.println("🚀 Testing Firefox...");
        testBrowser("firefox", hubUrl);
        
        System.out.println("✅ ¡Tests completados exitosamente!");
    }
    
    private static void testBrowser(String browserName, String hubUrl) throws MalformedURLException, InterruptedException {
        WebDriver driver = null;
        
        try {
            // Configurar driver según navegador
            if (browserName.equals("chrome")) {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--no-sandbox");
                driver = new RemoteWebDriver(new URL(hubUrl), options);
            } else if (browserName.equals("firefox")) {
                FirefoxOptions options = new FirefoxOptions();
                driver = new RemoteWebDriver(new URL(hubUrl), options);
            }
            
            // Configurar timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().window().maximize();
            
            // Realizar test
            System.out.println("  📱 Navegando a Google...");
            driver.get("https://www.google.com");
            
            // Buscar elemento
            WebElement searchBox = driver.findElement(By.name("q"));
            searchBox.sendKeys("Selenium Grid Docker");
            searchBox.submit();
            
            // Verificar resultado
            Thread.sleep(2000);
            String title = driver.getTitle();
            
            if (title.contains("Selenium Grid Docker")) {
                System.out.println("  ✅ Test exitoso en " + browserName);
            } else {
                System.out.println("  ❌ Test falló en " + browserName);
            }
            
        } catch (Exception e) {
            System.out.println("  ❌ Error en " + browserName + ": " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
