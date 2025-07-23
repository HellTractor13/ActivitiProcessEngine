# 使用activities引擎完成简单流程图部署
Activiti流程引擎原理
一、	什么是 BPM 和 Activiti？
	BPM（Business Process Management）: 是一个管理概念和方法，旨在通过优化和自动化业务流程，提高组织效率。BPM 不仅关注技术层面，还强调流程建模、监控及改进。

	Activiti工作流引擎: 是一个轻量级的开源工作流引擎，专注于 BPMN（业务流程模型与符号）。它提供了强大的流程建模、执行和监控功能，适用于复杂的业务流程。

	两者都基于BPMN 2.0标准（业务流程模型与符号）建模，确保业务人员与技术人员使用同一套可视化语言沟通。
二、	BPM 与 Activiti 的区别与联系
区别：
特性	BPM	Activiti
关注点	流程管理的整体方法	具体的工作流引擎，仅负责流程的自动化执行
技术深度	包含多个层面（建模、执行、优化等）	专注于流程执行，提供 API 接口
兼容性	可与多种软件系统集成	主要与 Java 应用和 Spring 框架结合
联系：
BPM = 流程设计 + 流程引擎 + 监控优化
BPM依赖流程引擎，流程引擎是BPM的技术核心：BPM方法论中的“执行”（Execute）阶段依赖流程引擎驱动流程运行（如任务分配、节点跳转、规则判断）。


三、	Activiti的数据存储
在properties文件中配置
 
Activiti引擎会自动创建25张表
 
Activiti使用到的表都是ACT_开头的。
表开头	说明
ACT_RE_*	RE表示repository(存储)，RepositoryService接口所操作的表。带此前缀的表包含的是静态信息，如，流程定义，流程的资源（图片，规则等）
ACT_RU_*	RU表示runtime，运行时表,RuntimeService接口所操作的表。这是运行时的表 存储着流程变量，用户任务，变量，职责（job）等运行时的数据。Activiti只存储实例执行期间的运行时数据，当流程实例结束时，将删除这些记录。 这就保证了这些运行时的表小且快
ACT_ID_*	ID表示identity (组织机构)，IdentityService接口所操作的表。用户记录，流程中使用到的用户和组。这些表包含标识的信息，如用户，用户组，等等
ACT_HI_*	HI表示history，历史数据表，HistoryService接口所操作的表。就是这些表包含着流程执行的历史相关数据，如结束的流程实例，变量，任务，等等
ACT_GE_*	全局通用数据及设置(general)，各种情况都使用的数据

四、	流程定义
绘制的流程图导出为符合BPMN2.0标准的.xml文件，流程引擎会自动解析该文件，识别流程节点，自动分配任务。
流程图中的员工提交申请节点定义如下：
<!-- 员工提交申请节点 -->
<userTask id="employeeApplyTask" name="提交请假申请"
          activiti:assignee="${employee}">
    <extensionElements>
        <activiti:formProperty id="leaveType" name="请假类型" type="enum" required="true">
            <activiti:value id="sick" name="病假"/>
            <activiti:value id="annual" name="年假"/>
            <activiti:value id="personal" name="事假"/>
        </activiti:formProperty>
<!—新增性别字段 类型为枚举 -->
        <activiti:formProperty id="gender" name="性别" type="enum" required="true">
            <activiti:value id="male" name="男"/>
            <activiti:value id="female" name="女"/>
        </activiti:formProperty>

        <activiti:formProperty id="age" name="年龄" type="integer" required="true"/>
        <activiti:formProperty id="phone" name="手机号码" type="string" required="true"/>
        <activiti:formProperty id="startDate" name="开始日期" type="date" required="true"/>
        <activiti:formProperty id="endDate" name="结束日期" type="date" required="true"/>
        <activiti:formProperty id="reason" name="请假原因" type="string"/>
    </extensionElements>
