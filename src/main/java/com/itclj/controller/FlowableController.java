package com.itclj.controller;

import com.itclj.delegate.SubProcessHelper;
import com.itclj.utils.R;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flowable")
public class FlowableController {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngine processEngine;

    @Resource
    private SubProcessHelper subProcessHelper;
    /**
     * 下单
     *
     * @param userId 用户id
     */
    @PostMapping("/purchaseOrder/{userId}/{purchaseOrderId}")
    public R purchaseOrder(@PathVariable String userId, @PathVariable String purchaseOrderId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("start", "PT24H");
        map.put("userId", userId);
        map.put("subProcessHelper", subProcessHelper);
        map.put("purchaseOrderId", purchaseOrderId);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process_pool3", map);
        String processId = processInstance.getId();
        String name = processInstance.getName();
        System.out.println(processId + ":" + name);
        return R.ok(processId + ":" + name);
    }

    /**
     * .提交采购订单的审批请求
     *
     * @param userId 用户id
     */
    @GetMapping("/start/{userId}/{purchaseOrderId}")
    public R startFlow(@PathVariable String userId, @PathVariable String purchaseOrderId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("start", "PT10S");
        map.put("userId", userId);
        map.put("subProcessHelper", subProcessHelper);
        map.put("purchaseOrderId", purchaseOrderId);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process_pool3", map);
        String processId = processInstance.getId();
        String name = processInstance.getName();
        System.out.println(processId + ":" + name);
        return R.ok(processId + ":" + name);
    }

    /**
     * .获取用户的任务
     *
     * @param userId 用户id
     */
    @GetMapping("/getTasks/{userId}")
    public R getTasks(@PathVariable String userId) {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc().list();
        return R.ok(tasks.toString());
    }
    @GetMapping("/getPurchaseOrderId/{purchaseOrderId}")
    public R getPurchaseOrderId(@PathVariable String purchaseOrderId) {
        List<Task> tasks = taskService.createTaskQuery().processVariableValueEquals("purchaseOrderId",purchaseOrderId).orderByTaskCreateTime().desc().list();
        return R.ok(tasks.toString());
    }
    @GetMapping("/add/{proInstId}")
    public R addPurchaseOrderId(@PathVariable String proInstId) {
        String activityId = null;
        Map<String, Object> executionVariables = new HashMap<>();
        List<ActivityInstance> highLightedFlowInstances = runtimeService.createActivityInstanceQuery()
                .activityType(BpmnXMLConstants.ELEMENT_SUBPROCESS).processInstanceId(proInstId).list();
        for(ActivityInstance activityInstance : highLightedFlowInstances) {
            if(activityInstance.getActivityName().equals("subProcess")) {
                activityId = activityInstance.getActivityId();
                break;
            }
        }
        runtimeService.addMultiInstanceExecution(activityId, proInstId,  executionVariables);
        return R.ok();
    }

    @GetMapping("/del/{proInstId}")
    public R delPurchaseOrderId(@PathVariable String proInstId) {
//        String activityId = null;
//        Map<String, Object> executionVariables = new HashMap<>();
//        List<ActivityInstance> highLightedFlowInstances = runtimeService.createActivityInstanceQuery()
//                .activityType(BpmnXMLConstants.ELEMENT_SUBPROCESS).processInstanceId(proInstId).list();
//        for(ActivityInstance activityInstance : highLightedFlowInstances) {
//            if(activityInstance.getActivityName().equals("subProcess")) {
//                activityId = activityInstance.getActivityId();
//                break;
//            }
//        }
        Execution executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(proInstId).variableValueEquals("test004").singleResult();
        Execution executions5 = runtimeService
                .createExecutionQuery()
                .processInstanceId(proInstId).variableValueEquals("test005").singleResult();
        Execution executions6 = runtimeService
                .createExecutionQuery()
                .processInstanceId(proInstId).variableValueEquals("test006").singleResult();
//                .list();
        runtimeService.deleteMultiInstanceExecution(executions.getId(), false);
        return R.ok();
    }
    /**
     * .审批通过
     */
    @GetMapping("/success/{taskId}")
    public R success(@PathVariable String taskId) {

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return R.error("流程不存在");
        }

//        System.out.println("getProcessVariables:" + task.getProcessVariables().toString());
        //通过审核
        HashMap<String, Object> map = new HashMap<>();
//        List<String> skuList = new ArrayList<>();
//        skuList.add("1");
//        skuList.add("2");
        map.put("paytype", 0);

        taskService.complete(taskId, map);
        return R.ok("流程审核通过！");
    }

    /**
     * .审批不通过
     */
    @PostMapping("/faile/{taskId}")
    public R faile(@PathVariable String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return R.error("流程不存在");
        }
        //通过审核
        HashMap<String, Object> map = new HashMap<>();
        map.put("approved", false);
        taskService.complete(taskId, map);
        return R.ok();
    }

    @RequestMapping(value = "processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

        //流程走完的不显示图
        if (pi == null) {
            return;
        }
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        Task task = tasks.get(0);
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        String InstanceId = task.getProcessInstanceId();
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(InstanceId)
                .list();

        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(
                bpmnModel,
                "png",
                activityIds,
                flows,
                engconf.getActivityFontName(),
                engconf.getLabelFontName(),
                engconf.getAnnotationFontName(),
                engconf.getClassLoader(),
                1.0,
                true);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
