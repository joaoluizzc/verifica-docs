# VerificaDocs

Sistema Java com Spring Boot, PostgreSQL e frontend Thymeleaf para upload e analise de documentos.

## Fluxo implementado

1. Usuario acessa a tela de autenticacao.
2. Sistema registra sucesso ou falha em `LogAcesso`.
3. Usuario envia um PDF ou imagem.
4. Backend salva o `Documento`.
5. Backend consulta a API OCR.space.
6. Sistema calcula `scoreSuspeita`, `grauConfianca` e `AnaliseOcorrencia`.
7. Usuario visualiza o resultado e registra uma resposta manual.

## Login de demonstracao

- E-mail: `usuario@demo.com`
- Senha: `123456`

## Banco de dados PostgreSQL

O sistema usa PostgreSQL por padrao quando iniciado sem o profile `local`.

Configuracao esperada:

```properties
DB_URL=jdbc:postgresql://localhost:5432/verificadocs
DB_USERNAME=postgres
DB_PASSWORD=sua_senha_do_postgres
```

Na maquina atual, o executavel do psql fica em:

```text
C:\Program Files\PostgreSQL\18\bin\psql.exe
```

As consultas para mostrar as tabelas e registros ficam em `docs/postgresql-consultas.sql`.

Tabelas principais:

- `perfis`
- `usuarios`
- `logs_acesso`
- `documentos`
- `analises`
- `analises_ocorrencias`

## API de analise

O projeto usa OCR.space para extrair texto de PDFs ou imagens:

- Documentacao: https://ocr.space/ocrapi
- Endpoint: `https://api.ocr.space/parse/image`
- Header: `apikey`
- Campo multipart do arquivo: `file`

Configure a chave criando um arquivo `.env` na raiz do projeto. Voce pode copiar o `.env.example`:

```bash
copy .env.example .env
```

Depois edite esta linha dentro do `.env`:

```properties
OCR_SPACE_API_KEY=sua_chave
```

Tambem e possivel configurar pelo terminal:

```bash
set OCR_SPACE_API_KEY=sua_chave
```

O arquivo `.env` fica fora do Git por seguranca, entao a chave real nao vai para o GitHub.

Se a chave nao for configurada ou a API falhar, o sistema mantem o fluxo em modo demonstrativo e registra a origem da analise.
Na tela de resultado, o sistema mostra se a analise veio de `OCR real` ou `Demo`.

## Executar

### Pelo IntelliJ IDEA

Abra a pasta que contem o `pom.xml`:

```text
verifica-docs
```

Depois espere o IntelliJ carregar o Maven. A classe para executar e:

```text
src/main/java/com/trabalho/verificadocs/VerificaDocsApplication.java
```

Se o botao verde de Run nao aparecer:

1. Clique com o botao direito no `pom.xml`.
2. Clique em `Add as Maven Project`, se aparecer.
3. Va em `File > Project Structure > Project`.
4. Configure o SDK para Java 17 ou Java 21.
5. Abra `VerificaDocsApplication.java` de novo.

### Sem PostgreSQL instalado

Use o profile local com H2 em memoria. E o jeito mais facil para testar:

```bash
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Acesse:

```text
http://localhost:8080
```

### Com PostgreSQL via Docker

Com Docker aberto:

```bash
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Acesse:

```text
http://localhost:8080
```

### Com PostgreSQL instalado manualmente

Configure o banco `verificadocs` e rode:

```bash
.\mvnw.cmd spring-boot:run
```

Acesse:

```text
http://localhost:8080
```

## Tecnologias

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf
- PostgreSQL
- OCR.space
