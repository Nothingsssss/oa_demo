package com.zj.oa.contorller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import com.zj.oa.pojo.*;
import com.zj.oa.service.SysUserRoleService;
import com.zj.oa.service.employeeService;
import com.zj.oa.service.sysService;
import com.zj.oa.utils.objToMap;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

@Controller
public class UserController {

    @Autowired
    private sysService sysService;
    @Autowired
    private employeeService employeeService;
    @Autowired
    private SysUserRoleService sysUserRoleService;


    @RequestMapping("/login")
    public String login(HttpServletRequest request, Model model){
        String exceptionName = (String) request.getAttribute("shiroLoginFailure");
        if(exceptionName != null){
            if(UnknownAccountException.class.getName().equals(exceptionName)){
                model.addAttribute("errorMsg","用户账号不存在！");
            }else if(IncorrectCredentialsException.class.getName().equals(exceptionName)){
                model.addAttribute("errorMsg","密码不正确！");
            }else if("InValidateCode".equals(exceptionName)){
                model.addAttribute("errorMsg","验证码错误！");
            }else{
                model.addAttribute("errorMsg","未知错误！");
            }
        }
        return "login";
    }

    @RequestMapping("/main")
    public String main(ModelMap modelMap){
        Subject subject = SecurityUtils.getSubject();
        ActiveUser activeUser = (ActiveUser) subject.getPrincipal();
        modelMap.addAttribute("activeUser",activeUser);
        return "index";
    }

