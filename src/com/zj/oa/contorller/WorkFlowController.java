package com.zj.oa.contorller;

import com.zj.oa.pojo.ActiveUser;
import com.zj.oa.pojo.BaoxiaoBill;
import com.zj.oa.service.BaoxiaoBillService;
import com.zj.oa.service.WorkFlowService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class WorkFlowController {

    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    private BaoxiaoBillService baoxiaoBillService;

    //部署流程定义
    @RequestMapping("/deployProcess")
    public String deployProcess(MultipartFile fileName, String processName, Model model){
        //判断上传的文件是否是zip文件
        if(!fileName.getContentType().equals("application/x-zip-compressed")){
            model.addAttribute("msg","文件上传格式错误！请上传zip压缩文件！");
            return "forward:add_process.jsp";
        }

        try {
            this.workFlowService.saveNewDeploy(fileName.getInputStream(),processName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/processDefinitionList";

    }

    //查看流程
    @RequestMapping("/processDefinitionList")
    public String processDefinitionList(Model model){

        //1:查询部署对象信息，对应表（act_re_deployment）
        List<Deployment> deploymentList = workFlowService.findDeploymentList();
        //2:查询流程定义的信息，对应表（act_re_procdef）
        List<ProcessDefinition> processDefinitionList = workFlowService.findProcessDefinitionList();
        //将数据返回给页面
        model.addAttribute("depList", deploymentList);
        model.addAttribute("pdList", processDefinitionList);
        return "workflow_list";
    }

    //保存报销单并启动流程
    @RequestMapping("/saveStartBaoxiao")
    public String saveStartBaoxiao(BaoxiaoBill baoxiaoBill, Model model){
        //判断输入是否合法
        if(baoxiaoBill.getTitle().equals("") || baoxiaoBill.getRemark().equals("") || baoxiaoBill.getMoney()==null){
            model.addAttribute("msg","请完整填写报销申请表！");
            return "apply_baoxiao";
        }
        if(baoxiaoBill.getMoney()>100000){
            model.addAttribute("msg","金额过大，请找BOOS当面申请！");
            return "apply_baoxiao";
        }
        baoxiaoBill.setCreatdate(new Date());
        Subject subject = SecurityUtils.getSubject();
        ActiveUser principal = (ActiveUser) subject.getPrincipal();
        baoxiaoBill.setUserId(principal.getId());
        baoxiaoBill.setState(1);
        //保存请假信息
        baoxiaoBillService.saveBaoxiaoBill(baoxiaoBill);
        //启动流程
        workFlowService.saveStartProcess(baoxiaoBill.getId(),principal.getUsername());

        return "redirect:/myTaskList";

    }

    //展示我的待办任务
    @RequestMapping("/myTaskList")
    public String myTaskList(Model model){
        ActiveUser principal = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        List<Task> list = workFlowService.findTaskListByName(principal.getUsername());
        model.addAttribute("taskList",list);
        return "workflow_task";
    }

    //展示办理任务界面
    @RequestMapping("/viewTaskForm")
    public String viewTaskForm(String taskId,Model model){
        BaoxiaoBill bill = workFlowService.findBaoxiaoBillByTaskId(taskId);
        List<Comment> comlist = workFlowService.findCommentByTaskId(taskId);
        List<String> outcomeList = this.workFlowService.findOutComeListByTaskId(taskId);
        model.addAttribute("baoxiaoBill",bill);
        model.addAttribute("commentList",comlist);
        model.addAttribute("outcomeList",outcomeList);
        model.addAttribute("taskId",taskId);
        return "approve_baoxiao";
    }

    //办理任务，推进流程
    @RequestMapping("/submitTask")
    public String submitTask(long id,String taskId,String comment,String outcome){
        ActiveUser principal = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        String username = principal.getUsername();
        this.workFlowService.saveSubmitTask(id, taskId, comment, outcome, username);
        return "redirect:/myTaskList";
    }

    //查看当前流程图
    @RequestMapping("/viewCurrentImage")
    public String viewCurrentImage(String taskId, Model model){
        //获取任务id，获取任务对象，使用任务对象获取流程定义id，查询流程定义对象
        ProcessDefinition pd = workFlowService.findProcessDefinitionByTaskId(taskId);
        model.addAttribute("deploymentId",pd.getDeploymentId());
        model.addAttribute("imageName",pd.getDiagramResourceName());
        /**二：查看当前活动，获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中*/
        Map<String, Object> map = workFlowService.findCoordingByTask(taskId);
        model.addAttribute("acs", map);
        return "viewimage";
    }

    //查看流程图
    @RequestMapping("/viewImage")
    public String viewImage(String deploymentId,String imageName,HttpServletResponse response) throws Exception {
        //获取资源文件表（act_ge_bytearray）中资源图片输入流InputStream
        InputStream in = workFlowService.findImageInputStream(deploymentId,imageName);
        //从response对象获取输出流
        OutputStream out = response.getOutputStream();
        //将输入流中的数据读取出来，写到输出流中
        for(int b = -1;(b=in.read())!= -1;){
            out.write(b);

        }
        in.close();
        out.close();
        return  null;
    }

    // 查看历史的批注信息
    @RequestMapping("/viewHisComment")
    public String viewHisComment(long id,ModelMap model){
        //1：使用报销单ID，查询报销单对象
        BaoxiaoBill bill = baoxiaoBillService.findBaoxiaoBillById(id);
        model.addAttribute("baoxiaoBill", bill);
        //2：使用请假单ID，查询历史的批注信息
        List<Comment> commentList = workFlowService.findCommentByBaoxiaoBillId(id);
        model.addAttribute("commentList", commentList);

        return "workflow_commentlist";
    }

    /**
     * 删除部署信息
     */
    @RequestMapping("/delDeployment")
    public String delDeployment(String deploymentId){
        //使用部署对象ID，删除流程定义
        workFlowService.deleteProcessDefinitionByDeploymentId(deploymentId);
        return "redirect:/processDefinitionList";
    }

}
