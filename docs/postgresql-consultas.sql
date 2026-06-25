-- Execute no psql ou no Query Tool do pgAdmin.
-- Banco usado pela aplicacao:
--   verificadocs

CREATE DATABASE verificadocs;

-- Depois de conectar no banco verificadocs, use:

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- Tabelas principais geradas pelo JPA:
--   perfis
--   usuarios
--   logs_acesso
--   documentos
--   analises
--   analises_ocorrencias

SELECT id, nome_original, tipo, mime_type, status, data_upload, tamanho_bytes, caminho_arquivo
FROM documentos
ORDER BY data_upload DESC;

SELECT
    a.id,
    d.nome_original,
    a.status,
    a.grau_confianca,
    a.score_suspeita,
    a.indicio_edicao,
    a.modo_demonstracao,
    a.protocolo_externo,
    a.data_solicitacao,
    a.data_retorno
FROM analises a
JOIN documentos d ON d.id = a.documento_id
ORDER BY a.data_solicitacao DESC;

SELECT
    ao.id,
    ao.analise_id,
    ao.tipo_ocorrencia,
    ao.campo,
    ao.valor_encontrado,
    ao.confianca
FROM analises_ocorrencias ao
ORDER BY ao.id DESC;
