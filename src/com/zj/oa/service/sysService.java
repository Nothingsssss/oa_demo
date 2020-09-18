package com.zj.oa.service;

import com.zj.oa.pojo.MenuTree;
import com.zj.oa.pojo.SysPermission;
import com.zj.oa.pojo.SysRole;

import java.util.List;

public interface sysService {
    List<SysRole> findAllRoles();

    SysRole findRolesAndPermissionsByUserId(String userId);

    List<MenuTree> loadMenuTree();

    List<SysPermission> findAllMenus();

    List<SysRole> findRolesAndPermissions();

    void addRoleAndPermissions(SysRole role, int[] permissionIds);

    void addSysPermission(SysPermission permission);
    void deletePermission(long id);

    List<MenuTree> getAllMenuAndPermision();

    List<SysPermission> findPermissionsByRoleId(String roleId);

    void deleteRoles(String id);

    void updateRoleAndPermissions(String roleId, int[] permissionIds);

    public List<SysPermission> findPermissionListByUserId(String userid) throws Exception;
}
