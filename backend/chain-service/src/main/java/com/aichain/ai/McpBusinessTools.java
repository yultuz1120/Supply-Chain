package com.aichain.ai;

import com.alibaba.fastjson.JSON;
import com.aichain.dto.DeliveryOrderPageQueryDTO;
import com.aichain.entity.Employee;
import com.aichain.mapper.EmployeeMapper;
import com.aichain.service.DeliveryService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class McpBusinessTools {
    private final EmployeeMapper employeeMapper;
    private final DeliveryService deliveryService;

    public McpBusinessTools(EmployeeMapper employeeMapper, DeliveryService deliveryService) {
        this.employeeMapper = employeeMapper;
        this.deliveryService = deliveryService;
    }

    @Tool("查询集团人员列表，返回人员ID、姓名、账号、手机号和状态")
    public String listStaff(@P("最多返回多少条，建议1到20") Integer limit) {
        int safeLimit = limit == null ? 10 : Math.max(1, Math.min(limit, 20));
        List<Employee> employees = employeeMapper.listTop(safeLimit);
        return JSON.toJSONString(employees);
    }

    @Tool("根据门店ID查询配送订单，可选配送状态：1待接单、2已接单、3配送中、4已送达、5已取消")
    public String getStoreDeliveryOrders(@P("门店ID") Long storeId,
                                         @P("配送状态，可不传") Integer status) {
        DeliveryOrderPageQueryDTO query = new DeliveryOrderPageQueryDTO();
        query.setPage(1);
        query.setPageSize(20);
        query.setStoreId(storeId);
        query.setStatus(status);
        return JSON.toJSONString(deliveryService.page(query));
    }

    @Tool("根据配送单ID查询配送状态、门店和配送员信息")
    public String getDeliveryOrderStatus(@P("配送单ID") Long deliveryOrderId) {
        return JSON.toJSONString(deliveryService.getById(deliveryOrderId));
    }

    @Tool("查询配送中心汇总数据，包括门店、人员、骑手、待接单、配送中、已送达和催单数量")
    public String getDeliveryCenterSummary() {
        return JSON.toJSONString(deliveryService.summary());
    }

    @Tool("对未完成的配送单发起催单。只有用户明确要求催单时才能调用")
    public String urgeDeliveryOrder(@P("配送单ID") Long deliveryOrderId,
                                    @P("催单原因或用户原话") String remark) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", deliveryService.urge(deliveryOrderId, "AI_MCP", remark));
        result.put("deliveryOrderId", deliveryOrderId);
        return JSON.toJSONString(result);
    }
}

