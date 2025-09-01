#!/bin/bash

echo "================================================"
echo "  Shiftly - Sistema de Controle de Ponto"
echo "  Inicializando aplicacao..."
echo "================================================"

# Verifica se Java esta instalado
if ! command -v java &> /dev/null; then
    echo "ERRO: Java nao encontrado. Instale Java 17 ou superior."
    exit 1
fi

# Verifica se Maven esta instalado
if ! command -v mvn &> /dev/null; then
    echo "ERRO: Maven nao encontrado. Instale Maven 3.6 ou superior."
    exit 1
fi

# Compila o projeto
echo "Compilando projeto..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "ERRO: Falha na compilacao"
    exit 1
fi

# Executa a aplicacao
echo "Iniciando Shiftly..."
mvn javafx:run
