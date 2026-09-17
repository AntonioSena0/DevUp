# DevUp

Manager local de bancos de dados via Docker, com interface gráfica em Java.
Suba PostgreSQL e MySQL com um clique, acompanhe o status e copie a string de conexão pronta.

## Requisitos

- Docker Desktop instalado e rodando
- Para rodar o instalador (.msi): só Windows 64-bit, nada mais
- Para rodar o .jar: Java 21+
- Para compilar: JDK 21+ e Maven 3.9+

## Como rodar

### Instalador

1. Baixe o `DevUp-0.1.0.msi` da aba Releases
2. Instale (instalação por usuário, sem admin) e abra o DevUp pelo Menu Iniciar
3. Clique em **Ligar** no banco que quiser usar

## Credenciais (fixas, ambiente local)

| Banco      | Host      | Porta | Usuário | Senha              | Database       |
|------------|-----------|-------|---------|--------------------|----------------|
| PostgreSQL | localhost | 5432  | root    | devup_database_pass | devup_database |
| MySQL      | localhost | 3306  | root    | devup_database_pass | devup_database |

JDBC pronta (botão **Copiar URL** na interface):

```
jdbc:postgresql://localhost:5432/devup_database
jdbc:mysql://localhost:3306/devup_database?createDatabaseIfNotExist=true&serverTimezone=UTC
```

Se a porta padrão estiver ocupada, o app usa a próxima livre sozinho.
Containers antigos com credencial diferente são recriados automaticamente no próximo Ligar.

## Interface

- **Ligar / Parar** — sobe ou para o banco (rede, volume e senha resolvidos sozinhos)
- **Ligar tudo / Desligar tudo** — opera os dois bancos de uma vez
- **Testar** — executa `SELECT 1` de verdade via JDBC
- **Ver logs** — últimas 80 linhas do container
- **Copiar URL** — copia a JDBC para a área de transferência
- **Apagar tudo** — apaga container e volume (pede confirmação, apaga os dados)
- Indicador **DOCKER ON/OFF** — se o Docker Desktop estiver fechado, aparece um aviso com botão para abri-lo

## Testes

```bat
mvn test
```

14 testes JUnit (unitários + integração com Docker, pulados sozinhos se o Docker estiver off).

## Segurança

- Containers só escutam em `127.0.0.1` (não expõem na rede local)
- A credencial fixa é proposital e serve só para desenvolvimento local — nunca use em produção ou host compartilhado
- Senhas e portas gerenciadas ficam em `~/.devup/`, fora do repositório
- `target/`, `installer/` e `.idea/` estão no `.gitignore` e não devem ser commitados

## Estrutura

```
src/main/java/org/devup/
  Main.java        abre a interface
  ui/              DevUpGui, DbCard (cartão padrão), UiStyle (tema)
  core/            DockerRunner, DockerManager, DockerNetwork, ConfigStore, PortUtils
  db/              DbPreset (bancos suportados)
src/main/resources/  logo oficial + ícones
src/test/          testes JUnit
package-exe.bat    builda o .jar, gera e instala o .msi, abre o app
```