</userTask>
<sequenceFlow sourceRef="employeeApplyTask" targetRef="managerApprovalTask"/>
< userTask >标签表明该节点为任务节点
<activiti:formProperty>表示在流程中内嵌表单，内嵌表单的定义和流程的定义都绑定在xml文件里。
五、	流程部署
流程首次部署时，引擎会在act_ge_bytearray存储原始文件（包括流程图XML和图片），将解析原始文件生成的流程定义数据写入act_re_procdef。
内嵌表单是和流程一起被定义且存储的，在数据库的act_ge_bytearray表的byte_字段中，为二进制文件。如果想要查看表单的定义，需要把这个字段的二进制文件转换成UTF8。
想要添加字段，则直接在<extensionElements>中添加属性，比如添加性别属性：
<activiti:formProperty id="gender" name="性别" type="string"/>。
这个时候再去数据库中解析二进制文件，查到性别属性，添加成功。
证明了内嵌表单与流程一起定义，想要添加字段直接在流程定义文件中添加即可。
 

这也是使用流程引擎的意义，activiti提供了标准api，避免对数据库直接进行操作，标准为：
1、绝对应该使用 Activiti 提供的 API 进行前后端交互避免直接操作数据库，除非进行特定的审计或报表查询
2、新增表单字段只需修改 BPMN 文件并重新部署
3、使用 FormService 和 TaskService 处理表单渲染和提交
4、通过流程变量自动持久化表单数据

六、	发起流程（启动流程实例）
填入员工和领导的id后，即可正式发起一个具体的流程实例，名为“请假申请流程”
 
正在运行的流程实例存储在ACT_RU_EXECUTION表中
 
 
可以看到发起流程实例后，ACT_RU_EXECUTION表中新增了一条记录，其中字段PROC_DEF_ID_（流程定义ID）的数据和流程定义阶段查出的最新流程定义的PROC_DEF_ID_（流程定义ID）的值是一样的。
证明了该流程实例是根据最新定义的流程图来运行的。
PROC_INST_ID_ 就是指流程实例ID，确定流程实例的唯一标识。

根据流程定义，流程发起后会自动分配给员工“提交请假申请”的任务，而领导此时没有任务
 
 
可以看到员工“e0”已经被分配了任务，领导“m0”在员工成功提交申请前没有任务。而且员工任务中的PROCESS_INSTANCE_ID(流程实例ID)就为流程启动时，在execution表查出的流程实例ID。
查询结果符合预期
证明了请假申请流程发起后任务的成功分配

在员工任务提交请假申请中提交表单
 

运行中填写的表单数据保存在ACT_RU_VARIABLE（运行时表）中；
提交表单前查询的流程变量：
 
提交表单后查询的流程变量：可以看到ACT_RU_VARIABLE表中新增了刚才在页面中填写的数据，证明了流程引擎会自动存储表单数据，以及存储的位置
 
此外，可以看到同样的数据出现了两次，有一个绑定了TASK_ID，另一个没有。这是因为当任务分配好后，在任务中使用的变量成为任务变量，任务变量只能在当前任务中使用，为了后续节点能够收到当前任务的数据，使用api：TaskService 中的complete方法完成任务后，activiti流程引擎会自动把传入complete方法的变量升为流程变量（全局变量），流程变量在整个流程中共享。
同时，由于员工提交申请表后该任务已结束，按照流程定义会进入领导审批任务
 
此时可以查到领导“m0”已经被分配了任务，并且变量中的TASK_ID就为当前任务的TASK_ID。

流程结束后，流程变量会归档到ACT_HI_VARINST（历史表）中，此时再查运行流程表会发现数据已经删除了。

 
领导审批，员工确认后，流程结束，已经结束的流程保存在ACT_HI_PROCINST表中，使用过的变量也将从ACT_RU_VARIABLE 中删除，全部记录在ACT_HI_VARINST中。
 
 
 
证明了流程结束后，activiti会自动删除运行时（ACT_RU_*）流程数据。这就保证了这些运行时的表小且快。
