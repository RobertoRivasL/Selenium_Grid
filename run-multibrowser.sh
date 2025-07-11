#!/bin/bash

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}      SELENIUM GRID - MULTI-BROWSER TESTS${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# Verificar que el Grid esté corriendo
echo -e "${YELLOW}🔍 Verificando Selenium Grid...${NC}"
if curl -s http://localhost:4444/wd/hub/status > /dev/null; then
    echo -e "${GREEN}✅ Selenium Grid está corriendo${NC}"
else
    echo -e "${RED}❌ Selenium Grid no está corriendo${NC}"
    echo -e "${YELLOW}💡 Ejecuta: docker-compose up -d${NC}"
    exit 1
fi

echo ""
echo -e "${PURPLE}🌐 Selecciona el modo de ejecución:${NC}"
echo ""
echo -e "${YELLOW}1.${NC} QuickAllureTest SOLO (tu clase original + Edge)"
echo -e "${YELLOW}2.${NC} Configuración ORIGINAL corregida"
echo -e "${YELLOW}3.${NC} TODAS las clases en paralelo (Chrome + Firefox + Edge)"
echo -e "${YELLOW}4.${NC} SeleniumGridTest - Chrome + Firefox + Edge paralelo"
echo -e "${YELLOW}5.${NC} SeleniumGridTest - Secuencial (debugging)"
echo -e "${YELLOW}6.${NC} SeleniumGridTest - Solo Chrome"
echo -e "${YELLOW}7.${NC} SeleniumGridTest - Solo Firefox"
echo -e "${YELLOW}8.${NC} SeleniumGridTest - Solo Edge"
echo -e "${YELLOW}9.${NC} Test BÁSICO de conectividad"
echo -e "${YELLOW}10.${NC} Ver estado del Grid"
echo ""
echo -n "Elige una opción (1-10): "
read -n 1 opcion
echo ""
echo ""

case $opcion in
    1)
        echo -e "${GREEN}🚀 Ejecutando QuickAllureTest ÚNICAMENTE (con Edge)${NC}"
        echo -e "${BLUE}📝 Usando: testng-quickallure-only.xml${NC}"
        mvn clean test -DsuiteXmlFile=src/test/resources/testng-quickallure-only.xml
        ;;
    2)
        echo -e "${GREEN}🚀 Ejecutando configuración ORIGINAL corregida${NC}"
        echo -e "${BLUE}📝 Usando: testng-original-fixed.xml${NC}"
        mvn clean test -DsuiteXmlFile=src/test/resources/testng-original-fixed.xml
        ;;
    3)
        echo -e "${GREEN}🚀 Ejecutando TODAS las clases en paralelo (Chrome + Firefox + Edge)${NC}"
        echo -e "${BLUE}📝 Usando: testng-multibrowser.xml (completo)${NC}"
        mvn clean test -DsuiteXmlFile=src/test/resources/testng-multibrowser.xml
        ;;
    4)
        echo -e "${GREEN}🚀 SeleniumGridTest - Chrome + Firefox + Edge PARALELO${NC}"
        echo -e "${BLUE}📝 Usando: testng-seleniumgrid-parallel.xml${NC}"
        mvn clean test -DsuiteXmlFile=src/test/resources/testng-seleniumgrid-parallel.xml
        ;;
    5)
        echo -e "${GREEN}🚀 SeleniumGridTest - SECUENCIAL (debugging)${NC}"
        echo -e "${BLUE}📝 Usando: testng-sequential.xml${NC}"
        mvn clean test -DsuiteXmlFile=src/test/resources/testng-sequential.xml
        ;;
    6)
        echo -e "${GREEN}🚀 SeleniumGridTest - Solo CHROME${NC}"
        mvn clean test -Dbrowser=chrome -Dtest=SeleniumGridTest
        ;;
    7)
        echo -e "${GREEN}🚀 SeleniumGridTest - Solo FIREFOX${NC}"
        mvn clean test -Dbrowser=firefox -Dtest=SeleniumGridTest
        ;;
    8)
        echo -e "${GREEN}🚀 SeleniumGridTest - Solo EDGE${NC}"
        mvn clean test -Dbrowser=edge -Dtest=SeleniumGridTest
        ;;
    9)
        echo -e "${GREEN}🚀 Test BÁSICO de conectividad${NC}"
        mvn clean test -Dtest=BasicGridConnectionTest
        ;;
    10)
        echo -e "${GREEN}🔍 Estado del Selenium Grid:${NC}"
        echo ""
        echo -e "${YELLOW}📊 Información del Hub:${NC}"
        curl -s http://localhost:4444/wd/hub/status | jq . 2>/dev/null || curl -s http://localhost:4444/wd/hub/status
        echo ""
        echo -e "${YELLOW}🐳 Contenedores Docker:${NC}"
        docker-compose ps
        echo ""
        echo -e "${YELLOW}🌐 URLs de VNC:${NC}"
        echo "Chrome: vnc://localhost:5900 o http://localhost:7900"
        echo "Firefox: vnc://localhost:5901 o http://localhost:7901"
        echo "Edge: vnc://localhost:5902 o http://localhost:7902"
        ;;
    *)
        echo -e "${RED}❌ Opción no válida${NC}"
        exit 1
        ;;
esac

# Verificar resultado y ofrecer reporte
if [ $? -eq 0 ] && [ $opcion -le 9 ]; then
    echo ""
    echo -e "${GREEN}✅ Tests completados exitosamente${NC}"
    echo ""
    echo -e "${PURPLE}📊 ¿Quieres generar el reporte Allure? (y/n)${NC}"
    read -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}📈 Generando reporte Allure...${NC}"
        mvn allure:report
        echo -e "${GREEN}✅ Reporte generado en: target/allure-reports${NC}"
        echo ""
        echo -e "${PURPLE}🌐 ¿Quieres abrir el reporte en el navegador? (y/n)${NC}"
        read -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            mvn allure:serve
        fi
    fi
elif [ $opcion -le 9 ]; then
    echo -e "${RED}❌ Los tests fallaron. Revisa los logs arriba.${NC}"
fi

echo ""
echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}           ¡Proceso completado!${NC}"
echo -e "${BLUE}================================================${NC}"