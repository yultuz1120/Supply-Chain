package com.aichain.ai;

import com.aichain.properties.AiProperties;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15q.BgeSmallZhV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.v2.MilvusV2EmbeddingStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class KnowledgeService {
    private final AiProperties properties;
    private final EmbeddingModel embeddingModel = new BgeSmallZhV15QuantizedEmbeddingModel();
    private volatile EmbeddingStore<TextSegment> embeddingStore;

    public KnowledgeService(AiProperties properties) {
        this.properties = properties;
    }

    public synchronized Map<String, Object> indexBuiltInDocuments() {
        List<TextSegment> segments = loadBuiltInSegments();
        EmbeddingStore<TextSegment> store = store();
        store.removeAll();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        store.addAll(embeddings, segments);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("documentCount", listBuiltInDocuments().size());
        result.put("segmentCount", segments.size());
        result.put("collection", properties.getMilvus().getCollection());
        result.put("message", "农产品演示知识库已写入 Milvus");
        return result;
    }

    public List<Map<String, Object>> search(String query, int maxResults) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(Math.max(1, Math.min(maxResults, 10)))
                .minScore(0.45)
                .build();
        EmbeddingSearchResult<TextSegment> result = store().search(request);
        List<Map<String, Object>> matches = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : result.matches()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("score", match.score());
            item.put("text", match.embedded().text());
            item.put("document", match.embedded().metadata().getString("document"));
            item.put("category", match.embedded().metadata().getString("category"));
            matches.add(item);
        }
        return matches;
    }

    public ContentRetriever contentRetriever() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store())
                .embeddingModel(embeddingModel)
                .maxResults(4)
                .minScore(0.45)
                .build();
    }

    public List<String> listBuiltInDocuments() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:knowledge/*.md");
            List<String> names = new ArrayList<>();
            for (Resource resource : resources) {
                names.add(resource.getFilename());
            }
            Collections.sort(names);
            return names;
        } catch (IOException ex) {
            throw new IllegalStateException("读取内置知识文档失败", ex);
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("uri", properties.getMilvus().getUri());
        result.put("collection", properties.getMilvus().getCollection());
        result.put("embeddingModel", "bge-small-zh-v1.5-quantized");
        result.put("documentCount", listBuiltInDocuments().size());
        result.put("initialized", embeddingStore != null);
        result.put("available", available());
        return result;
    }

    public boolean available() {
        try {
            URI uri = URI.create(properties.getMilvus().getUri());
            int port = uri.getPort() > 0 ? uri.getPort() : 19530;
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(uri.getHost(), port), 500);
                return true;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    private List<TextSegment> loadBuiltInSegments() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:knowledge/*.md");
            List<TextSegment> segments = new ArrayList<>();
            for (Resource resource : resources) {
                String text = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String fileName = resource.getFilename();
                String category = fileName == null ? "农产品" : fileName.replaceFirst("^\\d+-", "").replace(".md", "");
                int start = 0;
                int chunk = 500;
                int overlap = 80;
                while (start < text.length()) {
                    int end = Math.min(start + chunk, text.length());
                    String part = text.substring(start, end).trim();
                    if (!part.isEmpty()) {
                        Metadata metadata = new Metadata()
                                .put("document", fileName == null ? "演示资料" : fileName)
                                .put("category", category);
                        segments.add(TextSegment.from(part, metadata));
                    }
                    if (end == text.length()) break;
                    start = end - overlap;
                }
            }
            if (segments.isEmpty()) throw new IllegalStateException("没有找到内置知识文档");
            return segments;
        } catch (IOException ex) {
            throw new IllegalStateException("读取内置知识文档失败", ex);
        }
    }

    private EmbeddingStore<TextSegment> store() {
        if (embeddingStore == null) {
            synchronized (this) {
                if (embeddingStore == null) {
                    MilvusV2EmbeddingStore.Builder builder = MilvusV2EmbeddingStore.builder()
                            .uri(properties.getMilvus().getUri())
                            .collectionName(properties.getMilvus().getCollection())
                            .dimension(embeddingModel.dimension())
                            .autoFlushOnInsert(true);
                    if (StringUtils.hasText(properties.getMilvus().getToken())) {
                        builder.token(properties.getMilvus().getToken());
                    }
                    embeddingStore = builder.build();
                }
            }
        }
        return embeddingStore;
    }
}

