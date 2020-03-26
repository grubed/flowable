package com.itclj.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReviewNoApprove implements JavaDelegate {

    private Logger logger = LoggerFactory.getLogger(ReviewNoApprove.class);

    @Override
    public void execute(DelegateExecution delegateExecution) {
        //可以发送消息给某人
        logger.info("拒绝，userId是：{}", delegateExecution.getVariable("userId"));
    }
}
