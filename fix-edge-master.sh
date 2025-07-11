#!/bin/bash

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${BLUE}=================================================${NC}"
echo -e "${BLUE}     REPARADOR MAESTRO DE EDGE${NC}"
echo -e "${BLUE}=================================================${NC}"
echo ""

echo -e "${RED}⚠️  PROBLEMA DETECTADO: Edge no se puede conectar${NC}"
echo -e "${YELLOW}✅ Chrome y Firefox funcionan correctamente${NC}"
echo ""
echo -e "${PURPLE}🔧 Selecciona una solución:${NC}"
echo ""
echo -e "${YELLOW}1.${NC} Diagnóstico completo (recomendado primero)"
echo -e "${YELLOW}2.${NC} Reparar nodo Edge (reiniciar y reconfigurar)"
echo -e "${YELLOW}3.${NC} Usar configuración alternativa de Edge"
echo -e "${YELLOW}4.${NC} Continuar SIN Edge (solo Chrome + Firefox)"
echo -e "${YELLOW}5.${NC} Probar Edge en modo debug"
echo -e "${YELLOW}6.${NC} Reiniciar Grid completo"
echo -e "${YELLOW}7.${NC} Ver logs detallados de Edge"
echo ""
echo -n "Elige una opción (1-7): "
read -n 1 opcion
echo ""
echo ""

case $opcion in
    1)
        echo -e "${GREEN}🔍 Ejecutando diagnóstico completo...${NC}"
        ./edge-diagnostic.sh
        ;;
    2)
        echo -e "${GREEN}🛠️  Reparando nodo Edge...${NC}"
        ./fix-edge-node.sh
        echo ""
        echo -e "${BLUE}🧪 ¿Quieres probar Edge ahora? (y/n)${NC}"
        read -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            mvn test -Dbrowser=edge -Dtest=BasicGridConnectionTest
        fi
        ;;
    3)
        echo -e "${GREEN}🔄 Usando configuración alternativa de Edge...${NC}"
        echo "Parando Grid actual..."
        docker-compose down
        echo "Iniciando con configuración alternativa..."
        docker-compose -f docker-compose-edge-alternative.yml up -d
        echo "Esperando que el Grid se inicie..."
        sleep 30
        echo -e "${BLUE}🧪 ¿Quieres probar Edge alternativo? (y/n)${NC}"
        read -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            mvn test -Dbrowser=edge -Dtest=BasicGridConnectionTest
        fi
        ;;
    4)
        echo -e "${GREEN}🚀 Continuando sin Edge (Chrome + Firefox)...${NC}"
        echo "Parando Grid actual..."
        docker-compose down
        echo "Iniciando Grid sin Edge..."
        docker-compose -f docker-compose-no-edge.yml up -d
        echo "Esperando que el Grid se inicie..."
        sleep 15
        echo -e "${BLUE}🧪 ¿Quieres ejecutar tests sin Edge? (y/n)${NC}"
        read -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            mvn test -DsuiteXmlFile=src/test/resources/testng-no-edge.xml
        fi
        ;;
    5)
        echo -e "${GREEN}🐛 Probando Edge en modo debug...${NC}"
        echo "Creando sesión Edge directamente..."
        curl -X POST http://localhost:4444/wd/hub/session \
          -H "Content-Type: application/json" \
          -d '{
            "capabilities": {
              "alwaysMatch": {
                "browserName": "MicrosoftEdge",
                "platformName": "linux"
              }
            }
          }' | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    if 'sessionId' in data.get('value', {}):
        session_id = data['value']['sessionId']
        print(f'✅ Sesión Edge creada: {session_id}')
        print('🌐 Abre VNC: vnc://localhost:5902')
        print('🌐 O noVNC: http://localhost:7902')
        input('Presiona Enter para cerrar la sesión...')
        import subprocess
        subprocess.run(['curl', '-X', 'DELETE', f'http://localhost:4444/wd/hub/session/{session_id}'])
        print('✅ Sesión cerrada')
    else:
        print('❌ Error creando sesión Edge')
        print(json.dumps(data, indent=2))
except Exception as e:
    print('❌ Error:', e)
"
        ;;
    6)
        echo -e "${GREEN}🔄 Reiniciando Grid completo...${NC}"
        docker-compose down -v
        docker system prune -f
        echo "Descargando imágenes actualizadas..."
        docker-compose pull
        echo "Iniciando Grid..."
        docker-compose up -d
        echo "Esperando que el Grid se inicie..."
        sleep 45
        echo -e "${BLUE}Estado del Grid:${NC}"
        curl -s http://localhost:4444/wd/hub/status | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    nodes = data.get('value', {}).get('nodes', [])
    print(f'Nodos registrados: {len(nodes)}')
    for node in nodes:
        slots = node.get('slots', [])
        browsers = set(slot.get('stereotype', {}).get('browserName', 'Unknown') for slot in slots)
        print(f'  Navegadores: {list(browsers)}')
except:
    print('Grid no disponible')
"
        ;;
    7)
        echo -e "${GREEN}📋 Logs detallados de Edge...${NC}"
        echo "=== LOGS COMPLETOS DE EDGE ==="
        docker-compose logs edge-node
        echo ""
        echo "=== LOGS DEL HUB (relacionados con Edge) ==="
        docker-compose logs selenium-hub | grep -i edge
        ;;
    *)
        echo -e "${RED}❌ Opción no válida${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${BLUE}=================================================${NC}"
echo -e "${BLUE}           PROCESO COMPLETADO${NC}"
echo -e "${BLUE}=================================================${NC}"
echo ""
echo -e "${YELLOW}💡 Próximos pasos recomendados:${NC}"
echo -e "   1. Si Edge funciona: mvn test -Dbrowser=edge"
echo -e "   2. Si sigue fallando: usar opción 4 (sin Edge)"
echo -e "   3. Para tests completos: mvn test -DsuiteXmlFile=src/test/resources/testng-no-edge.xml"