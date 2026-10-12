package com.aichain.mapper;

import com.aichain.entity.Courier;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourierMapper {
    @Select("select * from sc_courier order by online_status desc, id")
    List<Courier> list();

    @Select("select * from sc_courier where id=#{id}")
    Courier getById(Long id);

    @Select("select count(*) from sc_courier")
    Integer countAll();

    @Insert("insert into sc_courier(name,phone,online_status,create_time,update_time) values(#{name},#{phone},#{onlineStatus},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Courier courier);

    @Update("update sc_courier set name=#{name}, phone=#{phone}, online_status=#{onlineStatus}, update_time=now() where id=#{id}")
    void update(Courier courier);

    @Update("update sc_courier set online_status=#{status}, update_time=now() where id=#{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}

