package com.zj.oa.service.impl;

import com.zj.oa.mapper.SysUserRoleMapper;
import com.zj.oa.pojo.SysUserRole;
import com.zj.oa.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserRoleServiceImpl implements SysUserRoleService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public int saveSysUserRole(SysUserRole sysUserRole) {
        int i = sysUserRoleMapper.insert(sysUserRole);
        return i;
    }
}
