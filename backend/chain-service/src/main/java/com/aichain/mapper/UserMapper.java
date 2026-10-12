package com.aichain.mapper;

import com.aichain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * @author Yultuz
 */
@Mapper
public interface UserMapper {
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    void insert(User user);

    Integer countByMap(Map map);
}