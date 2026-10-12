package com.aichain.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface SupplyChainAssistant {
    @SystemMessage({
            "你是AI智慧供应链项目的智能客服。使用简洁、专业的中文回答。",
            "农产品知识问题优先依据检索到的知识库资料，并说明资料名称。",
            "实时人员、门店和配送问题应调用工具，不得编造数据。",
            "只有用户明确要求催促某个配送单时，才能调用urge_delivery_order。",
            "若缺少门店ID或配送单ID，先向用户询问，不要猜测。"
    })
    TokenStream chat(@MemoryId String conversationId, @UserMessage String message);
}

