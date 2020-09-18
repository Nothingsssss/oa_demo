package com.zj.oa.service;

import com.zj.oa.pojo.Employee;
import com.zj.oa.pojo.EmployeeCustom;

import java.util.List;

public interface employeeService {
    Employee findEmployee(String name);

    Employee findEmployeeManager(Long managerId);

    List<EmployeeCustom> findUserAndRoleList();

    boolean findEamil(String email);

    int saveUser(Employee user);

    void updateEmployeeRole(String roleId, String userId);

    List<Employee> findEmployeeByLevel(int level);
}
