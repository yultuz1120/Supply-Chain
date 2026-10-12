package com.aichain.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.aichain.constant.MessageConstant;
import com.aichain.constant.PasswordConstant;
import com.aichain.constant.StatusConstant;
import com.aichain.context.BaseContext;
import com.aichain.dto.EmployeeDTO;
import com.aichain.dto.EmployeeLoginDTO;
import com.aichain.dto.EmployeePageQueryDTO;
import com.aichain.entity.Employee;
import com.aichain.exception.AccountLockedException;
import com.aichain.exception.AccountNotFoundException;
import com.aichain.exception.PasswordErrorException;
import com.aichain.mapper.EmployeeMapper;
import com.aichain.result.PageResult;
import com.aichain.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //md5加密
       password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        //EmployeeDTO方便封装前端传回来的数据，但是传到mapper层建议用实体类
        Employee employee = new Employee();
//        employee.setName(employeeDTO.getName());
        //对象属性拷贝,前提是两个类名属性一致
        BeanUtils.copyProperties(employeeDTO, employee);
        //在employee中有多余的属性，需要手动设置.
        employee.setStatus(StatusConstant.ENABLE);//避免硬编码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //设置当前记录创建人Id和修改人id
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.insert(employee);
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);//目前返回的是page对象
        long total = page.getTotal();
        List<Employee> records = page.getResult();
        PageResult pageResult = new PageResult(total, records);
        return pageResult;
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);
        employeeMapper.update(employee);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("*****");
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }
}
