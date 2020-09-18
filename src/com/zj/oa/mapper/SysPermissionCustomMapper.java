package com.zj.oa.mapper;



import com.zj.oa.pojo.SysPermission;
import com.zj.oa.pojo.TreeMenu;

import java.util.List;

public interface SysPermissionCustomMapper {

	
	public List<TreeMenu> getTreeMenu();
	
	public List<SysPermission> getSubMenu(int id);
}
