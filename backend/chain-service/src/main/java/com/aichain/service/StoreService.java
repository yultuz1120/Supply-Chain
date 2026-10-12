package com.aichain.service;

import com.aichain.dto.StoreDTO;
import com.aichain.entity.Store;
import com.aichain.result.PageResult;

public interface StoreService {
    PageResult page(int page, int pageSize, String keyword);
    Store getById(Long id);
    void save(StoreDTO dto);
    void update(StoreDTO dto);
}

