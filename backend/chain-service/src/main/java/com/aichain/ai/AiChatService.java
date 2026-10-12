package com.aichain.ai;

import com.aichain.properties.AiProperties;
import dev.langchain4j.mcp.McpToolProvider;
import com.aichain.ai.http.UrlConnectionHttpClient;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Base64;

@Slf4j
@Service
public class AiChatService {
    private static final MediaType UTF8_TEXT = new MediaType("text", "plain", StandardCharsets.UTF_8);
    private final AiProperties properties;
    private final KnowledgeService knowledgeService;
    private final McpBusinessTools localTools;
    private volatile SupplyChainAssistant assistant;

    public AiChatService(AiProperties properties, KnowledgeService knowledgeService, McpBusinessTools localTools) {
        this.properties = properties;
        this.knowledgeService = knowledgeService;
        this.localTools = localTools;
    }

    public boolean configured() {
        return StringUtils.hasText(properties.getDeepseek().getApiKey());
    }

    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", "DeepSeek");
        result.put("model", properties.getDeepseek().getModel());
        result.put("configured", configured());
        result.put("mcpUrl", properties.getMcp().getUrl());
        result.put("knowledge", knowledgeService.status());
        return result;
    }

    public SseEmitter stream(String conversationId, String message) {
        SseEmitter emitter = new SseEmitter(180_000L);
        if (!configured()) {
            try {
                emitter.send(SseEmitter.event().name("error")
                        .data("DeepSeek API Key 尚未配置，请设置环境变量 DEEPSEEK_API_KEY 后重启后端。", UTF8_TEXT));
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
            return emitter;
        }

        try {
            TokenStream stream = assistant().chat(conversationId, message);
            stream.onPartialResponse(token -> send(emitter, "token", token))
                    .onRetrieved(contents -> send(emitter, "references", contents.toString()))
                    .onToolExecuted(tool -> send(emitter, "tool", tool.toString()))
                    .onCompleteResponse(response -> {
                        send(emitter, "done", "[DONE]");
                        emitter.complete();
                    })
                    .onError(error -> {
                        log.warn("DeepSeek对话失败", error);
                        send(emitter, "error", friendlyMessage(error));
                        emitter.complete();
                    })
                    .start();
        } catch (Exception ex) {
            send(emitter, "error", friendlyMessage(ex));
            emitter.complete();
        }
        return emitter;
    }

    private SupplyChainAssistant assistant() {
        if (assistant == null) {
            synchronized (this) {
                if (assistant == null) {
                    OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                            .httpClientBuilder(UrlConnectionHttpClient.builder())
                            .baseUrl(properties.getDeepseek().getBaseUrl())
                            .apiKey(properties.getDeepseek().getApiKey())
                            .modelName(properties.getDeepseek().getModel())
                            .temperature(0.2)
                            .timeout(Duration.ofSeconds(90))
                            .accumulateToolCallId(false)
                            .logRequests(false)
                            .logResponses(false)
                            .build();

                    AiServices<SupplyChainAssistant> builder = AiServices.builder(SupplyChainAssistant.class)
                            .streamingChatModel(model)
                            .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(20));

                    if (knowledgeService.available()) {
                        try {
                            builder.contentRetriever(knowledgeService.contentRetriever());
                        } catch (Exception ex) {
                            log.warn("Milvus暂不可用，本次启动先使用无RAG模式: {}", ex.getMessage());
                        }
                    } else {
                        log.info("Milvus端口未开放，本次启动使用无RAG模式");
                    }

                    if (isInProcessMcp()) {
                        // 内置端点仍完整实现 MCP；同 JVM 调用直接复用工具，避免 Windows/JDK 21 回环限制。
                        builder.tools(localTools);
                    } else {
                        try {
                            McpTransport transport = new StreamableHttpMcpTransport.Builder()
                                    .url(properties.getMcp().getUrl())
                                    .timeout(Duration.ofSeconds(10))
                                    .build();
                            McpClient client = new DefaultMcpClient.Builder()
                                    .key("supply-chain-mcp")
                                    .transport(transport)
                                    .initializationTimeout(Duration.ofSeconds(10))
                                    .toolExecutionTimeout(Duration.ofSeconds(15))
                                    .cacheToolList(true)
                                    .build();
                            builder.toolProvider(McpToolProvider.builder().mcpClients(client).build());
                        } catch (Exception ex) {
                            log.warn("MCP服务暂不可用，使用同等本地工具降级: {}", ex.getMessage());
                            builder.tools(localTools);
                        }
                    }
                    assistant = builder.build();
                }
            }
        }
        return assistant;
    }

    private boolean isInProcessMcp() {
        String url = properties.getMcp().getUrl();
        return url != null && (url.startsWith("http://localhost:8080/")
                || url.startsWith("http://127.0.0.1:8080/"));
    }

    private void send(SseEmitter emitter, String name, Object data) {
        try {
            String utf8Base64 = Base64.getEncoder().encodeToString(
                    String.valueOf(data).getBytes(StandardCharsets.UTF_8));
            emitter.send(SseEmitter.event().name(name).data(utf8Base64, UTF8_TEXT));
        } catch (IOException ex) {
            log.debug("SSE连接已关闭: {}", ex.getMessage());
        }
    }

    private String friendlyMessage(Throwable error) {
        String message = error.getMessage();
        if (message == null) return "AI服务暂时不可用，请稍后重试";
        if (message.contains("401") || message.toLowerCase().contains("unauthorized")) {
            return "DeepSeek API Key 无效或未生效，请检查 DEEPSEEK_API_KEY";
        }
        return "AI服务调用失败：" + message;
    }
}

