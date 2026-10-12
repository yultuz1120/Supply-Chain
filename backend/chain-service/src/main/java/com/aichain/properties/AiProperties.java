package com.aichain.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "chain.ai")
public class AiProperties {
    private Deepseek deepseek = new Deepseek();
    private Milvus milvus = new Milvus();
    private Mcp mcp = new Mcp();

    @Data
    public static class Deepseek {
        private String baseUrl = "https://api.deepseek.com";
        private String apiKey;
        private String model = "deepseek-flash";
    }

    @Data
    public static class Milvus {
        private String uri = "http://localhost:19530";
        private String token;
        private String collection = "supply_chain_knowledge";
    }

    @Data
    public static class Mcp {
        private String url = "http://localhost:8080/mcp";
    }
}

