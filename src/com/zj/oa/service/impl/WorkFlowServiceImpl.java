package com.zj.oa.service.impl;

import com.zj.oa.mapper.BaoxiaoBillMapper;
import com.zj.oa.pojo.BaoxiaoBill;
import com.zj.oa.service.BaoxiaoBillService;
import com.zj.oa.service.WorkFlowService;
import com.zj.oa.utils.Constants;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@Service
public class WorkFlowServiceImpl implements WorkFlowService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private FormService formService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private BaoxiaoBillMapper baoxiaoBillMapper;


    //部署流程定义
    @Override
    public void saveNewDeploy(InputStream in, String fileName) {
        try {
            ZipInputStream zipInputStream = new ZipInputStream(in);
            repositoryService.createDeployment()
                    .name(fileName)
                    .addZipInputStream(zipInputStream)
                    .deploy();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public List<Deployment> findDeploymentList() {
        List<Deployment> list = repositoryService.createDeploymentQuery()
                .orderByDeploymenTime().desc()
                .list();
        return list;
    }

    @Override
    public List<ProcessDefinition> findProcessDefinitionList() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion().desc().list();
        return list;
    }

    @Override
    public void saveStartProcess(long baoxiaoId, String username) {
        //使用当前对象获取到流程定义的key（对象的名称就是流程定义的key）
        String key = Constants.BAOXIAO_KEY;

        Map<String, Object> variables = new HashMap<String,Object>();
        variables.put("inputUser", username);//表示惟一用户

        //格式：baoxiao.id的形式（使用流程变量）
        String objId = key+"."+baoxiaoId;
        variables.put("objId", objId);

        runtimeService.startProcessInstanceByKey(key,objId,variables);
    }

    @Override
    public List<Task> findTaskListByName(String username) {
        List<Task> list = this.taskService.createTaskQuery()
                .taskAssignee(username)
                .orderByTaskCreateTime().desc().list();
        return list;
    }

    @Override
    public BaoxiaoBill findBaoxiaoBillByTaskId(String taskId) {
        Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance pi = this.runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        String businessKey = pi.getBusinessKey();
        String id="";
        if(StringUtils.isNotBlank(businessKey)){
            id = businessKey.split("\\.")[1];
        }

        BaoxiaoBill bill = baoxiaoBillMapper.selectByPrimaryKey(Long.parseLong(id));

        return bill;
    }

    @Override
    public List<Comment> findCommentByTaskId(String taskId) {
        Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        List<Comment> list = this.taskService.getProcessInstanceComments(processInstanceId);
        return list;
    }

    @Override
    public List<String> findOutComeListByTaskId(String taskId) {
        //返回存放连线的名称集合
        List<String> list = new ArrayList<String>();
        //1:使用任务ID，查询任务对象
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //2：获取流程定义ID
        String processDefinitionId = task.getProcessDefinitionId();
        //3：查询ProcessDefinitionEntiy对象
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
        //使用任务对象Task获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        //获取当前活动的id
        String activityId = pi.getActivityId();
        //4：获取当前的活动
        ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);
        //5：获取当前活动完成之后连线的名称
        List<PvmTransition> pvmList = activityImpl.getOutgoingTransitions();
        if(pvmList!=null && pvmList.size()>0){
            for(PvmTransition pvm:pvmList){
                String name = (String) pvm.getProperty("name");
                if(StringUtils.isNotBlank(name)){
                    list.add(name);
                } else{
                    list.add("默认提交");
                }
            }
        }
        return list;
    }

    @Override
    public void saveSubmitTask(long id, String taskId, String comment, String outcome, String username) {
        //使用任务ID，查询任务对象，获取流程流程实例ID
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        /**
         * 注意：添加批注的时候，由于Activiti底层代码是使用：
         * 		String userId = Authentication.getAuthenticatedUserId();
         CommentEntity comment = new CommentEntity();
         comment.setUserId(userId);
         所有需要从Session中获取当前登录人，作为该任务的办理人（审核人），对应act_hi_comment表中的User_ID的字段，不过不添加审核人，该字段为null
         所以要求，添加配置执行使用Authentication.setAuthenticatedUserId();添加当前任务的审核人
         * */
        //加当前任务的审核人
        Authentication.setAuthenticatedUserId(username);
        //添加批注
        taskService.addComment(taskId, processInstanceId, comment);
        /**
         * 2：如果连线的名称是“默认提交”，那么就不需要设置，如果不是，就需要设置流程变量
         * 在完成任务之前，设置流程变量，按照连线的名称，去完成任务
         流程变量的名称：outcome
         流程变量的值：连线的名称
         */
        Map<String, Object> variables = new HashMap<String,Object>();
        if(outcome!=null && !outcome.equals("默认提交")){
            variables.put("message", outcome);
            //3：使用任务ID，完成当前人的个人任务，同时流程变量
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
        /**
         * 5：在完成任务之后，判断流程是否结束
         如果流程结束了，更新请假单表的状态从1变成2（审核中-->审核完成）
         */
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        //流程结束了
        if(pi==null){
            //更新请假单表的状态从1变成2（审核中-->审核完成）
            BaoxiaoBill bill = baoxiaoBillMapper.selectByPrimaryKey(id);
            bill.setState(2);
            baoxiaoBillMapper.updateByPrimaryKey(bill);
        }
    }

    @Override
    public ProcessDefinition findProcessDefinitionByTaskId(String taskId) {

        Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
        String pi = task.getProcessDefinitionId();
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(pi)
                .singleResult();
        return pd;
    }

    @Override
    public Map<String, Object> findCoordingByTask(String taskId) {
        //存放坐标
        Map<String,Object> map = new HashMap<String,Object>();
        //使用任务id，查询任务对象
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //获取流程定义id
        String pd = task.getProcessDefinitionId();
        //获取流程定义的实体对象
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(pd);
        //流程实例id
        String processInstanceId = task.getProcessInstanceId();
        //使用流程实例id查询正在执行的执行对象表，获取当前活动对应的流程实例对象
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        //获取当前活动的ID
        String activityId = pi.getActivityId();
        //获取当前活动对象
        ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);//活动ID
        map.put("x",activityImpl.getX());
        map.put("y",activityImpl.getY());
        map.put("width",activityImpl.getWidth());
        map.put("height",activityImpl.getHeight());


        return map;
    }

    @Override
    public InputStream findImageInputStream(String deploymentId, String imageName) {
        return repositoryService.getResourceAsStream(deploymentId,imageName);
    }

    @Override
    public List<Comment> findCommentByBaoxiaoBillId(long id) {
        String bussiness_key = Constants.BAOXIAO_KEY +"."+id;
        HistoricProcessInstance pi = this.historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(bussiness_key).singleResult();
        List<Comment> commentList = this.taskService.getProcessInstanceComments(pi.getId());

        return commentList;
    }

    @Override
    public void deleteProcessDefinitionByDeploymentId(String deploymentId) {
        this.repositoryService.deleteDeployment(deploymentId, true);
    }

    @Override
    public Task findTaskByBussinessKey(String bussiness_key) {
        Task task = this.taskService.createTaskQuery().processInstanceBusinessKey(bussiness_key).singleResult();
        return task;
    }

}
