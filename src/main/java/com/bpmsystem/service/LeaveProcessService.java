package com.bpmsystem.service;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.util.List;
import java.util.Map;

public interface LeaveProcessService {
    // 启动请假流程（需要员工ID和领导ID）
    ProcessInstance startProcess(String employeeId, String managerId);

    // 获取用户任务
    List<Task> getUserTasks(String userId);

    // 完成任务（需要任务ID和表单数据）
    void completeTask(String taskId, Map<String, Object> variables);

    // 获取流程实例
    ProcessInstance getProcessInstance(String processInstanceId);

    // 获取任务表单数据
    Map<String, Object> getTaskFormData(String taskId);

    // 获取任务变量
    Map<String, Object> getTaskVariables(String taskId);

    // 获取任务
    Task getTaskById(String taskId);

    Map<String, Object> getProcessVariables(String processInstanceId);

    long countProcessedTasksByManager(String managerId);
}