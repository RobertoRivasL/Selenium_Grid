#!/bin/bash

# ================================
# OWASP SUPPRESSIONS VALIDATOR
# ================================

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SUPPRESSIONS_FILE="owasp-suppressions.xml"
SCHEMA_URL="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd"

echo -e "${BLUE}🛡️  OWASP Suppressions Validator${NC}"
echo "================================"

# Función para verificar si existe el archivo
check_file_exists() {
    if [ ! -f "$SUPPRESSIONS_FILE" ]; then
        echo -e "${RED}❌ Archivo no encontrado: $SUPPRESSIONS_FILE${NC}"
        echo "Creando archivo de ejemplo..."
        create_example_file
        return 1
    fi
    echo -e "${GREEN}✅ Archivo encontrado: $SUPPRESSIONS_FILE${NC}"
    return 0
}

# Función para crear archivo de ejemplo
create_example_file() {
    cat > "$SUPPRESSIONS_FILE" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<suppressions>
    <!-- Archivo de suppressions de ejemplo -->
    <!-- Documentación: https://dependency-check.github.io/DependencyCheck/general/suppression.html -->

    <!--
    <suppress>
        <notes><![CDATA[
        Ejemplo de suppression
        Fecha: 2025-07-07
        Autor: DevOps Team
        ]]></notes>
        <packageUrl>pkg:maven/org.example/example@1.0.0</packageUrl>
        <cve>CVE-2023-XXXXX</cve>
    </suppress>
    -->
</suppressions>
EOF
    echo -e "${GREEN}✅ Archivo de ejemplo creado${NC}"
}

# Función para validar XML básico
validate_xml_syntax() {
    echo -e "${BLUE}🔍 Validando sintaxis XML...${NC}"

    if command -v xmllint >/dev/null 2>&1; then
        if xmllint --noout "$SUPPRESSIONS_FILE" 2>/dev/null; then
            echo -e "${GREEN}✅ Sintaxis XML válida${NC}"
            return 0
        else
            echo -e "${RED}❌ Error de sintaxis XML${NC}"
            echo "Ejecuta: xmllint --noout $SUPPRESSIONS_FILE"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠️  xmllint no disponible, saltando validación de sintaxis${NC}"
        return 0
    fi
}

# Función para descargar schema
download_schema() {
    echo -e "${BLUE}📥 Descargando schema OWASP...${NC}"

    if command -v curl >/dev/null 2>&1; then
        if curl -s -f "$SCHEMA_URL" -o "dependency-suppression.xsd"; then
            echo -e "${GREEN}✅ Schema descargado${NC}"
            return 0
        else
            echo -e "${YELLOW}⚠️  No se pudo descargar el schema${NC}"
            return 1
        fi
    elif command -v wget >/dev/null 2>&1; then
        if wget -q "$SCHEMA_URL" -O "dependency-suppression.xsd"; then
            echo -e "${GREEN}✅ Schema descargado${NC}"
            return 0
        else
            echo -e "${YELLOW}⚠️  No se pudo descargar el schema${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠️  curl/wget no disponible${NC}"
        return 1
    fi
}

# Función para validar contra schema
validate_against_schema() {
    echo -e "${BLUE}🔍 Validando contra schema OWASP...${NC}"

    if [ ! -f "dependency-suppression.xsd" ]; then
        if ! download_schema; then
            echo -e "${YELLOW}⚠️  Schema no disponible, saltando validación${NC}"
            return 0
        fi
    fi

    if command -v xmllint >/dev/null 2>&1; then
        if xmllint --noout --schema "dependency-suppression.xsd" "$SUPPRESSIONS_FILE" 2>/dev/null; then
            echo -e "${GREEN}✅ Validación de schema exitosa${NC}"
            return 0
        else
            echo -e "${RED}❌ Error de validación de schema${NC}"
            echo "Ejecuta: xmllint --schema dependency-suppression.xsd $SUPPRESSIONS_FILE"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠️  xmllint no disponible${NC}"
        return 0
    fi
}

# Función para testear con Maven
test_with_maven() {
    echo -e "${BLUE}🧪 Testeando suppressions con Maven...${NC}"

    if [ ! -f "pom.xml" ]; then
        echo -e "${YELLOW}⚠️  pom.xml no encontrado, saltando test de Maven${NC}"
        return 0
    fi

    echo "Ejecutando: mvn org.owasp:dependency-check-maven:check"
    if mvn org.owasp:dependency-check-maven:check -q; then
        echo -e "${GREEN}✅ Test de Maven exitoso${NC}"
        return 0
    else
        echo -e "${RED}❌ Test de Maven falló${NC}"
        echo "Revisa los logs de Maven para más detalles"
        return 1
    fi
}

