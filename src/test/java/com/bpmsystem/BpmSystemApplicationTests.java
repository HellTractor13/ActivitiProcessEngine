package com.bpmsystem;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BpmSystemApplicationTests {

    @Test
    /**
     * 使用activiti 提供的默认方式来创建数据库表：
     * ***/
    public void testCreateDbTable() {
        //他会去读取 resources/activiti.cfg.xml 文件来创建表
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        System.out.println(processEngine);
    }

}
