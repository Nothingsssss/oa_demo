package com.zj.oa.service.impl;

import com.zj.oa.mapper.EmployeeMapper;
import com.zj.oa.mapper.SysPermissionMapper;
import com.zj.oa.mapper.SysPermissionMapperCustom;
import com.zj.oa.mapper.SysUserRoleMapper;
import com.zj.oa.pojo.*;
import com.zj.oa.service.employeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("employeeService")
public class employeeServiceImpl implements employeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private SysPermissionMapperCustom permissionMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;


    @Override
    public Employee findEmployee(String name) {
        EmployeeExample example = new EmployeeExample();
        EmployeeExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(name);
        List<Employee> employees = employeeMapper.selectByExample(example);
        if(employees != null && employees.size()>0){
            return employees.get(0);
        }
        return null;
    }

    @Override
    public Employee findEmployeeManager(Long managerId) {
        return employeeMapper.selectByPrimaryKey(managerId);
    }

    @Override
    public List<EmployeeCustom> findUserAndRoleList() {
        return permissionMapper.findUserAndRoleList();
    }

    @Override
    public boolean findEamil(String email) {
        Employee employee = this.employeeMapper.findEamil(email);
        if (employee == null){
            return false;
        }
        return true;
    }

    @Override
    public int saveUser(Employee user) {
        int i = this.employeeMapper.insertSelective(user);
        return i;
    }

    @Override
    public void updateEmployeeRole(String roleId, String userId) {
        SysUserRoleExample example = new SysUserRoleExample();
        SysUserRoleExample.Criteria criteria = example.createCriteria();
        criteria.andSysUserIdEqualTo(userId);

        SysUserRole userRole = userRoleMapper.selectByExample(example).get(0);
        userRole.setSysRoleId(roleId);

        userRoleMapper.updateByPrimaryKey(userRole);
    }

    @Override
    public List<Employee> findEmployeeByLevel(int level) {
        EmployeeExample example = new EmployeeExample();
        EmployeeExample.Criteria criteria = example.createCriteria();
        criteria.andRoleEqualTo(level);
        List<Employee> list = employeeMapper.selectByExample(example);

        return list;
    }

}
