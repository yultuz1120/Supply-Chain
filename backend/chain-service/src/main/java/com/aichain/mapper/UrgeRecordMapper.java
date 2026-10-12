package com.aichain.mapper;

import com.aichain.entity.UrgeRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface UrgeRecordMapper {
    @Insert("insert into sc_urge_record(delivery_order_id,store_id,operator_id,source,remark,create_time) " +
            "values(#{deliveryOrderId},#{storeId},#{operatorId},#{source},#{remark},#{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UrgeRecord record);
}

