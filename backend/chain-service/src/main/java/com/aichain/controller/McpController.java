package com.aichain.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aichain.ai.McpBusinessTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 供 LangChain4j MCP Client 调用的 Streamable HTTP JSON-RPC 端点。
 * Demo 只实现 initialize、tools/list、tools/call 和 ping。
 */
@Slf4j
@RestController
@RequestMapping("/mcp")
public class McpController {
    private final McpBusinessTools tools;
    private final ObjectMapper objectMapper;

    public McpController(McpBusinessTools tools, ObjectMapper objectMapper) {
        this.tools = tools;
        this.objectMapper = objectMapper;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handle(@RequestBody JsonNode request) {
        JsonNode id = request.get("id");
        String method = request.path("method").asText();
        if (id == null && method.startsWith("notifications/")) {
            return ResponseEntity.accepted().build();
        }
        try {
            Object result;
            switch (method) {
                case "initialize":
                    result = initialize(request.path("params"));
                    break;
                case "ping":
                    result = Collections.emptyMap();
                    break;
                case "tools/list":
                    result = Collections.singletonMap("tools", toolDefinitions());
                    break;
                case "tools/call":
                    result = callTool(request.path("params"));
                    break;
                default:
                    return response(id, null, error(-32601, "Method not found: " + method));
            }
            return response(id, result, null);
        } catch (Exception ex) {
            log.warn("MCP调用失败 method={}", method, ex);
            return response(id, null, error(-32000, ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Void> getChannel() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> closeSession() {
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> initialize(JsonNode params) {
        String protocolVersion = params.path("protocolVersion").asText("2025-06-18");
        Map<String, Object> capabilities = Collections.singletonMap("tools", Collections.singletonMap("listChanged", false));
        Map<String, Object> serverInfo = new LinkedHashMap<>();
        serverInfo.put("name", "ai-supply-chain-mcp");
        serverInfo.put("version", "1.0.0");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", protocolVersion);
        result.put("capabilities", capabilities);
        result.put("serverInfo", serverInfo);
        result.put("instructions", "查询集团人员、门店配送状态，并在用户明确要求时执行配送催单。");
        return result;
    }

    private List<Map<String, Object>> toolDefinitions() {
        List<Map<String, Object>> definitions = new ArrayList<>();
        definitions.add(tool("list_staff", "查询集团人员列表",
                schema(Collections.singletonMap("limit", integerProperty("最大条数，1到20")), Collections.emptyList())));
        Map<String, Object> storeProps = new LinkedHashMap<>();
        storeProps.put("storeId", integerProperty("门店ID"));
        storeProps.put("status", integerProperty("配送状态，可选"));
        definitions.add(tool("get_store_delivery_orders", "查询指定门店的配送订单",
                schema(storeProps, Collections.singletonList("storeId"))));
        definitions.add(tool("get_delivery_order_status", "查询配送单详情和状态",
                schema(Collections.singletonMap("deliveryOrderId", integerProperty("配送单ID")), Collections.singletonList("deliveryOrderId"))));
        definitions.add(tool("get_delivery_center_summary", "查询配送中心汇总指标",
                schema(Collections.emptyMap(), Collections.emptyList())));
        Map<String, Object> urgeProps = new LinkedHashMap<>();
        urgeProps.put("deliveryOrderId", integerProperty("配送单ID"));
        urgeProps.put("remark", stringProperty("催单原因"));
        definitions.add(tool("urge_delivery_order", "用户明确要求时，对未完成配送单发起催单",
                schema(urgeProps, Collections.singletonList("deliveryOrderId"))));
        return definitions;
    }

    private Map<String, Object> callTool(JsonNode params) {
        String name = params.path("name").asText();
        JsonNode args = params.path("arguments");
        String text;
        switch (name) {
            case "list_staff":
                text = tools.listStaff(nullableInt(args, "limit"));
                break;
            case "get_store_delivery_orders":
                text = tools.getStoreDeliveryOrders(requiredLong(args, "storeId"), nullableInt(args, "status"));
                break;
            case "get_delivery_order_status":
                text = tools.getDeliveryOrderStatus(requiredLong(args, "deliveryOrderId"));
                break;
            case "get_delivery_center_summary":
                text = tools.getDeliveryCenterSummary();
                break;
            case "urge_delivery_order":
                text = tools.urgeDeliveryOrder(requiredLong(args, "deliveryOrderId"), args.path("remark").asText("AI客服催单"));
                break;
            default:
                throw new IllegalArgumentException("未知工具: " + name);
        }
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "text");
        content.put("text", text);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", Collections.singletonList(content));
        result.put("isError", false);
        return result;
    }

    private ResponseEntity<Map<String, Object>> response(JsonNode id, Object result, Object error) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("jsonrpc", "2.0");
        body.put("id", id == null || id.isNull() ? null : objectMapper.convertValue(id, Object.class));
        if (error == null) body.put("result", result); else body.put("error", error);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Mcp-Session-Id", "supply-chain-demo");
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    private Map<String, Object> error(int code, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message == null ? "MCP tool failed" : message);
        return error;
    }

    private Map<String, Object> tool(String name, String description, Map<String, Object> inputSchema) {
        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("name", name);
        tool.put("description", description);
        tool.put("inputSchema", inputSchema);
        return tool;
    }

    private Map<String, Object> schema(Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        schema.put("additionalProperties", false);
        return schema;
    }

    private Map<String, Object> integerProperty(String description) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("type", "integer");
        value.put("description", description);
        return value;
    }

    private Map<String, Object> stringProperty(String description) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("type", "string");
        value.put("description", description);
        return value;
    }

    private Long requiredLong(JsonNode args, String name) {
        if (!args.hasNonNull(name)) throw new IllegalArgumentException("缺少参数: " + name);
        return args.get(name).asLong();
    }

    private Integer nullableInt(JsonNode args, String name) {
        return args.hasNonNull(name) ? args.get(name).asInt() : null;
    }
}
