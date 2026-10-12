package com.aichain.controller.admin;

import com.aichain.dto.StoreDTO;
import com.aichain.entity.Store;
import com.aichain.result.PageResult;
import com.aichain.result.Result;
import com.aichain.service.StoreService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/admin/stores")
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public Result<PageResult> page(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   String keyword) {
        return Result.success(storeService.page(page, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public Result<Store> detail(@PathVariable Long id) {
        return Result.success(storeService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody StoreDTO dto) {
        storeService.save(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody StoreDTO dto) {
        storeService.update(dto);
        return Result.success();
    }
}

