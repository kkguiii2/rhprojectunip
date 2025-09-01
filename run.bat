@echo off
echo ================================================
echo   Shiftly - Sistema de Controle de Ponto
echo   Inicializando aplicacao...
echo ================================================

REM Verifica se Java esta instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: Java nao encontrado. Instale Java 17 ou superior.
    pause
    exit /b 1
)

REM Verifica se Maven esta instalado
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: Maven nao encontrado. Instale Maven 3.6 ou superior.
    pause
    exit /b 1
)

REM Compila o projeto
echo Compilando projeto...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo ERRO: Falha na compilacao
    pause
    exit /b 1
)

REM Executa a aplicacao
echo Iniciando Shiftly...
call mvn javafx:run

pause
