package com.zj.oa.service;

import com.zj.oa.pojo.BaoxiaoBill;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface WorkFlowService {
    void saveNewDeploy(InputStream in,String fileName);//部署流程定义
    List<Deployment> findDeploymentList();//1:查询部署对象信息
    List<ProcessDefinition> findProcessDefinitionList();//2:查询流程定义的信息
    void saveStartProcess(long baoxiaoId,String username);//启动流程
    List<Task> findTaskListByName(String username);//查看个人任务
    BaoxiaoBill findBaoxiaoBillByTaskId(String taskId);//查询出当前任务办理信息
    List<Comment> findCommentByTaskId(String taskId);//查询出办理的批注信息
    List<String> findOutComeListByTaskId(String taskId);//查询出所有的批注
    void saveSubmitTask(long id, String taskId, String comment, String outcome, String username);//完成当前任务
    ProcessDefinition findProcessDefinitionByTaskId(String taskId);//使用任务对象获取流程定义id，查询流程定义对象
    Map<String,Object> findCoordingByTask(String taskId);///**二：查看当前活动，获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中*/
    InputStream findImageInputStream(String deploymentId, String imageName);//获取资源文件表（act_ge_bytearray）中资源图片输入流InputStream
    List<Comment> findCommentByBaoxiaoBillId(long id);//查询历史批注信息
    void deleteProcessDefinitionByDeploymentId(String deploymentId);
    Task findTaskByBussinessKey(String bussiness_key);
}
