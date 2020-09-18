package com.zj.oa.service;

import com.zj.oa.pojo.MenuTree;

import java.util.List;

public interface SysPermissionService {
    List<MenuTree> loadMenuThree(String userId) throws Exception;
}
