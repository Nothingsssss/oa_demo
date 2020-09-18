package com.zj.oa.mapper;



import com.zj.oa.pojo.EmployeeCustom;
import com.zj.oa.pojo.MenuTree;
import com.zj.oa.pojo.SysPermission;
import com.zj.oa.pojo.SysRole;

import java.util.List;


public interface SysPermissionMapperCustom {
	
	//根据用户id查询菜单
	public List<SysPermission> findMenuListByUserId(String userid)throws Exception;
	//根据用户id查询权限url
	public List<SysPermission> findPermissionListByUserId(String userid)throws Exception;

	public List<SysPermission> findMenuList888(String userid)throws Exception;

	public List<MenuTree> getMenuTree(String userid);

	public List<MenuTree> getMenuTree1();

	public List<SysPermission> getSubMenu();
	
	public List<EmployeeCustom> findUserAndRoleList();
	
	public SysRole findRoleAndPermissionListByUserId(String userId);
	
	public List<SysRole> findRoleAndPermissionList();
	
	public List<SysPermission> findMenuAndPermissionByUserId(String userId);
	
	public List<MenuTree> getAllMenuAndPermision();
	
	public List<SysPermission> findPermissionsByRoleId(String roleId);
}
