package com.zj.oa.service.impl;

import com.zj.oa.mapper.SysPermissionMapperCustom;
import com.zj.oa.pojo.MenuTree;
import com.zj.oa.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysPermissionServiceImpl implements SysPermissionService {

    @Autowired
    SysPermissionMapperCustom sysPermissionMapperCustom;

    @Override
    public List<MenuTree> loadMenuThree(String userId) throws Exception {
//        return sysPermissionMapperCustom.getMenuTree();
        return sysPermissionMapperCustom.getMenuTree(userId);
    }
}
