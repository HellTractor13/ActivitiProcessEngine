package com.bpmsystem;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class EmbeddedFormReader {

    public static void main(String[] args) throws Exception {
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();

        // 1. 获取流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("leaveProcess")
                .latestVersion()
                .singleResult();

        // 2. 读取 BPMN XML
        try (InputStream bpmnStream = repositoryService.getResourceAsStream(
                processDefinition.getDeploymentId(),
                "leaveProcess.bpmn20.xml")) {

            // 3. 解析 XML 获取表单
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(bpmnStream);

            // 获取特定任务节点（示例：employeeApplyTask）
            String taskId = "employeeApplyTask";
            Element userTask = (Element) doc.getElementsByTagNameNS(
                            "http://www.omg.org/spec/BPMN/20100524/MODEL",
                            "userTask")
                    .item(0); // 简化处理，实际需遍历匹配 id

            // 4. 提取表单属性
            NodeList formProperties = userTask.getElementsByTagNameNS(
                    "http://activiti.org/bpmn",
                    "formProperty");

            for (int i = 0; i < formProperties.getLength(); i++) {
                Element prop = (Element) formProperties.item(i);
                String id = prop.getAttribute("id");
                String name = prop.getAttribute("name");
                String type = prop.getAttribute("type");
                System.out.println("表单字段: " + id + " | " + name + " | " + type);
            }
        }
    }
}