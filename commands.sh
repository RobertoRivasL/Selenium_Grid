#!/bin/bash

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Chequeo de dependencias
check_dependencies() {
    local missing=0
    for cmd in docker-compose curl nc python3 mvn; do
        if ! command -v $cmd &> /dev/null; then
            echo -e "${RED}❌ Falta la dependencia: $cmd${NC}"
            missing=1
        fi
    done
    if [ $missing -eq 1 ]; then
        echo -e "${RED}Por favor instala las dependencias faltantes y vuelve a intentarlo.${NC}"
        exit 1
    fi
}

# Limpieza de pantalla robusta
safe_clear() {
    command -v clear &> /dev/null && clear
}

# Menú principal
show_menu() {
    echo -e "${BLUE}===========================================${NC}"
    echo -e "${BLUE}     SELENIUM GRID DOCKER MANAGER${NC}"
    echo -e "${BLUE}===========================================${NC}"
    echo "1. Levantar Selenium Grid"
    echo "2. Verificar estado del Grid"
    echo "3. Ver logs del Hub"
    echo "4. Ver logs de los nodos"
    echo "5. Ejecutar tests"
    echo "6. Parar el Grid"
    echo "7. Limpiar contenedores"
    echo "8. Abrir Grid Console"
    echo "9. Verificar conectividad"
    echo "0. Salir"
    echo -e "${BLUE}===========================================${NC}"
}

start_grid() {
    echo -e "${GREEN}🚀 Levantando Selenium Grid...${NC}"
    if ! docker-compose up -d; then
        echo -e "${RED}❌ Error al levantar el Grid.${NC}"
        return
    fi
    echo -e "${YELLOW}⏳ Esperando que el Grid esté listo...${NC}"
    sleep 10
    echo -e "${GREEN}✅ Grid iniciado. Verificando estado...${NC}"
    check_grid_status
}

check_grid_status() {
    echo -e "${BLUE}📊 Estado del Selenium Grid:${NC}\n"
    echo -e "${YELLOW}📦 Contenedores:${NC}"
    docker-compose ps || echo -e "${RED}No se pudo obtener el estado de los contenedores.${NC}"
    echo ""
    echo -e "${YELLOW}🔍 Estado del Grid:${NC}"
    curl -s http://localhost:4444/wd/hub/status | python3 -m json.tool 2>/dev/null || echo "Grid no disponible"
    echo ""
    echo -e "${YELLOW}🌐 URLs importantes:${NC}"
    echo "Grid Console: http://localhost:4444"
    echo "Hub Status: http://localhost:4444/wd/hub/status"
    echo "Sessions: http://localhost:4444/ui/index.html#/sessions"
}

view_hub_logs() {
    echo -e "${BLUE}📋 Logs del Selenium Hub:${NC}"
    docker-compose logs -f selenium-hub || echo -e "${RED}No se pudo obtener logs del Hub.${NC}"
}

view_node_logs() {
    echo -e "${BLUE}📋 Logs de los nodos:${NC}"
    echo "Seleccione el nodo:"
    echo "1. Chrome"
    echo "2. Firefox"
    echo "3. Edge"
    echo "4. Todos"
    read -p "Opción: " node_choice
    case $node_choice in
        1) docker-compose logs -f chrome-node ;;
        2) docker-compose logs -f firefox-node ;;
        3) docker-compose logs -f edge-node ;;
        4) docker-compose logs -f chrome-node firefox-node edge-node ;;
        *) echo -e "${RED}Opción no válida${NC}" ;;
    esac
}

run_tests() {
    echo -e "${GREEN}🧪 Ejecutando tests de Selenium...${NC}"
    if ! curl -s http://localhost:4444/wd/hub/status > /dev/null; then
        echo -e "${RED}❌ El Grid no está disponible. Iniciando...${NC}"
        start_grid
        sleep 5
    fi
    echo -e "${YELLOW}⏳ Compilando proyecto...${NC}"
    if ! mvn clean compile; then
        echo -e "${RED}❌ Falló la compilación.${NC}"
        return
    fi
    echo -e "${YELLOW}⏳ Ejecutando tests...${NC}"
    if ! mvn test; then
        echo -e "${RED}❌ Fallaron los tests.${NC}"
        return
    fi
    echo -e "${GREEN}✅ Tests completados. Revisa los resultados arriba.${NC}"
}

stop_grid() {
    echo -e "${YELLOW}🛑 Parando Selenium Grid...${NC}"
    if ! docker-compose down; then
        echo -e "${RED}❌ Error al detener el Grid.${NC}"
        return
    fi
    echo -e "${GREEN}✅ Grid detenido.${NC}"
}

clean_containers() {
    echo -e "${YELLOW}🧹 Limpiando contenedores y volúmenes...${NC}"
    docker-compose down -v --remove-orphans
    docker system prune -f
    echo -e "${GREEN}✅ Limpieza completada.${NC}"
}

open_console() {
    echo -e "${BLUE}🌐 Abriendo Grid Console...${NC}"
    if command -v xdg-open > /dev/null; then
        xdg-open http://localhost:4444
    elif command -v open > /dev/null; then
        open http://localhost:4444
    else
        echo "Abrir manualmente: http://localhost:4444"
    fi
}

check_connectivity() {
    echo -e "${BLUE}🔍 Verificando conectividad...${NC}\n"
    echo -e "${YELLOW}📡 Puertos:${NC}"
    for port in 4444 4442 4443 5900 5901; do
        status=$(nc -z localhost $port && echo 'OK' || echo 'FAIL')
        echo "Puerto $port: $status"
    done
    echo ""
    echo -e "${YELLOW}🔍 Grid API:${NC}"
    curl -s http://localhost:4444/wd/hub/status | grep -q "ready" && echo "Grid: OK" || echo "Grid: FAIL"
    echo ""
    echo -e "${YELLOW}📋 Nodos registrados:${NC}"
    curl -s http://localhost:4444/wd/hub/status | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    nodes = data.get('value', {}).get('nodes', [])
    for node in nodes:
        print(f\"Node: {node.get('id', 'Unknown')} - Status: {node.get('availability', 'Unknown')}\")
except:
    print('No se pudo obtener información de nodos')
"
}

main() {
    check_dependencies
    while true; do
        show_menu
        read -p "Seleccione una opción: " choice
        case $choice in
            1) start_grid ;;
            2) check_grid_status ;;
            3) view_hub_logs ;;
            4) view_node_logs ;;
            5) run_tests ;;
            6) stop_grid ;;
            7) clean_containers ;;
            8) open_console ;;
            9) check_connectivity ;;
            0) echo -e "${GREEN}👋 ¡Hasta luego!${NC}"; exit 0 ;;
            *) echo -e "${RED}❌ Opción no válida${NC}" ;;
        esac
        echo ""
        read -p "Presione Enter para continuar..."
        safe_clear
    done
}

main