    @RequestMapping("/main2")
    @ResponseBody
    public ActiveUser main2(ActiveUser activeUser){
        ActiveUser activeUser1 = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        activeUser.setUsername(activeUser1.getUsername());
        return  activeUser;
    }
    //将验证码放入登陆界面
    @RequestMapping("/checkCode")
    public void checkCode(HttpServletRequest request,HttpServletResponse response){
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(200, 100,4,8);
        String code = captcha.getCode();
        //将验证码存入session中
        HttpSession session = request.getSession();
        session.setAttribute("vrifyCode",code);
        try {
            captcha.write(response.getOutputStream());
            response.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //用户管理界面
    @RequestMapping("/findUserList")
    public ModelAndView findUserList(){
        ModelAndView mv = new ModelAndView();
        List<SysRole> allRoles = sysService.findAllRoles();
        List<EmployeeCustom> list = employeeService.findUserAndRoleList();
//        List<SysRole2> allRoles = new ArrayList<>();
//        for (SysRole sysRole : allRoles1) {
//            SysRole2 sysRole2 = new SysRole2();
//            String UUID = sysRole.getId();
//            Integer userId=UUID.toString().hashCode();
//            userId= userId< 0 ? -userId: userId;   //String.hashCode()可能会是负数，如果为负数需要转换为正数
//            sysRole2.setId(Long.valueOf(String.valueOf(userId)));
//            sysRole2.setAvailable(sysRole.getAvailable());
//            sysRole2.setName(sysRole.getName());
//            sysRole2.setPermissionList(sysRole.getPermissionList());
//            allRoles.add(sysRole2);
//        }
        mv.addObject("userList", list);
        mv.addObject("allRoles", allRoles);

        mv.setViewName("userlist");
        return mv;
    }

    //查看当前员工的角色和权限列表
    @RequestMapping("/viewPermissionByUser")
    @ResponseBody
    public SysRole viewPermissionByUser(String userName) {
        SysRole sysRole = sysService.findRolesAndPermissionsByUserId(userName);

        System.out.println(sysRole.getName()+"," +sysRole.getPermissionList());
        return sysRole;
    }

    //重新分配待办人
    @RequestMapping("/assignRole")
    @ResponseBody
    public Map<String, String> assignRole(String roleId, String userId) {
        Map<String, String> map = new HashMap<>();
        try {
            employeeService.updateEmployeeRole(roleId, userId);
            map.put("msg", "分配权限成功");
        } catch (Exception e) {
            e.printStackTrace();
            map.put("msg", "分配权限失败");
        }
        return map;
    }

    //根据员工级别查找下一级别主管
    @RequestMapping("/findNextManager")
    @ResponseBody
    public List<Employee> findNextManager(int level) {
        level++; //加一，表示下一个级别
        List<Employee> list = employeeService.findEmployeeByLevel(level);
        if(list.isEmpty()){
            list.add(employeeService.findEmployee("mike"));
        }
        return list;

    }


    //添加用户
    @RequestMapping("/saveUser")
    public String saveUser(Employee user, Model model) {
        //判断邮箱是否已经存在
        boolean flag = this.employeeService.findEamil(user.getEmail());
        if(flag){
            model.addAttribute("msg","邮箱已经存在！");
            return "forward:/findUserList";
        }
        //判断其他属性是否为空
        if(user.getName().equals("") || user.getPassword().equals("") || user.getEmail().equals("")){
            model.addAttribute("msg","请完整填写资料！");
            return "forward:/findUserList";
        }
        //判断是否有上级id
        if(user.getManagerId() == null){
            model.addAttribute("msg","请输入上级id");
            return "forward:/findUserList";
        }
        String salt = "eteokues";
        user.setSalt(salt);
        Md5Hash password = new Md5Hash(user.getPassword(),salt,2);
        user.setPassword(password.toString());
        this.employeeService.saveUser(user);

        //通过user.name 获取到新插入的id
        Employee employee = this.employeeService.findEmployee(user.getName());

        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setId(employee.getId()+"");
        sysUserRole.setSysUserId(user.getName());
        sysUserRole.setSysRoleId(user.getRole()+"");
        int a = this.sysUserRoleService.saveSysUserRole(sysUserRole);


        model.addAttribute("msg",a>0?"添加成功!":"添加失败！");
        return "forward:/findUserList";
    }

    //角色添加
    @RequestMapping("/toAddRole")
    public ModelAndView toAddRole() {
        List<MenuTree> allPermissions = sysService.loadMenuTree();
        List<SysPermission> menus = sysService.findAllMenus();
        List<SysRole> permissionList = sysService.findRolesAndPermissions();

        ModelAndView mv = new ModelAndView();
        mv.addObject("allPermissions", allPermissions);
        mv.addObject("menuTypes", menus);
        mv.addObject("roleAndPermissionsList", permissionList);
        mv.setViewName("rolelist");

        return mv;

    }

    //保存角色和权限
    @RequestMapping("/saveRoleAndPermissions")
    public String saveRoleAndPermissions(SysRole role,int[] permissionIds,Model model) {

        if (role.getName().equals("")){
            model.addAttribute("msg","请设置角色昵称！");
            return "forward:/toAddRole";
        }

        if (permissionIds == null){
            model.addAttribute("msg","请设置权限！");
            return "forward:/toAddRole";
        }

        //设置role主键，使用uuid
       String uuid = UUID.randomUUID().toString();
        Integer userId=uuid.toString().hashCode();
        userId= userId< 0 ? -userId: userId;   //String.hashCode()可能会是负数，如果为负数需要转换为正数
        role.setId(String.valueOf(userId));
        //默认可用
        role.setAvailable("1");

        sysService.addRoleAndPermissions(role, permissionIds);
        model.addAttribute("msg","添加成功！");
        return "forward:/toAddRole";
    }
    //新建权限
    @RequestMapping("/saveSubmitPermission")
    public String saveSubmitPermission(SysPermission permission) {
        if (permission.getAvailable() == null) {
            permission.setAvailable("0");
        }
        sysService.addSysPermission(permission);
        return "redirect:/toAddRole";
    }

    //删除权限
    @RequestMapping("/deletePermission")
    public String deletePermission(long[] permissionIds){
        for (long permissionId : permissionIds) {
            sysService.deletePermission(permissionId);
        }
        return "redirect:/toAddRole";
    }

    //角色列表
    @RequestMapping("/findRoles")  //rest
    public ModelAndView findRoles() {
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        List<SysRole> roles = sysService.findAllRoles();
        List<MenuTree> allMenuAndPermissions = sysService.getAllMenuAndPermision();

        ModelAndView mv = new ModelAndView();
        mv.addObject("allRoles", roles);
        mv.addObject("activeUser",activeUser);
        mv.addObject("allMenuAndPermissions", allMenuAndPermissions);

        mv.setViewName("permissionlist");
        return mv;
    }
    //角色列表 -> 编辑
    @RequestMapping("/loadMyPermissions")
    @ResponseBody
    public List<SysPermission> loadMyPermissions(String roleId) {
        List<SysPermission> list = sysService.findPermissionsByRoleId(roleId);

        for (SysPermission sysPermission : list) {
            System.out.println(sysPermission.getId()+","+sysPermission.getType()+"\n"+sysPermission.getName() + "," + sysPermission.getUrl()+","+sysPermission.getPercode());
        }
        return list;
    }

    //删除角色
    @RequestMapping("/deleteRole/{id}")
    public String deleteRole(@PathVariable(value = "id")String id){
        sysService.deleteRoles(id);
        return "redirect:/findRoles";
    }

    //更新保存角色的权限
    @RequestMapping("/updateRoleAndPermission")
    public String updateRoleAndPermission(String roleId,int[] permissionIds) {
        sysService.updateRoleAndPermissions(roleId, permissionIds);
        return "redirect:/findRoles";
    }
}
