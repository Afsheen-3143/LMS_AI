package com.afsheen.aiassistant.rag;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * The "R" in RAG (Retrieval-Augmented Generation).
 *
 * <p><b>Embeddings &amp; vector similarity search:</b> every chunk in the
 * vector store was converted to a fixed-length numeric vector (an
 * "embedding") that captures its meaning. To retrieve, the user's question is
 * embedded the same way, and {@link VectorStore#similaritySearch} returns the
 * chunks whose vectors are closest to the question's vector by cosine
 * similarity - i.e. the chunks that mean something similar to the question,
 * even if they don't share exact keywords.
 *
 * <p><b>Retrieval - top-k + a lightweight rerank:</b> a single similarity
 * search is a blunt instrument, so this over-fetches a larger candidate pool
 * ({@code topK * OVERFETCH_FACTOR}) and then reranks it by blending the
 * vector similarity score with a plain keyword-overlap score before cutting
 * back down to {@code topK}. This is a heuristic, not a trained cross-encoder
 * reranker (Spring AI 1.0 does not ship one out of the box) - it simply
 * nudges results that also share literal terms with the question above
 * results that are only vector-similar, which helps with exact things like
 * course codes or fee amounts that embeddings alone can blur together.
 */
@Service
public class RagRetrievalService {

    private static final Logger logger = LogManager.getLogger(RagRetrievalService.class);

    private static final int OVERFETCH_FACTOR = 3;
    private static final double SIMILARITY_THRESHOLD = 0.5;

    private final VectorStore vectorStore;

    public RagRetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> retrieve(String query, int topK) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK * OVERFETCH_FACTOR)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();

        List<Document> candidates = vectorStore.similaritySearch(searchRequest);
        if (candidates == null || candidates.isEmpty()) {
            logger.debug("No vector store matches above similarity threshold for query: {}", query);
            return List.of();
        }

        Set<String> queryTerms = tokenize(query);

        return candidates.stream()
                .sorted(Comparator.comparingDouble(
                        (Document doc) -> combinedScore(doc, queryTerms)).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    private double combinedScore(Document document, Set<String> queryTerms) {
        // Document.getScore() is the vector similarity (0..1) from the store.
        double vectorScore = document.getScore() != null ? document.getScore() : 0.0;

        Set<String> docTerms = tokenize(document.getText());
        long overlap = queryTerms.stream().filter(docTerms::contains).count();
        double keywordScore = queryTerms.isEmpty() ? 0.0 : (double) overlap / queryTerms.size();

        // Weighted blend: similarity does most of the work, keyword overlap
        // is a tie-breaker/boost, not the primary signal.
        return (0.8 * vectorScore) + (0.2 * keywordScore);
    }

    private Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("\\W+"))
                .collect(Collectors.toSet());
    }
}
