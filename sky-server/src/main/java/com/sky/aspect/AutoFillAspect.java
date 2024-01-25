package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

//自定义切面，实现公共字段自动填充处理逻辑
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //切入点
    @Pointcut("execution(* com.sky.mapper..*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {

    }
    //前置通知在切入点之前执行JoinPoint连接点
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始执行公共字段自动填充");
        //获取数据库操作类型
        MethodSignature signature=(MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFill autoFill=signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的注解对象
        OperationType operationType=autoFill.value();//获取数据库操作类型
        Object args[]=joinPoint.getArgs();
        if(args ==null || args.length==0){
            return;
        }
        Object entity=args[0];
        if(operationType==OperationType.INSERT){

            try {
                Method setUpdateTime= entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser=entity.getClass().getDeclaredMethod("setUpdateUser",Long.class);
                Method setCreateTime=entity.getClass().getDeclaredMethod("setCreateTime",LocalDateTime.class);
                Method setCreateUser=entity.getClass().getDeclaredMethod("setCreateUser",Long.class);
                setCreateTime.invoke(entity,LocalDateTime.now());
                setCreateUser.invoke(entity, BaseContext.getCurrentId());
                setUpdateTime.invoke(entity,LocalDateTime.now());
                setUpdateUser.invoke(entity,BaseContext.getCurrentId());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else if (operationType==OperationType.UPDATE){
            try {
                Method setUpdateTime= entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser=entity.getClass().getDeclaredMethod("setUpdateUser",Long.class);
                setUpdateTime.invoke(entity,LocalDateTime.now());
                setUpdateUser.invoke(entity,BaseContext.getCurrentId());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
