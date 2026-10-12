package com.aichain.mapper;

import com.github.pagehelper.Page;
import com.aichain.annotation.AutoFill;
import com.aichain.dto.EmployeePageQueryDTO;
import com.aichain.entity.Employee;
import com.aichain.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @Insert("insert into employee(name,username,password,phone,sex,id_number,create_time,update_time,create_user,update_user,status)" +
            "values " +
            "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser},#{status})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Employee employee);

    //使用动态SQL,写到映射文件
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);

    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);

    @Select("select count(*) from employee")
    Integer countAll();

    @Select("select id,username,name,phone,sex,status,create_time,update_time from employee order by id limit #{limit}")
    List<Employee> listTop(Integer limit);
}
