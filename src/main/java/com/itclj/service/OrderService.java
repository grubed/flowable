package com.itclj.service;

import com.itclj.delegate.SubProcessHelper;
import com.itclj.utils.R;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Resource
    private SubProcessHelper subProcessHelper;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngine processEngine;

    public void purchaseOrder( String userId,  String purchaseOrderId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("start", "PT24H");
        map.put("userId", userId);
        map.put("subProcessHelper", subProcessHelper);
        map.put("purchaseOrderId", purchaseOrderId);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process_pool3", map);
        String processId = processInstance.getId();
        String name = processInstance.getName();
        System.out.println(processId + ":" + name);

        Task task = taskService.createTaskQuery().processVariableValueEquals("purchaseOrderId",purchaseOrderId).orderByTaskCreateTime().desc().singleResult();
        if (task == null) {
            return ;
        }
        HashMap<String, Object> map2 = new HashMap<>();
        map2.put("paytype", 0);
        taskService.complete(task.getId(), map2);

    }

    public void pay(String purchaseOrderId) {
        Task task = taskService.createTaskQuery().processVariableValueEquals("purchaseOrderId",purchaseOrderId).orderByTaskCreateTime().desc().singleResult();
        if (task == null) {
            return ;
        }
        HashMap<String, Object> map = new HashMap<>();
        taskService.complete(task.getId(), map);

    }

    public void audit(String purchaseOrderId, Boolean audit) {
        Task task = taskService.createTaskQuery().processVariableValueEquals("purchaseOrderId",purchaseOrderId).orderByTaskCreateTime().desc().singleResult();
        if (task == null) {
            return ;
        }
        HashMap<String, Object> map = new HashMap<>();
        if(audit) {
            map.put("state", "agree");
        } else {
            map.put("state", "reject");
        }
        taskService.complete(task.getId(), map);
    }

    public void outbound(String purchaseOrderId, String outboundId) {
        List<Task> tasks = taskService.createTaskQuery().processVariableValueEquals("purchaseOrderId",purchaseOrderId).orderByTaskCreateTime().desc().list();
        for (Task onetask : tasks) {
            if(onetask.getProcessVariables().get("assignee").equals("")) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("outboundId","");
                taskService.complete(onetask.getId(), map);
            }
        }
    }

    public void delPurchaseOrderId(String purchaseOrderId, List<String> orderSkuList) {
        for(String orderSku : orderSkuList) {
            Execution executions = runtimeService
                    .createExecutionQuery().processVariableValueEquals("purchaseOrderId",purchaseOrderId)
                    .variableValueEquals(orderSku).singleResult();
            runtimeService.deleteMultiInstanceExecution(executions.getId(), false);
        }
    }
    public void addPurchaseOrderId(String purchaseOrderId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("purchaseOrderId",purchaseOrderId).singleResult();
        String activityId = null;
        Map<String, Object> executionVariables = new HashMap<>();
        List<ActivityInstance> highLightedFlowInstances = runtimeService.createActivityInstanceQuery()
                .activityType(BpmnXMLConstants.ELEMENT_SUBPROCESS).processInstanceId(processInstance.getId()).list();
        for(ActivityInstance activityInstance : highLightedFlowInstances) {
            if(activityInstance.getActivityName().equals("subProcess")) {
                activityId = activityInstance.getActivityId();
                break;
            }
        }
        runtimeService.addMultiInstanceExecution(activityId, processInstance.getId(),  executionVariables);
    }
}
