package com.aichain.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.aichain.dto.StoreDTO;
import com.aichain.entity.Store;
import com.aichain.exception.BaseException;
import com.aichain.mapper.StoreMapper;
import com.aichain.result.PageResult;
import com.aichain.service.StoreService;
import com.aichain.vo.StoreOverviewVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class StoreServiceImpl implements StoreService {
    private final StoreMapper storeMapper;

    public StoreServiceImpl(StoreMapper storeMapper) {
        this.storeMapper = storeMapper;
    }

    @Override
    public PageResult page(int page, int pageSize, String keyword) {
        PageHelper.startPage(page, pageSize);
        Page<StoreOverviewVO> result = storeMapper.page(keyword);
        return new PageResult(result.getTotal(), result.getResult());
    }

    @Override
    public Store getById(Long id) {
        Store store = storeMapper.getById(id);
        if (store == null) {
            throw new BaseException("门店不存在");
        }
        return store;
    }

    @Override
    public void save(StoreDTO dto) {
        Store store = new Store();
        BeanUtils.copyProperties(dto, store);
        storeMapper.insert(store);
    }

    @Override
    public void update(StoreDTO dto) {
        if (dto.getId() == null || storeMapper.getById(dto.getId()) == null) {
            throw new BaseException("门店不存在");
        }
        Store store = new Store();
        BeanUtils.copyProperties(dto, store);
        storeMapper.update(store);
    }
}