# Función para verificar configuración en pom.xml
check_pom_configuration() {
    echo -e "${BLUE}🔍 Verificando configuración en pom.xml...${NC}"

    if [ ! -f "pom.xml" ]; then
        echo -e "${YELLOW}⚠️  pom.xml no encontrado${NC}"
        return 0
    fi

    if grep -q "dependency-check-maven" pom.xml; then
        echo -e "${GREEN}✅ Plugin OWASP encontrado en pom.xml${NC}"
    else
        echo -e "${RED}❌ Plugin OWASP NO encontrado en pom.xml${NC}"
        echo "Agrega el plugin OWASP Dependency Check al pom.xml"
        return 1
    fi

    if grep -q "suppressionFiles" pom.xml; then
        echo -e "${GREEN}✅ Configuración de suppressions encontrada${NC}"
    else
        echo -e "${YELLOW}⚠️  Configuración de suppressions no encontrada${NC}"
        echo "Considera agregar <suppressionFiles> en la configuración del plugin"
    fi

    return 0
}

# Función para mostrar estadísticas del archivo
show_statistics() {
    echo -e "${BLUE}📊 Estadísticas del archivo:${NC}"

    if [ -f "$SUPPRESSIONS_FILE" ]; then
        total_lines=$(wc -l < "$SUPPRESSIONS_FILE")
        suppress_blocks=$(grep -c "<suppress>" "$SUPPRESSIONS_FILE" 2>/dev/null || echo "0")
        comments=$(grep -c "<!--" "$SUPPRESSIONS_FILE" 2>/dev/null || echo "0")

        echo "📄 Total de líneas: $total_lines"
        echo "🚫 Bloques de suppression: $suppress_blocks"
        echo "💬 Comentarios: $comments"

        if [ "$suppress_blocks" -gt 0 ]; then
            echo -e "${GREEN}✅ Archivo contiene suppressions activas${NC}"
        else
            echo -e "${YELLOW}ℹ️  No hay suppressions activas (solo template)${NC}"
        fi
    fi
}

# Función para limpiar archivos temporales
cleanup() {
    if [ -f "dependency-suppression.xsd" ]; then
        rm -f "dependency-suppression.xsd"
        echo -e "${BLUE}🧹 Archivos temporales limpiados${NC}"
    fi
}

# Función para mostrar ayuda
show_help() {
    echo "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  -h, --help     Mostrar esta ayuda"
    echo "  -f, --file     Especificar archivo de suppressions (default: owasp-suppressions.xml)"
    echo "  -t, --test     Solo ejecutar test con Maven"
    echo "  -c, --clean    Limpiar archivos temporales"
    echo "  -s, --stats    Mostrar solo estadísticas"
    echo ""
    echo "Ejemplos:"
    echo "  $0                           # Validación completa"
    echo "  $0 -f my-suppressions.xml   # Validar archivo específico"
    echo "  $0 -t                        # Solo test con Maven"
}

# Función principal
main() {
    local test_only=false
    local stats_only=false

    # Procesar argumentos
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -f|--file)
                SUPPRESSIONS_FILE="$2"
                shift 2
                ;;
            -t|--test)
                test_only=true
                shift
                ;;
            -c|--clean)
                cleanup
                exit 0
                ;;
            -s|--stats)
                stats_only=true
                shift
                ;;
            *)
                echo -e "${RED}❌ Opción desconocida: $1${NC}"
                show_help
                exit 1
                ;;
        esac
    done

    echo -e "${BLUE}Archivo objetivo: $SUPPRESSIONS_FILE${NC}"
    echo ""

    if [ "$stats_only" = true ]; then
        show_statistics
        exit 0
    fi

    if [ "$test_only" = true ]; then
        test_with_maven
        exit $?
    fi

    # Validación completa
    local success=true

    if ! check_file_exists; then
        success=false
    fi

    if ! validate_xml_syntax; then
        success=false
    fi

    if ! validate_against_schema; then
        success=false
    fi

    if ! check_pom_configuration; then
        success=false
    fi

    show_statistics

    echo ""
    echo -e "${BLUE}🧪 Ejecutando test final con Maven...${NC}"
    if ! test_with_maven; then
        success=false
    fi

    echo ""
    if [ "$success" = true ]; then
        echo -e "${GREEN}🎉 ¡Validación completa exitosa!${NC}"
        echo -e "${GREEN}✅ El archivo de suppressions está listo para usar${NC}"
    else
        echo -e "${RED}❌ Validación falló${NC}"
        echo -e "${YELLOW}Revisa los errores anteriores y corrige el archivo${NC}"
    fi

    cleanup

    if [ "$success" = true ]; then
        exit 0
    else
        exit 1
    fi
}

# Verificar prerequisitos
if ! command -v mvn >/dev/null 2>&1; then
    echo -e "${RED}❌ Maven no está instalado${NC}"
    exit 1
fi

# Ejecutar función principal
main "$@"