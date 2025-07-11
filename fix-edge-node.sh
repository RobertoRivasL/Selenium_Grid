#!/bin/bash

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}===============================================${NC}"
echo -e "${BLUE}        REPARANDO NODO EDGE${NC}"
echo -e "${BLUE}===============================================${NC}"
echo ""

# 1. Parar y limpiar Edge específicamente
echo -e "${YELLOW}🛑 1. Parando nodo Edge...${NC}"
docker-compose stop edge-node
docker-compose rm -f edge-node
echo ""

# 2. Verificar y descargar imagen Edge
echo -e "${YELLOW}📦 2. Verificando imagen Edge...${NC}"
echo "Descargando/actualizando imagen selenium/node-edge:4.15.0..."
docker pull selenium/node-edge:4.15.0
echo ""

# 3. Limpiar redes y volúmenes
echo -e "${YELLOW}🧹 3. Limpiando redes...${NC}"
docker network prune -f
echo ""

# 4. Reiniciar solo Edge
echo -e "${YELLOW}🚀 4. Iniciando nodo Edge...${NC}"
docker-compose up -d edge-node
echo ""

# 5. Esperar que Edge se registre
echo -e "${YELLOW}⏳ 5. Esperando que Edge se registre (30 segundos)...${NC}"
for i in {1..30}; do
    echo -n "."
    sleep 1
done
echo ""
echo ""

# 6. Verificar registro
echo -e "${YELLOW}🔍 6. Verificando registro de Edge...${NC}"
sleep 5

curl -s http://localhost:4444/status | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    nodes = data.get('value', {}).get('nodes', [])
    edge_found = False

    for node in nodes:
        slots = node.get('slots', [])
        for slot in slots:
            browser = slot.get('stereotype', {}).get('browserName', '')
            if browser.lower() in ['msedge', 'microsoftedge', 'edge']:
                edge_found = True
                print(f'✅ Edge encontrado: {browser}')
                break

    if not edge_found:
        print('❌ Edge NO encontrado en el Grid')
        print('Nodos disponibles:')
        for node in nodes:
            slots = node.get('slots', [])
            browsers = set()
            for slot in slots:
                browser = slot.get('stereotype', {}).get('browserName', 'Unknown')
                browsers.add(browser)
            print(f'  Node: {list(browsers)}')
    else:
        print('✅ Edge registrado correctamente en el Grid')

except Exception as e:
    print('❌ Error verificando Grid:', e)
" 2>/dev/null || echo "❌ No se pudo verificar el estado del Grid"

echo ""

# 7. Mostrar logs recientes de Edge
echo -e "${YELLOW}📋 7. Logs recientes de Edge:${NC}"
docker-compose logs --tail=10 edge-node
echo ""

# 8. Verificar conectividad
echo -e "${YELLOW}🌐 8. Verificando conectividad Edge:${NC}"

# Función para verificar puerto sin nc
check_port() {
    local host=$1
    local port=$2
    local timeout=3

    # Método 1: Usar curl con timeout
    if command -v curl > /dev/null; then
        if curl --connect-timeout $timeout --silent --show-error "http://$host:$port" > /dev/null 2>&1; then
            echo "✅ OK"
        else
            echo "❌ FAIL"
        fi
    # Método 2: Usar PowerShell en Windows
    elif command -v powershell.exe > /dev/null; then
        if powershell.exe -Command "Test-NetConnection -ComputerName $host -Port $port -InformationLevel Quiet" 2>/dev/null | grep -q "True"; then
            echo "✅ OK"
        else
            echo "❌ FAIL"
        fi
    # Método 3: Usar Python como fallback
    elif command -v python3 > /dev/null; then
        if python3 -c "
import socket
import sys
try:
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.settimeout($timeout)
    result = sock.connect_ex(('$host', $port))
    sock.close()
    sys.exit(0 if result == 0 else 1)
except:
    sys.exit(1)
" 2>/dev/null; then
            echo "✅ OK"
        else
            echo "❌ FAIL"
        fi
    else
        echo "⚠️ SKIP (sin herramientas de red)"
    fi
}

echo -n "Edge VNC (5902): "
check_port "localhost" "5902"

echo -n "Edge noVNC (7902): "
check_port "localhost" "7902"

echo ""

echo -e "${GREEN}✅ Proceso de reparación completado${NC}"
echo -e "${BLUE}💡 Si Edge sigue fallando, ejecuta: docker-compose logs edge-node${NC}"
echo -e "${BLUE}===============================================${NC}"