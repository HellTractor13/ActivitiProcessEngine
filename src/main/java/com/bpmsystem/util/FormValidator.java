package com.bpmsystem.util;

import java.util.Map;

public class FormValidator {

    public static void validateTaskForm(String taskDefinitionKey, Map<String, Object> formData) {
        switch (taskDefinitionKey) {
            case "employeeApplyTask":
                validateField(formData, "leaveType", String.class);
                validateField(formData, "gender", String.class);
                validateField(formData, "startDate", String.class);
                validateField(formData, "endDate", String.class);
                break;
            case "managerApprovalTask":
                validateField(formData, "approvalStatus", String.class);
                break;
            case "employeeConfirmTask":
                validateField(formData, "confirmationStatus", String.class);
                break;
            default:
                throw new IllegalArgumentException("未知的任务类型: " + taskDefinitionKey);
        }
    }

    private static void validateField(Map<String, Object> formData, String fieldName, Class<?> type) {
        if (!formData.containsKey(fieldName)) {
            throw new IllegalArgumentException("缺少必要字段: " + fieldName);
        }
        if (!type.isInstance(formData.get(fieldName))) {
            throw new IllegalArgumentException(fieldName + " 字段类型错误");
        }
    }
}