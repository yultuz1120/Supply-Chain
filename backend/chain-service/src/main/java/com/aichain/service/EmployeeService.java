package com.aichain.service;

import com.aichain.dto.EmployeeDTO;
import com.aichain.dto.EmployeeLoginDTO;
import com.aichain.dto.EmployeePageQueryDTO;
import com.aichain.entity.Employee;
import com.aichain.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);
//新增员工
    void save(EmployeeDTO employeeDTO);

    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    void startOrStop(Integer status, Long id);

    Employee getById(Long id);

    void update(EmployeeDTO employeeDTO);
}
