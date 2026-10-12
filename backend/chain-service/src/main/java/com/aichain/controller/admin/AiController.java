package com.aichain.controller.admin;

import com.aichain.ai.AiChatService;
import com.aichain.ai.KnowledgeService;
import com.aichain.result.Result;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/admin/ai")
public class AiController {
    private static final MediaType UTF8_TEXT = new MediaType("text", "plain", StandardCharsets.UTF_8);
    private final AiChatService chatService;
    private final KnowledgeService knowledgeService;

    public AiController(AiChatService chatService, KnowledgeService knowledgeService) {
        this.chatService = chatService;
        this.knowledgeService = knowledgeService;
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> status() {
        return Result.success(chatService.status());
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody Map<String, String> body) {
        String conversationId = body.getOrDefault("conversationId", UUID.randomUUID().toString());
        String message = body.get("message");
        if (message == null || message.trim().isEmpty()) {
            SseEmitter emitter = new SseEmitter();
            try {
                emitter.send(SseEmitter.event().name("error").data("请输入问题", UTF8_TEXT));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
            return emitter;
        }
        return chatService.stream(conversationId, message.trim());
    }

    @GetMapping("/knowledge/documents")
    public Result<List<String>> documents() {
        return Result.success(knowledgeService.listBuiltInDocuments());
    }

    @PostMapping("/knowledge/index")
    public Result<Map<String, Object>> index() {
        return Result.success(knowledgeService.indexBuiltInDocuments());
    }

    @PostMapping("/knowledge/search")
    public Result<List<Map<String, Object>>> search(@RequestBody Map<String, Object> body) {
        String query = String.valueOf(body.getOrDefault("query", ""));
        int maxResults = body.get("maxResults") instanceof Number ? ((Number) body.get("maxResults")).intValue() : 5;
        return Result.success(knowledgeService.search(query, maxResults));
    }
}
