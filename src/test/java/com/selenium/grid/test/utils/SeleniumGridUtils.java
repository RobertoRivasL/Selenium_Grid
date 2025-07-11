package com.selenium.grid.test.utils;

import io.qameta.allure.Allure;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

///**
// * ✅ UTILIDADES PARA SELENIUM GRID
// *
// * Funciones helper para:
// * - Manejo de screenshots
// * - Validaciones robustas
// * - Información del Grid
// * - Manejo de ventanas/tabs
// * - Logging avanzado
// */
public class SeleniumGridUtils {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * ✅ Toma screenshot y lo adjunta a Allure
     */
    public static void takeScreenshot(WebDriver driver, String stepName) {
        try {
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                String attachmentName = String.format("%s_%s", stepName, timestamp);

                Allure.addAttachment(attachmentName, "image/png",
                        new ByteArrayInputStream(screenshot), "png");

                System.out.println("📸 Screenshot tomado: " + attachmentName);
            }
        } catch (Exception e) {
            System.err.println("❌ Error tomando screenshot: " + e.getMessage());
        }
    }

    /**
     * ✅ Espera inteligente con múltiples condiciones
     */
    public static boolean waitForCondition(WebDriver driver, Duration timeout,
                                           ExpectedCondition<?>... conditions) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);

        for (ExpectedCondition<?> condition : conditions) {
            try {
                wait.until(condition);
                return true;
            } catch (TimeoutException e) {
                System.out.println("⚠️ Condición no cumplida: " + condition.toString());
                continue;
            }
        }
        return false;
    }

    /**
     * ✅ Información detallada del Grid y sesión
     */
    public static GridSessionInfo getGridSessionInfo(WebDriver driver) {
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
     * ✅ Manejo seguro de ventanas/tabs
     */
    public static boolean switchToWindow(WebDriver driver, String windowTitle, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        String originalWindow = driver.getWindowHandle();

        try {
            // Espera hasta que haya más de una ventana abierta
            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> allWindows = driver.getWindowHandles();

            for (String window : allWindows) {
                if (!window.equals(originalWindow)) {
                    driver.switchTo().window(window);
                    if (driver.getTitle().contains(windowTitle)) {
                        System.out.println("✅ Cambiado a ventana: " + windowTitle);
                        return true;
                    }
                }
            }
        } catch (TimeoutException e) {
            System.err.println("❌ Timeout esperando ventana: " + windowTitle);
        }

        driver.switchTo().window(originalWindow);
        return false;
    }

    /**
     * ✅ Scroll inteligente a elemento
     */
    public static void scrollToElement(WebDriver driver, WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                    element
            );
            Thread.sleep(500); // Pequeña pausa para que complete el scroll
            System.out.println("📍 Scroll realizado al elemento");
        } catch (Exception e) {
            System.err.println("❌ Error en scroll: " + e.getMessage());
        }
    }

    /**
     * ✅ Validación de conectividad con el Grid
     */
    public static boolean isGridAvailable(String hubUrl) {
        try {
            // Implementar ping al Grid Hub
            java.net.URL url = new java.net.URL(hubUrl + "/status");
            java.net.HttpURLConnection connection =
                    (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            boolean available = responseCode == 200;

            System.out.println(available ?
                    "✅ Grid disponible en: " + hubUrl :
                    "❌ Grid no disponible: " + responseCode);

            return available;
        } catch (Exception e) {
            System.err.println("❌ Error verificando Grid: " + e.getMessage());
            return false;
        }
    }

    /**
     * ✅ Limpieza de cookies y storage
     */
    public static void cleanBrowserState(WebDriver driver) {
        try {
            // Limpiar cookies
            driver.manage().deleteAllCookies();

            // Limpiar localStorage y sessionStorage
            ((JavascriptExecutor) driver).executeScript(
                    "window.localStorage.clear(); window.sessionStorage.clear();"
            );

            System.out.println("🧹 Estado del navegador limpiado");
        } catch (Exception e) {
            System.err.println("❌ Error limpiando estado: " + e.getMessage());
        }
    }

    /**
     * ✅ Verificar si un elemento está realmente visible
     */
    public static boolean isElementVisibleAndClickable(WebDriver driver, WebElement element) {
        try {
            // Verificar que esté displayed
            if (!element.isDisplayed()) return false;

            // Verificar que esté enabled
            if (!element.isEnabled()) return false;

            // Verificar que no esté oculto por CSS
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
     * ✅ Retry con exponential backoff
     */
    public static <T> T retryOperation(java.util.function.Supplier<T> operation,
                                       int maxAttempts, Duration initialDelay) {
        Exception lastException = null;
        Duration currentDelay = initialDelay;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                System.out.println("⚠️ Intento " + attempt + " falló: " + e.getMessage());

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(currentDelay.toMillis());
                        currentDelay = currentDelay.multipliedBy(2); // Exponential backoff
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
     * ✅ Clase para información de sesión del Grid
     */
    public static class GridSessionInfo {
        private final String browserName;
        private final String browserVersion;
        private final String sessionId;
        private final String platform;

        public GridSessionInfo(String browserName, String browserVersion,
                               String sessionId, String platform) {
            this.browserName = browserName;
            this.browserVersion = browserVersion;
            this.sessionId = sessionId;
            this.platform = platform;
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
    }

    /**
     * ✅ Logger personalizado para tests
     */
    public static void logTestStep(String stepName, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String threadName = Thread.currentThread().getName();

        System.out.println(String.format("[%s] [%s] 🔹 %s: %s",
                timestamp, threadName, stepName, details));

        // También enviarlo a Allure
        Allure.step(stepName + ": " + details);
    }
}