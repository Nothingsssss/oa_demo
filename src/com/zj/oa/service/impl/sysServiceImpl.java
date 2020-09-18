package com.zj.oa.service.impl;

import com.zj.oa.mapper.SysPermissionMapper;
import com.zj.oa.mapper.SysPermissionMapperCustom;
import com.zj.oa.mapper.SysRoleMapper;
import com.zj.oa.mapper.SysRolePermissionMapper;
import com.zj.oa.pojo.*;
import com.zj.oa.service.sysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class sysServiceImpl implements sysService {

    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysPermissionMapperCustom sysPermissionMapperCustom;
    @Autowired
    private SysPermissionMapper sysPermissionMapper;
    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Override
    public List<SysRole> findAllRoles() {
        return roleMapper.selectByExample(null);
    }

    @Override
    public SysRole findRolesAndPermissionsByUserId(String userId) {
        return sysPermissionMapperCustom.findRoleAndPermissionListByUserId(userId);
    }

    @Override
    public List<MenuTree> loadMenuTree() {
        return sysPermissionMapperCustom.getMenuTree1();
    }

    @Override
    public List<SysPermission> findAllMenus() {
        SysPermissionExample example = new SysPermissionExample();
        SysPermissionExample.Criteria criteria = example.createCriteria();
        //criteria.andTypeLike("%menu%");
        criteria.andTypeEqualTo("menu");
        return sysPermissionMapper.selectByExample(example);
    }

    @Override
    public List<SysRole> findRolesAndPermissions() {
        return sysPermissionMapperCustom.findRoleAndPermissionList();
    }

    @Override
    public void addRoleAndPermissions(SysRole role, int[] permissionIds) {
        //添加角色
        roleMapper.insert(role);
        //添加角色和权限关系表
        for (int i = 0; i < permissionIds.length; i++) {
            SysRolePermission rolePermission = new SysRolePermission();
            //16进制随机码
            String uuid = UUID.randomUUID().toString();
            rolePermission.setId(uuid);
            rolePermission.setSysRoleId(role.getId());
            rolePermission.setSysPermissionId(permissionIds[i]+"");
            rolePermissionMapper.insert(rolePermission);
        }
    }

    @Override
    public void addSysPermission(SysPermission permission) {
        sysPermissionMapper.insert(permission);
    }

    @Override
    public void deletePermission(long id) {
        sysPermissionMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<MenuTree> getAllMenuAndPermision() {
        return sysPermissionMapperCustom.getAllMenuAndPermision();
    }

    @Override
    public List<SysPermission> findPermissionsByRoleId(String roleId) {
        return sysPermissionMapperCustom.findPermissionsByRoleId(roleId);
    }

    @Override
    public void deleteRoles(String roleId) {
        this.roleMapper.deleteByPrimaryKey(roleId);
    }

    @Override
    public void updateRoleAndPermissions(String roleId, int[] permissionIds) {
        //先删除角色权限关系表中角色的权限关系
        SysRolePermissionExample example = new SysRolePermissionExample();
        SysRolePermissionExample.Criteria criteria = example.createCriteria();
        criteria.andSysRoleIdEqualTo(roleId);
        rolePermissionMapper.deleteByExample(example);
        //重新创建角色权限关系
        for (Integer pid : permissionIds) {
            SysRolePermission rolePermission = new SysRolePermission();
            String uuid = UUID.randomUUID().toString();
            rolePermission.setId(uuid);
            rolePermission.setSysRoleId(roleId);
            rolePermission.setSysPermissionId(pid.toString());

            rolePermissionMapper.insert(rolePermission);
        }
    }

    @Override
    public List<SysPermission> findPermissionListByUserId(String userid)
            throws Exception {
        return sysPermissionMapperCustom.findPermissionListByUserId(userid);
    }
}
