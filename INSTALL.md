# Guia de Instalação - Shiftly

Este guia detalha todos os passos necessários para instalar e configurar o sistema Shiftly.

## 📋 Pré-requisitos

### 1. Java Development Kit (JDK) 17 ou superior

#### Windows:
1. Baixe o JDK 17 do site oficial da Oracle ou OpenJDK
2. Execute o instalador e siga as instruções
3. Configure a variável JAVA_HOME:
   - Abra "Variáveis de Ambiente"
   - Adicione JAVA_HOME apontando para o diretório do JDK
   - Adicione %JAVA_HOME%\bin ao PATH

#### Linux/Mac:
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# macOS (usando Homebrew)
brew install openjdk@17
```

### 2. Apache Maven 3.6 ou superior

#### Windows:
1. Baixe o Maven do site oficial
2. Extraia em um diretório (ex: C:\maven)
3. Adicione o diretório bin do Maven ao PATH

#### Linux/Mac:
```bash
# Ubuntu/Debian
sudo apt install maven

# macOS (usando Homebrew)
brew install maven
```

### 3. Verificação da Instalação
```bash
java -version
mvn -version
```

## 🗄️ Configuração do SQL Server (Opcional)

O sistema funciona sem SQL Server (usando H2), mas para ambiente de produção, recomenda-se SQL Server.

### Opção 1: SQL Server Express (Gratuito)
1. Baixe SQL Server Express do site da Microsoft
2. Execute o instalador
3. Configure com autenticação mista
4. Anote a instância criada (geralmente SQLEXPRESS)

### Opção 2: SQL Server Developer (Gratuito)
1. Baixe SQL Server Developer Edition
2. Execute instalação completa
3. Configure usuário SA com senha forte

### Opção 3: Docker SQL Server
```bash
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=ShiftlyPass123!" \
   -p 1433:1433 --name sqlserver \
   -d mcr.microsoft.com/mssql/server:2022-latest
```

### Configuração do Usuário
Execute no SQL Server Management Studio:
```sql
-- Criar banco (opcional, sistema cria automaticamente)
CREATE DATABASE ShiftlyDB;

-- Configurar usuário SA (se necessário)
ALTER LOGIN sa ENABLE;
ALTER LOGIN sa WITH PASSWORD = 'ShiftlyPass123!';
```

## 🚀 Instalação do Shiftly

### 1. Clone ou Baixe o Projeto
```bash
git clone [url-do-repositorio]
cd shiftly
```

### 2. Configure o Banco (se necessário)
Edite `src/main/resources/application.properties`:
```properties
# Configurações do SQL Server
database.sqlserver.host=localhost
database.sqlserver.port=1433
database.sqlserver.username=sa
database.sqlserver.password=SuaSenhaAqui
```

### 3. Compile o Projeto
```bash
mvn clean compile
```

### 4. Execute a Aplicação

#### Opção A: Execução Direta
```bash
mvn javafx:run
```

#### Opção B: Scripts Prontos
```bash
# Windows
run.bat

# Linux/Mac
./run.sh
```

#### Opção C: JAR Executável
```bash
mvn clean package
java -jar target/shiftly-system-1.0.0.jar
```

## 🔧 Configurações Avançadas

### Configuração de Memória
Para sistemas com muitos usuários:
```bash
java -Xmx2g -Xms1g -jar shiftly-system-1.0.0.jar
```

### Configuração de Logs
Edite `src/main/resources/logback.xml` para ajustar níveis de log.

### Configuração de Rede
Se o SQL Server estiver em outra máquina:
```properties
database.sqlserver.host=192.168.1.100
database.sqlserver.port=1433
```

## 🛠️ Solução de Problemas

### Erro: "Java não encontrado"
```bash
# Verifique instalação
java -version

# Se não funcionar, reinstale o JDK e configure PATH
```

### Erro: "Maven não encontrado"
```bash
# Verifique instalação
mvn -version

# Se não funcionar, baixe e configure PATH
```

### Erro: "Conexão com SQL Server falhou"
1. Verifique se SQL Server está rodando
2. Confirme credenciais em application.properties
3. Teste conectividade:
```bash
telnet localhost 1433
```

### Erro: "Falha ao carregar FXML"
```bash
# Recompile o projeto
mvn clean compile

# Verifique se resources estão no classpath
mvn clean package
```

### Erro: "Porta já está em uso"
1. Verifique se outra instância está rodando
2. Mude a porta do SQL Server se necessário

## 📦 Distribuição

### Criar Executável Standalone
```bash
mvn clean package
# JAR estará em target/shiftly-system-1.0.0.jar
```

### Criar Instalador (Avançado)
Use jpackage (Java 17+):
```bash
jpackage --input target \
         --name Shiftly \
         --main-jar shiftly-system-1.0.0.jar \
         --main-class com.shiftly.app.ShiftlyLauncher \
         --type msi
```

## 🔄 Atualização

### Atualizar Aplicação
1. Faça backup do banco de dados
2. Baixe nova versão
3. Execute novamente

### Migração de Dados
O sistema mantém compatibilidade automática entre versões.

## 📞 Suporte

### Logs do Sistema
Localização: `logs/shiftly.log`

### Problemas Comuns
1. **Tela preta**: Verifique drivers gráficos
2. **Lentidão**: Aumente memória JVM
3. **Erro de conexão**: Verifique configurações de rede

### Informações para Suporte
Inclua sempre:
- Versão do Java (`java -version`)
- Versão do Maven (`mvn -version`)
- Sistema operacional
- Logs do erro (`logs/shiftly.log`)
- Configurações utilizadas (sem senhas)

## 🏢 Implantação Corporativa

### Requisitos Mínimos
- **CPU**: Dual-core 2.0 GHz
- **RAM**: 4 GB (8 GB recomendado)
- **Disco**: 1 GB livre
- **Rede**: 100 Mbps para SQL Server remoto

### Configuração Multi-usuário
Para múltiplos usuários simultaneamente:
1. Use SQL Server dedicado
2. Configure balanceamento de carga se necessário
3. Monitore performance regularmente

### Backup e Segurança
1. Configure backup automático do SQL Server
2. Mantenha logs por tempo adequado
3. Configure firewall apropriadamente
4. Use conexões SSL em produção

---

**Shiftly v1.0.0** - Sistema de Controle de Ponto Eletrônico  
Para dúvidas, consulte a documentação completa no README.md
