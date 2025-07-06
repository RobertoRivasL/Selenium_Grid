#!/bin/bash

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}===========================================${NC}"
echo -e "${BLUE}     CONFIGURACIÓN DE TESTS ALLURE${NC}"
echo -e "${BLUE}===========================================${NC}"
echo ""

echo -e "${GREEN}🔧 Configurando estructura del proyecto...${NC}"

# 1. Crear estructura de directorios
echo -e "${YELLOW}📁 Creando directorios...${NC}"
mkdir -p src/test/java
mkdir -p src/test/resources
mkdir -p src/main/java

# 2. Mover archivos si están en el lugar incorrecto
echo -e "${YELLOW}📁 Organizando archivos...${NC}"
if [ -f "src/main/java/QuickTestCorrectedAllureTest.java" ]; then
    mv src/main/java/QuickTestCorrectedAllureTest.java src/test/java/
    echo "✅ Archivo movido a src/test/java/"
fi

# 3. Crear testng.xml
echo -e "${YELLOW}📝 Creando testng.xml...${NC}"
cat > src/test/resources/testng.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">

<suite name="SeleniumGridTestSuite" parallel="false" thread-count="1" verbose="1">

    <listeners>
        <listener class-name="io.qameta.allure.testng.AllureTestNg"/>
    </listeners>

    <test name="CrossBrowserTests" preserve-order="true">
        <classes>
            <class name="QuickTestCorrectedAllureTest"/>
        </classes>
    </test>

</suite>
EOF

# 4. Crear allure.properties
echo -e "${YELLOW}📝 Creando allure.properties...${NC}"
cat > src/test/resources/allure.properties << 'EOF'
# Configuración de Allure Reports
allure.results.directory=target/allure-results
allure.attach.screenshot.mode=ON_FAILURE
allure.attach.page.source=ON_FAILURE
EOF

# 5. Verificar estructura
echo -e "${YELLOW}📋 Verificando estructura del proyecto...${NC}"
tree src/ 2>/dev/null || find src/ -type f

echo ""
echo -e "${GREEN}✅ Configuración completada${NC}"
echo ""

echo -e "${BLUE}📋 Comandos para ejecutar tests:${NC}"
echo -e "${YELLOW}  1. Compilar:${NC} mvn clean compile"
echo -e "${YELLOW}  2. Ejecutar tests:${NC} mvn test"
echo -e "${YELLOW}  3. Ejecutar test específico:${NC} mvn test -Dtest=QuickTestCorrectedAllureTest"
echo -e "${YELLOW}  4. Generar reporte:${NC} mvn allure:report"
echo -e "${YELLOW}  5. Servir reporte:${NC} mvn allure:serve"
echo ""

echo -e "${GREEN}🎯 ¿Quieres ejecutar los tests ahora? (y/n)${NC}"
read -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${GREEN}🚀 Ejecutando tests...${NC}"
    mvn clean test

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Tests ejecutados exitosamente${NC}"
        echo -e "${BLUE}¿Quieres generar el reporte Allure? (y/n)${NC}"
        read -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            mvn allure:report
            echo -e "${GREEN}✅ Reporte generado en target/allure-reports${NC}"
        fi
    else
        echo -e "${RED}❌ Los tests fallaron. Verifica la configuración.${NC}"
    fi
fi

echo ""
echo -e "${GREEN}🎉 ¡Configuración completada!${NC}"