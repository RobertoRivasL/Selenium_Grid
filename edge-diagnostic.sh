#!/bin/bash

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}===============================================${NC}"
echo -e "${BLUE}        DIAGNÓSTICO DEL NODO EDGE${NC}"
echo -e "${BLUE}===============================================${NC}"
echo ""

# 1. Verificar contenedores
echo -e "${YELLOW}📦 1. Estado de contenedores:${NC}"
docker-compose ps
echo ""

# 2. Verificar logs de Edge
echo -e "${YELLOW}📋 2. Logs del nodo Edge (últimas 20 líneas):${NC}"
docker-compose logs --tail=20 edge-node
echo ""

# 3. Verificar Grid status
echo -e "${YELLOW}🔍 3. Estado del Grid y nodos registrados:${NC}"
curl -s http://localhost:4444/wd/hub/status | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print('Grid Status:', data.get('value', {}).get('ready', 'Unknown'))
    nodes = data.get('value', {}).get('nodes', [])
    print(f'Total nodes: {len(nodes)}')
    for i, node in enumerate(nodes):
        node_id = node.get('id', 'Unknown')[:8] + '...'
        availability = node.get('availability', 'Unknown')
        print(f'  Node {i+1}: {node_id} - {availability}')

        # Ver slots por navegador
        slots = node.get('slots', [])
        browsers = {}
        for slot in slots:
            browser = slot.get('stereotype', {}).get('browserName', 'Unknown')
            if browser in browsers:
                browsers[browser] += 1
            else:
                browsers[browser] = 1

        for browser, count in browsers.items():
            print(f'    {browser}: {count} slots')
except Exception as e:
    print('Error parsing Grid status:', e)
" 2>/dev/null || echo "Grid no disponible"
echo ""

# 4. Verificar conectividad específica de Edge
echo -e "${YELLOW}🌐 4. Verificando conectividad Edge:${NC}"
echo "Edge VNC (5902): $(nc -z localhost 5902 && echo 'OK' || echo 'FAIL')"
echo "Edge noVNC (7902): $(nc -z localhost 7902 && echo 'OK' || echo 'FAIL')"
echo ""

# 5. Verificar imagen Edge
echo -e "${YELLOW}🐳 5. Imagen de Edge disponible:${NC}"
docker images | grep selenium/node-edge || echo "Imagen Edge no encontrada"
echo ""

# 6. Verificar si Edge está en el Grid
echo -e "${YELLOW}🔍 6. Verificando capacidades de Edge en el Grid:${NC}"
curl -s http://localhost:4444/wd/hub/status | grep -i edge && echo "Edge encontrado" || echo "Edge NO encontrado en el Grid"
echo ""

# 7. Intentar crear sesión Edge directamente
echo -e "${YELLOW}🧪 7. Intentando crear sesión Edge directamente:${NC}"
curl -X POST http://localhost:4444/wd/hub/session \
  -H "Content-Type: application/json" \
  -d '{
    "capabilities": {
      "alwaysMatch": {
        "browserName": "MicrosoftEdge"
      }
    }
  }' | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    if 'sessionId' in data.get('value', {}):
        print('✅ Sesión Edge creada exitosamente')
        session_id = data['value']['sessionId']
        print(f'Session ID: {session_id}')
        # Cerrar sesión
        import subprocess
        subprocess.run(['curl', '-X', 'DELETE', f'http://localhost:4444/wd/hub/session/{session_id}'],
                      capture_output=True)
        print('✅ Sesión cerrada')
    else:
        print('❌ Error creando sesión Edge')
        print('Response:', json.dumps(data, indent=2))
except Exception as e:
    print('❌ Error parsing response:', e)
    print('Raw response:', sys.stdin.read())
" 2>/dev/null
echo ""

echo -e "${BLUE}===============================================${NC}"
echo -e "${BLUE}           DIAGNÓSTICO COMPLETADO${NC}"
echo -e "${BLUE}===============================================${NC}"