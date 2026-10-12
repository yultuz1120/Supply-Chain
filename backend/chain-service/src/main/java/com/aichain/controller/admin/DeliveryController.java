package com.aichain.controller.admin;

import com.aichain.dto.AssignCourierDTO;
import com.aichain.dto.CourierDTO;
import com.aichain.dto.DeliveryOrderPageQueryDTO;
import com.aichain.entity.Courier;
import com.aichain.result.PageResult;
import com.aichain.result.Result;
import com.aichain.service.DeliveryService;
import com.aichain.vo.DeliveryOrderVO;
import com.aichain.vo.DeliverySummaryVO;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/delivery")
public class DeliveryController {
    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/summary")
    public Result<DeliverySummaryVO> summary() {
        return Result.success(deliveryService.summary());
    }

    @GetMapping("/orders")
    public Result<PageResult> orders(DeliveryOrderPageQueryDTO query) {
        return Result.success(deliveryService.page(query));
    }

    @GetMapping("/orders/{id}")
    public Result<DeliveryOrderVO> order(@PathVariable Long id) {
        return Result.success(deliveryService.getById(id));
    }

    @PostMapping("/orders/{id}/assign")
    public Result<Void> assign(@PathVariable Long id, @Valid @RequestBody AssignCourierDTO dto) {
        deliveryService.assign(id, dto.getCourierId());
        return Result.success();
    }

    @PostMapping("/orders/{id}/status/{status}")
    public Result<Void> status(@PathVariable Long id, @PathVariable Integer status) {
        deliveryService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/orders/{id}/urge")
    public Result<String> urge(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body == null ? null : body.get("remark");
        return Result.success(deliveryService.urge(id, "PAGE", remark));
    }

    @GetMapping("/couriers")
    public Result<List<Courier>> couriers() {
        return Result.success(deliveryService.couriers());
    }

    @PostMapping("/couriers")
    public Result<Void> saveCourier(@Valid @RequestBody CourierDTO dto) {
        deliveryService.saveCourier(dto);
        return Result.success();
    }

    @PutMapping("/couriers")
    public Result<Void> updateCourier(@Valid @RequestBody CourierDTO dto) {
        deliveryService.updateCourier(dto);
        return Result.success();
    }

    @PostMapping("/couriers/{id}/status/{status}")
    public Result<Void> courierStatus(@PathVariable Long id, @PathVariable Integer status) {
        deliveryService.updateCourierStatus(id, status);
        return Result.success();
    }
}
