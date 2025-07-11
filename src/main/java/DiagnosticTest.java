import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;

public class DiagnosticTest {
    public static void main(String[] args) throws Exception {
        String hubUrl = "http://localhost:4444";
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        
        WebDriver driver = new RemoteWebDriver(new URL(hubUrl), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        try {
            System.out.println("🚀 Navegando a Google...");
            driver.get("https://www.google.com");
            
            System.out.println("📋 Título actual: '" + driver.getTitle() + "'");
            System.out.println("🌐 URL actual: '" + driver.getCurrentUrl() + "'");
            
            // Realizar búsqueda
            WebElement searchBox = driver.findElement(By.name("q"));
            searchBox.sendKeys("Selenium Grid Docker");
            searchBox.submit();
            
            Thread.sleep(3000);
            
            System.out.println("📋 Título después de búsqueda: '" + driver.getTitle() + "'");
            System.out.println("🌐 URL después de búsqueda: '" + driver.getCurrentUrl() + "'");
            
            // Verificar si contiene las palabras clave
            String title = driver.getTitle().toLowerCase();
            boolean containsSelenium = title.contains("selenium");
            boolean containsGrid = title.contains("grid");
            boolean containsDocker = title.contains("docker");
            
            System.out.println("🔍 Contiene 'selenium': " + containsSelenium);
            System.out.println("🔍 Contiene 'grid': " + containsGrid);
            System.out.println("🔍 Contiene 'docker': " + containsDocker);
            
            if (containsSelenium && containsGrid && containsDocker) {
                System.out.println("✅ TEST EXITOSO - Encontró todos los términos");
            } else {
                System.out.println("❌ TEST FALLÓ - No encontró todos los términos");
            }
            
        } finally {
            driver.quit();
        }
    }
}
