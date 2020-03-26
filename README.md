# spring-boot-flowable-demo
spring-boot-flowable-demo

## 测试

**提交采购订单的审批请求**

```
http://127.0.0.1:8080/flowable/start/1/1
```

**获取用户的任务**
```html
http://127.0.0.1:8080/flowable/getTasks/{userId}

http://127.0.0.1:8080/flowable/getTasks/1
```

**获取流程图**
```html
http://127.0.0.1:8080/flowable/processDiagram?processId=556081a9-1772-11ea-8d24-d2c6372034ce
```

## IDEA教程之Activiti插件
```html
https://blog.csdn.net/gozhuyinglong/article/details/80336765
```

## 参考

- https://blog.csdn.net/puhaiyang/article/details/79845248
- https://blog.csdn.net/qq_35098526/article/details/87818988
- 【官网文档】https://www.flowable.org/docs/userguide/index.html#flowableUIApps
- 【官网文档中文翻译】https://tkjohn.github.io/flowable-userguide/