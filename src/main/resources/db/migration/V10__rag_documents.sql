-- V10: RAG corpus storage.
-- Each row is a chunk of one of the markdown files in docs/RAG_CORPUS.
-- Retrieval uses Postgres' built-in full-text search (tsvector + GIN
-- index) instead of pgvector — keeps the dev/prod images identical
-- (postgres:16-alpine, no pgvector extension required) while still
-- delivering ranked retrieval. Swap to pgvector later by replacing
-- the GIN index and the search query in RetrievalService.
--
-- source_hash lets the loader skip re-indexing files that haven't
-- changed (idempotent reload).

CREATE TABLE rag_documents (
    id            BIGSERIAL PRIMARY KEY,
    source_path   VARCHAR(512) NOT NULL,
    source_hash   VARCHAR(64)  NOT NULL,
    chunk_index   INTEGER      NOT NULL,
    content       TEXT         NOT NULL,
    search_vector TSVECTOR     GENERATED ALWAYS AS (to_tsvector('english', content)) STORED,
    indexed_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rag_documents_search ON rag_documents USING GIN (search_vector);
CREATE INDEX idx_rag_documents_source ON rag_documents (source_path, source_hash);
