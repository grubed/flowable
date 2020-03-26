package com.itclj.delegate;

import com.itclj.service.OrderService;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubProcessHelper  implements Serializable {
    @Resource
    private OrderService orderService;

    public List<String> getUserNames() {
        List<String> userNames = new ArrayList<>();
        userNames.add("test004");
        userNames.add("test005");
        userNames.add("test006");
        return userNames;
    }


    public boolean isComplete(DelegateExecution execution) {
        Integer completeInstCount = (Integer) execution.getVariable("nrOfCompletedInstances");
        Integer instCount = (Integer) execution.getVariable("nrOfInstances");
        if(completeInstCount / instCount == 1) {
            return true;
        } else {
            return false;
        }
//        return completeInstCount > 1;
    }
}
