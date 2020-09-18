package com.zj.oa.utils;

import com.zj.oa.pojo.ActiveUser;
import com.zj.oa.pojo.Employee;
import com.zj.oa.service.employeeService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class ManagerTaskHandler implements TaskListener{
    @Override
    public void notify(DelegateTask delegateTask) {
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();

        employeeService employeeService = (employeeService) context.getBean("employeeService");
        Employee manager = employeeService.findEmployeeManager(activeUser.getManagerId());
        //设置个人任务的办理人
        delegateTask.setAssignee(manager.getName());
    }
}
