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

Configure a chave:

```bash
set OCR_SPACE_API_KEY=sua_chave
```

Se a chave nao for configurada ou a API falhar, o sistema mantem o fluxo em modo demonstrativo e registra a origem da analise.
Na tela de resultado, o sistema mostra se a analise veio de `OCR real` ou `Demo`.

## Executar

Com JDK instalado:

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
