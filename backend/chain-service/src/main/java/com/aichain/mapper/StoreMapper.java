package com.aichain.mapper;

import com.github.pagehelper.Page;
import com.aichain.entity.Store;
import com.aichain.vo.StoreOverviewVO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface StoreMapper {

    @Select("<script>" +
            "select s.id, s.name, s.status, s.create_time, " +
            "sum(case when d.status = 1 then 1 else 0 end) pending_count, " +
            "sum(case when d.status = 3 then 1 else 0 end) delivering_count, " +
            "sum(case when d.status = 4 then 1 else 0 end) delivered_count, " +
            "sum(case when d.urge_count &gt; 0 and d.status &lt; 4 then 1 else 0 end) urged_count " +
            "from sc_store s left join sc_delivery_order d on d.store_id = s.id " +
            "<where><if test='keyword != null and keyword != &quot;&quot;'>" +
            "and (cast(s.id as char) like concat('%',#{keyword},'%') or s.name like concat('%',#{keyword},'%'))" +
            "</if></where> group by s.id, s.name, s.status, s.create_time order by s.id" +
            "</script>")
    Page<StoreOverviewVO> page(@Param("keyword") String keyword);

    @Select("select * from sc_store where id = #{id}")
    Store getById(Long id);

    @Select("select count(*) from sc_store")
    Integer countAll();

    @Insert("insert into sc_store(name,status,create_time,update_time) values(#{name},#{status},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Store store);

    @Update("update sc_store set name=#{name}, status=#{status}, update_time=now() where id=#{id}")
    void update(Store store);
}

