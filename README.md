# Shiftly - Sistema de Controle de Ponto Eletrônico

Sistema completo de controle de ponto eletrônico desenvolvido em Java com JavaFX, que oferece reconhecimento facial, geolocalização e gestão completa de recursos humanos.

## 🚀 Características Principais

### Para Colaboradores
- ✅ Registro de ponto com reconhecimento facial
- 📍 Registro de ponto com geolocalização
- 📊 Consulta de histórico de batimentos
- ⏰ Consulta de saldo de horas extras
- 💰 Visualização de comprovantes de pagamento
- 🏖️ Solicitação e agendamento de férias

### Para RH (Recursos Humanos)
- 🔐 Acesso administrativo restrito
- ✏️ Correção e ajustes de batimentos de ponto
- ✅ Acompanhamento e aprovação de horas extras
- 🗓️ Gestão de férias dos colaboradores
- 📈 Relatórios de presença e produtividade

## 🛠️ Tecnologias Utilizadas

- **Java 17** - Linguagem principal
- **JavaFX** - Interface gráfica moderna
- **SQL Server** - Banco de dados principal
- **H2 Database** - Banco de dados fallback (em memória)
- **JWT** - Autenticação segura
- **Maven** - Gerenciamento de dependências
- **SLF4J + Logback** - Sistema de logs

## 📋 Pré-requisitos

### Software Necessário
- Java 17 ou superior
- Maven 3.6 ou superior
- SQL Server (opcional - sistema funciona com H2)

### Configuração do SQL Server (Opcional)
```sql
-- Configurações padrão do sistema:
Servidor: localhost:1433
Banco: ShiftlyDB
Usuário: sa
Senha: ShiftlyPass123!
```

## 🏗️ Instalação e Execução

### 1. Clone o Repositório
```bash
git clone [url-do-repositorio]
cd shiftly
```

### 2. Configure o SQL Server (Opcional)
Se você tiver SQL Server disponível, certifique-se de que está rodando e configure um usuário com as credenciais padrão. O sistema criará automaticamente o banco `ShiftlyDB` e todas as tabelas.

### 3. Compile o Projeto
```bash
mvn clean compile
```

### 4. Execute a Aplicação
```bash
mvn javafx:run
```

Ou compile um JAR executável:
```bash
mvn clean package
java -jar target/shiftly-system-1.0.0.jar
```

## 🗃️ Estrutura do Banco de Dados

O sistema cria automaticamente as seguintes tabelas:

### Tabela `usuarios`
- Informações dos colaboradores e usuários RH
- Dados de autenticação e permissões
- Informações de reconhecimento facial

### Tabela `pontos`
- Registros de entrada/saída
- Dados de geolocalização
- Status de validação facial
- Controle de correções manuais

### Tabela `ferias`
- Solicitações de férias
- Status de aprovação/recusa
- Histórico de aprovações

### Tabela `horas_extras`
- Registros de horas extras
- Controle de aprovação e pagamento
- Justificativas e descrições

### Tabela `comprovantes`
- Comprovantes de pagamento
- Detalhamento de valores
- Anexos de arquivos

## 💡 Funcionalidades Especiais

### Banco Dual (SQL Server + H2)
- Sistema verifica automaticamente se SQL Server está disponível
- Se SQL Server estiver offline, usa H2 em memória como fallback
- Sincronização automática quando SQL Server volta online
- Nenhum dado é perdido durante transições

### Autenticação JWT
- Tokens seguros com expiração configurável
- Controle de sessões ativas
- Proteção contra ataques de força bruta

### Interface Responsiva
- Design moderno com CSS customizado
- Cores profissionais (azul, verde, cinza)
- Experiência otimizada para desktop

## 👥 Usuários Padrão

O sistema vem com usuários pré-configurados para teste:

### Administrador
- **Email:** admin@shiftly.com
- **Senha:** admin123
- **Tipo:** ADMIN

### RH
- **Email:** maria.silva@empresa.com
- **Senha:** 123456
- **Tipo:** RH

### Colaboradores
- **Email:** joao.santos@empresa.com
- **Senha:** 123456
- **Tipo:** COLABORADOR

- **Email:** ana.costa@empresa.com
- **Senha:** 123456
- **Tipo:** COLABORADOR

## 🔧 Configurações Avançadas

### Alterando Configurações do Banco
Edite o arquivo `src/main/java/com/shiftly/repository/DatabaseConfig.java`:

```java
private static final String SQL_SERVER_HOST = "localhost";
private static final String SQL_SERVER_PORT = "1433";
private static final String SQL_SERVER_DATABASE = "ShiftlyDB";
private static final String SQL_SERVER_USERNAME = "sa";
private static final String SQL_SERVER_PASSWORD = "SuaSenhaAqui";
```

### Configurando Logs
Edite o arquivo `src/main/resources/logback.xml` para ajustar níveis de log e destinos.

### Personalizando Interface
Edite o arquivo `src/main/resources/css/styles.css` para personalizar cores e estilos.

## 🔄 Workflow de Desenvolvimento

### Estrutura do Projeto
```
Shiftly/
├── src/main/java/com/shiftly/
│   ├── app/           # Aplicação principal
│   ├── controller/    # Controllers JavaFX
│   ├── model/         # Classes de domínio
│   ├── repository/    # Acesso a dados (DAO)
│   ├── service/       # Lógica de negócio
│   └── util/          # Utilitários (JWT, etc.)
├── src/main/resources/
│   ├── css/           # Estilos CSS
│   ├── fxml/          # Layouts JavaFX
│   └── images/        # Recursos visuais
└── pom.xml            # Configuração Maven
```

### Adicionando Novas Funcionalidades
1. **Model:** Crie/modifique classes em `model/`
2. **Repository:** Implemente acesso a dados em `repository/`
3. **Service:** Adicione lógica de negócio em `service/`
4. **Controller:** Crie controllers em `controller/`
5. **FXML:** Desenhe interface em `resources/fxml/`

## 🔮 Roadmap Futuro

### Funcionalidades Planejadas
- [ ] Aplicativo mobile para registro de ponto
- [ ] Integração com APIs de reconhecimento facial
- [ ] Integração com APIs de geolocalização
- [ ] Relatórios avançados com gráficos
- [ ] Notificações push
- [ ] API REST para integrações
- [ ] Dashboard web adicional
- [ ] Integração com sistemas de RH existentes

### Integrações Futuras
- [ ] Sistemas de ponto biométrico
- [ ] ERPs corporativos
- [ ] Sistemas de folha de pagamento
- [ ] Aplicativos de comunicação interna

## 🐛 Solução de Problemas

### Erro: "Não foi possível conectar ao SQL Server"
- Verifique se o SQL Server está rodando
- Confirme as credenciais de acesso
- O sistema funcionará normalmente com H2

### Erro: "Arquivo FXML não encontrado"
- Certifique-se de que o projeto foi compilado corretamente
- Verifique se os recursos estão no classpath

### Performance lenta
- Verifique a conectividade com o banco
- Aumente a memória JVM se necessário: `-Xmx2g`

## 📄 Licença

Este projeto foi desenvolvido como demonstração técnica. Todos os direitos reservados.

## 🤝 Contribuição

Para contribuir com o projeto:
1. Faça um fork do repositório
2. Crie uma branch para sua feature
3. Implemente as mudanças
4. Teste completamente
5. Submeta um pull request

## 📞 Suporte

Para dúvidas e suporte técnico, entre em contato através dos canais oficiais da empresa.

---

**Shiftly v1.0.0** - Sistema de Controle de Ponto Eletrônico  
Desenvolvido com ❤️ usando Java e JavaFX
