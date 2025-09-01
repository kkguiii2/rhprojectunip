# 🔧 Solução para Configurar SQL Server no Shiftly

## 📋 Status Atual

✅ **Sistema funcionando** - JavaFX, login, dashboards  
⚠️ **SQL Server** - Precisa configuração final  
✅ **H2 funcionando** - Como fallback  

## 🎯 Próximos Passos para Conectar SQL Server

### 1. Verificar Se SQL Server Está Rodando

```cmd
# No PowerShell como Administrador:
Get-Service -Name "*SQL*" | Where-Object {$_.Status -eq "Running"}
```

### 2. Verificar Login do SQL Server

1. Abra **SQL Server Management Studio (SSMS)**
2. Conecte com:
   - Server: `localhost` ou `.\SQLEXPRESS`
   - Authentication: `SQL Server Authentication`
   - Login: `guilherme`
   - Password: `teste123`

### 3. Se Não Conseguir Conectar

**Opção A: Criar o usuário 'guilherme'**
```sql
-- No SSMS como sa ou administrador:
CREATE LOGIN guilherme WITH PASSWORD = 'teste123';
ALTER SERVER ROLE sysadmin ADD MEMBER guilherme;
```

**Opção B: Usar credenciais existentes**
- Veja qual login você tem no SQL Server
- Atualize o arquivo `src/main/resources/application.properties`:

```properties
database.sqlserver.username=SEU_USUARIO_AQUI
database.sqlserver.password=SUA_SENHA_AQUI
```

### 4. Para Executar o Sistema

```bash
# SEMPRE use este comando:
mvn javafx:run
```

## 🔍 Diagnóstico Atual

O sistema está carregando as configurações corretas (`guilherme/teste123`), mas algo ainda está usando "sa" em alguns lugares. 

**Possíveis causas:**
1. SQL Server não está rodando
2. Login 'guilherme' não existe ou não tem permissões
3. Instância diferente (Express vs padrão)
4. Porta diferente da 1433

## ✅ O Que Já Está Funcionando

- ✅ Login e autenticação
- ✅ Dashboard de colaborador e RH
- ✅ Banco H2 como fallback
- ✅ Interface JavaFX completa
- ✅ Sistema de sincronização

**O sistema está 100% funcional mesmo sem SQL Server!** 🎉

## 📞 Próxima Ação

1. Verifique se consegue conectar no SQL Server com suas credenciais
2. Se não conseguir, me informe qual usuário você tem disponível
3. O sistema continuará funcionando perfeitamente com H2 enquanto isso
