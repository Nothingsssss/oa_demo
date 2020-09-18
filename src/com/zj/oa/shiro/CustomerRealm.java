package com.zj.oa.shiro;

import com.zj.oa.pojo.ActiveUser;
import com.zj.oa.pojo.Employee;
import com.zj.oa.pojo.MenuTree;
import com.zj.oa.pojo.SysPermission;
import com.zj.oa.service.SysPermissionService;
import com.zj.oa.service.employeeService;
import com.zj.oa.service.sysService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class CustomerRealm extends AuthorizingRealm{

    @Autowired
    private employeeService employeeService;
    @Autowired
    private SysPermissionService sysPermissionService;
    @Autowired
    private sysService sysService;

    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        //用户输入的账户
        String username = (String) token.getPrincipal();
        Employee employee = null;
        List<MenuTree> menuTrees = null;
        try {
             employee = employeeService.findEmployee(username);
             menuTrees = sysPermissionService.loadMenuThree(employee.getName());
        }catch (Exception e){
            e.printStackTrace();
        }

        if(employee == null){
            return  null; //UnknownAccountException
        }



        //重新封装用户信息2
        ActiveUser activeUser = new ActiveUser();
        activeUser.setId(employee.getId());
        activeUser.setUsername(employee.getName());
        activeUser.setUsercode(employee.getName());
        activeUser.setManagerId(employee.getManagerId());
        activeUser.setMenuTree(menuTrees);

        String password = employee.getPassword();
        String salt = employee.getSalt();

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(activeUser, password, ByteSource.Util.bytes(salt), "CustomerRealm");
        return  info;

    }



    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
        ActiveUser activeUser = (ActiveUser) principal.getPrimaryPrincipal();
        //查询数据库认证用户拥有的角色和权限
        List<SysPermission> permissions = null;
        try {
            permissions = sysService.findPermissionListByUserId(activeUser.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> permisionList = new ArrayList<>();
        for (SysPermission sysPermission : permissions) {
            permisionList.add(sysPermission.getPercode());
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addStringPermissions(permisionList);

        return info;
    }
}
