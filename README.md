# Selenium Grid + Docker - Ejercicio Práctico

## 🎯 Objetivo
Configurar un entorno de Selenium Grid usando Docker para ejecutar tests distribuidos en múltiples navegadores (Chrome, Firefox, Edge).

## 📋 Prerrequisitos

### Software necesario:
- Docker (v20.10+)
- Docker Compose (v2.0+)
- Java 11+
- Maven 3.6+
- Git

### Verificación rápida:
```bash
docker --version
docker-compose --version
java -version
mvn --version
```

## 🏗️ Estructura del proyecto

```
selenium-grid-docker/
├── docker-compose.yml          # Configuración del Grid
├── pom.xml                     # Dependencias Maven
├── src/
│   ├── main/java/
│   └── test/
│       ├── java/
│       │   └── com/selenium/grid/tests/
│       │       └── SeleniumGridTest.java
│       └── resources/
│           └── testng.xml      # Configuración TestNG
├── commands.sh                 # Scripts de gestión
└── README.md                   # Esta guía
```

## 🚀 Inicio rápido

### 1. Clonar/Crear el proyecto
```bash
mkdir selenium-grid-docker
cd selenium-grid-docker
# Copiar todos los archivos proporcionados
```

### 2. Levantar el Selenium Grid
```bash
# Opción 1: Usando Docker Compose directamente
docker-compose up -d

# Opción 2: Usando el script de gestión
chmod +x commands.sh
./commands.sh
# Seleccionar opción 1
```

### 3. Verificar que el Grid esté funcionando
```bash
# Verificar contenedores
docker-compose ps

# Verificar Grid Console
open http://localhost:4444
```

### 4. Ejecutar los tests
```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar tests
mvn test

# O usar el script de gestión
./commands.sh
# Seleccionar opción 5
```

## 🔧 Configuración detallada

### Docker Compose - Componentes

#### Hub (Coordinador)
- **Puerto**: 4444 (WebDriver), 4442 (Events), 4443 (Publish)
- **Función**: Coordina la distribución de tests entre nodos
- **Console**: http://localhost:4444

#### Nodos (Ejecutores)
- **Chrome Node**: Puerto VNC 5900
- **Firefox Node**: Puerto VNC 5901
- **Edge Node**: Puerto VNC 5902

### Configuración de capacidades

#### Chrome
```java
ChromeOptions chromeOptions = new ChromeOptions();
chromeOptions.addArguments("--disable-dev-shm-usage");
chromeOptions.addArguments("--no-sandbox");
chromeOptions.addArguments("--disable-gpu");
driver = new RemoteWebDriver(new URL(hubUrl), chromeOptions);
```

#### Firefox
```java
FirefoxOptions firefoxOptions = new FirefoxOptions();
firefoxOptions.addArguments("--width=1920");
firefoxOptions.addArguments("--height=1080");
driver = new RemoteWebDriver(new URL(hubUrl), firefoxOptions);
```

## 🧪 Ejecución de tests

### Ejecutar todos los tests
```bash
mvn test
```

### Ejecutar tests específicos por browser
```bash
# Solo Chrome
mvn test -Dbrowser=chrome

# Solo Firefox  
mvn test -Dbrowser=firefox
```

### Ejecutar tests en paralelo
```bash
# TestNG ejecutará automáticamente los tests en paralelo
# según la configuración en testng.xml
mvn test -Dparallel=tests -DthreadCount=3
```

## 🔍 Monitoreo y debugging

### Grid Console
- **URL**: http://localhost:4444
- **Funciones**: Ver sesiones activas, nodos registrados, capacidades

### Logs en tiempo real
```bash
# Hub
docker-compose logs -f selenium-hub

# Nodos específicos
docker-compose logs -f chrome-node
docker-compose logs -f firefox-node

# Todos los servicios
docker-compose logs -f
```

