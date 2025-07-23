package com.bpmsystem.controller;

import com.bpmsystem.service.LeaveProcessService;
import com.bpmsystem.util.FormValidator;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leave")
public class LeaveProcessController {

    @Autowired
    private LeaveProcessService leaveProcessService;

    @PostMapping("/start")
    public ResponseEntity<?> startLeaveProcess(
            @RequestParam String employeeId,
            @RequestParam String managerId) {

        ProcessInstance instance = leaveProcessService.startProcess(employeeId, managerId);
        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceId", instance.getId());
        response.put("message", "请假流程已启动");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getUserTasks(@RequestParam String userId) {
        List<Task> tasks = leaveProcessService.getUserTasks(userId);

        List<Map<String, Object>> response = tasks.stream().map(task -> {
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("taskId", task.getId());
            taskInfo.put("taskName", task.getName());
            taskInfo.put("createTime", task.getCreateTime());
            taskInfo.put("assignee", task.getAssignee());
            return taskInfo;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeTask(
            @RequestParam String taskId,
            @RequestBody Map<String, Object> formData) {

        try {
            Task task = leaveProcessService.getTaskById(taskId);
            FormValidator.validateTaskForm(task.getTaskDefinitionKey(), formData);
            leaveProcessService.completeTask(taskId, formData);
            return ResponseEntity.ok("任务已完成");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("处理失败");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getProcessStatus(@RequestParam String processInstanceId) {
        ProcessInstance instance = leaveProcessService.getProcessInstance(processInstanceId);

        if (instance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("流程实例不存在");
        }

        Map<String, Object> variables = leaveProcessService.getProcessVariables(processInstanceId);

        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("currentActivity", instance.getActivityId());
        statusResponse.put("startTime", instance.getStartTime());
        statusResponse.put("variables", variables);

        return ResponseEntity.ok(statusResponse);
    }

    @GetMapping("/task/form")
    public ResponseEntity<?> getTaskForm(@RequestParam String taskId) {
        Map<String, Object> formData = leaveProcessService.getTaskFormData(taskId);
        return ResponseEntity.ok(formData);
    }

    @GetMapping("/task/variables")
    public ResponseEntity<?> getTaskVariables(@RequestParam String taskId) {
        Map<String, Object> variables = leaveProcessService.getTaskVariables(taskId);
        return ResponseEntity.ok(variables);
    }

    @GetMapping("/manager/tasks/count")
    public ResponseEntity<?> countProcessedTasksByManager(@RequestParam String managerId) {
        long count = leaveProcessService.countProcessedTasksByManager(managerId);
        return ResponseEntity.ok(count);
    }
}