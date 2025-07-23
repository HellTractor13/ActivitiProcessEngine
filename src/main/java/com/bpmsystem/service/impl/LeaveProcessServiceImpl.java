package com.bpmsystem.service.impl;

import com.bpmsystem.service.LeaveProcessService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LeaveProcessServiceImpl implements LeaveProcessService {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Override
    public ProcessInstance startProcess(String employeeId, String managerId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("employee", employeeId);
        variables.put("manager", managerId);
        return runtimeService.startProcessInstanceByKey("leaveProcess", variables);
    }

    @Override
    public List<Task> getUserTasks(String userId) {
        return taskService.createTaskQuery()
                .taskAssignee(userId)
                .list();
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String taskDefinitionKey = task.getTaskDefinitionKey();
        String processInstanceId = task.getProcessInstanceId();

        if ("managerApprovalTask".equals(taskDefinitionKey)) {
            String approvalStatus = (String) variables.get("approvalStatus");
            if ("批准".equals(approvalStatus)) {
                variables.put("approvalStatus", "approved");
            } else if ("拒绝".equals(approvalStatus)) {
                variables.put("approvalStatus", "rejected");
            }
        }

        taskService.complete(taskId, variables);
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    @Override
    public Task getTaskById(String taskId) {
        return taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
    }

    @Override
    public Map<String, Object> getTaskFormData(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String taskDefinitionKey = task.getTaskDefinitionKey();
        Map<String, Object> formData = new HashMap<>();

        if ("employeeApplyTask".equals(taskDefinitionKey)) {
            formData.put("leaveType", createEnumField("请假类型", Arrays.asList("病假", "年假", "事假")));
            formData.put("gender", createEnumField("性别", Arrays.asList("男", "女")));
            formData.put("startDate", createField("开始日期", "date"));
            formData.put("endDate", createField("结束日期", "date"));
            formData.put("reason", createField("请假原因", "string"));
        } else if ("managerApprovalTask".equals(taskDefinitionKey)) {
            formData.put("approvalStatus", createEnumField("审批结果", Arrays.asList("批准", "拒绝")));
            formData.put("comments", createField("审批意见", "string"));
        } else if ("employeeConfirmTask".equals(taskDefinitionKey)) {
            formData.put("confirmationStatus", createEnumField("确认审批结果", Collections.singletonList("确认")));
        }
        return formData;
    }

    private Map<String, Object> createField(String name, String type) {
        Map<String, Object> field = new HashMap<>();
        field.put("name", name);
        field.put("type", type);
        return field;
    }

    private Map<String, Object> createEnumField(String name, List<String> values) {
        Map<String, Object> field = createField(name, "enum");
        field.put("values", values);
        return field;
    }

    @Override
    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    @Override
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    @Override
    public long countProcessedTasksByManager(String managerId) {
        return historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(managerId)
                .taskDefinitionKey("managerApprovalTask")
                .finished()
                .count();
    }
}