### VNC (Visualización remota)
```bash
# Instalar cliente VNC
apt-get install vinagre  # Ubuntu/Debian
brew install vnc-viewer  # macOS

# Conectar a los nodos
vnc://localhost:5900  # Chrome
vnc://localhost:5901  # Firefox
vnc://localhost:5902  # Edge
```

## 🛠️ Comandos útiles

### Gestión del Grid
```bash
# Iniciar Grid
docker-compose up -d

# Parar Grid
docker-compose down

# Reiniciar Grid
docker-compose restart

# Ver estado
docker-compose ps

# Logs
docker-compose logs [service-name]
```

### Gestión de Maven
```bash
# Limpiar y compilar
mvn clean compile

# Ejecutar tests
mvn test

# Generar reportes
mvn surefire-report:report

# Ejecutar tests específicos
mvn test -Dtest=SeleniumGridTest#testGoogleSearch
```

## 📊 Escalabilidad

### Añadir más nodos
```yaml
# En docker-compose.yml
chrome-node-2:
  image: selenium/node-chrome:4.15.0
  depends_on:
    - selenium-hub
  environment:
    - HUB_HOST=selenium-hub
    - HUB_PORT=4444
    - NODE_MAX_INSTANCES=2
```

### Configurar diferentes versiones
```yaml
chrome-node-old:
  image: selenium/node-chrome:4.10.0
  # ... configuración
```

## 🔧 Troubleshooting

### Problemas comunes

#### Grid no inicia
```bash
# Verificar puertos ocupados
netstat -tlnp | grep :4444

# Limpiar contenedores
docker-compose down -v
docker system prune -f
```

#### Tests fallan por timeout
```java
// Aumentar timeouts
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
```

#### Nodos no se registran
```bash
# Verificar logs del hub
docker-compose logs selenium-hub

# Verificar conectividad
docker-compose exec chrome-node ping selenium-hub
```

### Optimizaciones de rendimiento

#### Memoria compartida
```yaml
volumes:
  - /dev/shm:/dev/shm  # Mejora rendimiento en Linux
```

#### Límites de sesiones
```yaml
environment:
  - NODE_MAX_INSTANCES=4    # Máximo 4 sesiones por nodo
  - NODE_MAX_SESSION=4      # Máximo 4 sesiones totales
```

## 📈 Métricas y monitoreo

### Verificar estado del Grid
```bash
curl -s http://localhost:4444/wd/hub/status | jq .
```

### Sesiones activas
```bash
curl -s http://localhost:4444/wd/hub/sessions | jq .
```

### Script de monitoreo
```bash
#!/bin/bash
while true; do
  echo "=== $(date) ==="
  curl -s http://localhost:4444/wd/hub/status | jq '.value.nodes[].availability'
  sleep 30
done
```

## 🔄 Integración con CI/CD

### Jenkins Pipeline
```groovy
pipeline {
    agent any
    stages {
        stage('Start Grid') {
            steps {
                sh 'docker-compose up -d'
            }
        }
        stage('Run Tests') {
            steps {
                sh 'mvn clean test'
            }
        }
        stage('Cleanup') {
            steps {
                sh 'docker-compose down'
            }
        }
    }
}
```

### GitHub Actions
```yaml
name: Selenium Grid Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Start Selenium Grid
        run: docker-compose up -d
      - name: Run Tests
        run: mvn test
      - name: Cleanup
        run: docker-compose down
```

## 📚 Recursos adicionales

- [Selenium Grid Documentation](https://selenium.dev/documentation/grid/)
- [Docker Selenium Images](https://github.com/SeleniumHQ/docker-selenium)
- [TestNG Documentation](https://testng.org/doc/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)

## 🎯 Próximos pasos

1. **Añadir más tests**: Crear tests para diferentes escenarios
2. **Reportes avanzados**: Integrar Allure o ExtentReports
3. **Kubernetes**: Migrar el Grid a Kubernetes
4. **Selenoid**: Explorar alternativas como Selenoid
5. **Parallel execution**: Optimizar ejecución paralela

¡Listo para ejecutar tests distribuidos! 🚀