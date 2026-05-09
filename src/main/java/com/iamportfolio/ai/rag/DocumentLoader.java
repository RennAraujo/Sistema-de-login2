package com.iamportfolio.ai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

/**
 * Loads markdown files from docs/RAG_CORPUS/ at app start, chunks them,
 * and persists each chunk as a row in rag_documents. Idempotent:
 * skips files whose SHA-256 hasn't changed since the last index.
 */
@Component
public class DocumentLoader {

    private static final Logger logger = LoggerFactory.getLogger(DocumentLoader.class);
    private static final int CHUNK_CHARS = 800;

    @Autowired
    private RagDocumentRepository repository;

    @Value("${app.ai.rag.corpus-dir:docs/RAG_CORPUS}")
    private String corpusDir;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void loadOnStartup() {
        Path dir = Paths.get(corpusDir).toAbsolutePath();
        if (!Files.isDirectory(dir)) {
            logger.warn("RAG corpus dir not found: {} — assistant will run without retrieval context.", dir);
            return;
        }
        try (Stream<Path> files = Files.list(dir)) {
            int total = 0, indexed = 0;
            for (Path file : files.filter(p -> p.toString().endsWith(".md")).toList()) {
                total++;
                String content = Files.readString(file);
                String hash = sha256(content);
                String relative = dir.relativize(file).toString();
                if (repository.findFirstBySourcePathAndSourceHash(relative, hash).isPresent()) {
                    continue;
                }
                repository.deleteBySourcePath(relative);
                List<String> chunks = chunk(content);
                for (int i = 0; i < chunks.size(); i++) {
                    RagDocument doc = new RagDocument();
                    doc.setSourcePath(relative);
                    doc.setSourceHash(hash);
                    doc.setChunkIndex(i);
                    doc.setContent(chunks.get(i));
                    repository.save(doc);
                }
                indexed++;
                logger.info("Indexed {} ({} chunks)", relative, chunks.size());
            }
            logger.info("RAG corpus loaded: {} files scanned, {} (re)indexed", total, indexed);
        } catch (IOException e) {
            logger.warn("Failed to scan RAG corpus dir {}: {}", dir, e.getMessage());
        }
    }

    static List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            int end = Math.min(i + CHUNK_CHARS, text.length());
            // try to break at a paragraph or sentence boundary
            if (end < text.length()) {
                int para = text.lastIndexOf("\n\n", end);
                if (para > i + CHUNK_CHARS / 2) end = para;
            }
            chunks.add(text.substring(i, end).trim());
            i = end;
        }
        return chunks;
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes()));
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }
}
