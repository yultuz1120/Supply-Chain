package com.aichain.mapper;

import com.github.pagehelper.Page;
import com.aichain.dto.DeliveryOrderPageQueryDTO;
import com.aichain.entity.DeliveryOrder;
import com.aichain.vo.DeliveryOrderVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface DeliveryOrderMapper {

    @Select("<script>" +
            "select d.*, s.name store_name, c.name courier_name, c.phone courier_phone " +
            "from sc_delivery_order d " +
            "left join sc_store s on s.id=d.store_id " +
            "left join sc_courier c on c.id=d.courier_id " +
            "<where>" +
            "<if test='storeId != null'>and d.store_id=#{storeId}</if>" +
            "<if test='courierId != null'>and d.courier_id=#{courierId}</if>" +
            "<if test='status != null'>and d.status=#{status}</if>" +
            "<if test='deliveryNumber != null and deliveryNumber != &quot;&quot;'>and d.delivery_number like concat('%',#{deliveryNumber},'%')</if>" +
            "</where> order by case when d.urge_count &gt; 0 and d.status &lt; 4 then 0 else 1 end, d.create_time desc" +
            "</script>")
    Page<DeliveryOrderVO> page(DeliveryOrderPageQueryDTO query);

    @Select("select d.*, s.name store_name, c.name courier_name, c.phone courier_phone " +
            "from sc_delivery_order d left join sc_store s on s.id=d.store_id " +
            "left join sc_courier c on c.id=d.courier_id where d.id=#{id}")
    DeliveryOrderVO getById(Long id);

    @Select("select count(*) from sc_delivery_order where status=#{status}")
    Integer countByStatus(Integer status);

    @Select("select count(*) from sc_delivery_order where urge_count > 0 and status < 4")
    Integer countUrged();

    @Update("update sc_delivery_order set courier_id=#{courierId}, status=2, accepted_time=now(), update_time=now() where id=#{id} and status=1")
    int assign(@Param("id") Long id, @Param("courierId") Long courierId);

    @Update("<script>update sc_delivery_order set status=#{status}, update_time=now()" +
            "<if test='status == 4'>, delivered_time=now()</if> where id=#{id}</script>")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("update sc_delivery_order set urge_count=urge_count+1,last_urge_time=#{urgeTime},update_time=#{urgeTime} where id=#{id} and status < 4")
    int incrementUrge(@Param("id") Long id, @Param("urgeTime") LocalDateTime urgeTime);

    @Insert("insert into sc_delivery_order(delivery_number,store_id,source_order_id,status,receiver_name,receiver_phone,delivery_address,urge_count,create_time,update_time) " +
            "values(#{deliveryNumber},#{storeId},#{sourceOrderId},#{status},#{receiverName},#{receiverPhone},#{deliveryAddress},0,now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(DeliveryOrder deliveryOrder);
}

