package com.zdouble.dbrouter;

import com.zdouble.dbrouter.annotation.DBRouter;
import com.zdouble.dbrouter.strategy.IDBRouterStrategy;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class DBRouterJoinPoint {
    private Logger logger = LoggerFactory.getLogger(DBRouterJoinPoint.class);
    private DBRouterConfig dbRouterConfig;
    private IDBRouterStrategy dbRouterStrategy;

    public DBRouterJoinPoint(DBRouterConfig dbRouterConfig,IDBRouterStrategy dbRouterStrategy) {
        this.dbRouterConfig = dbRouterConfig;
        this.dbRouterStrategy = dbRouterStrategy;
    }

    @Pointcut("@annotation(com.zdouble.dbrouter.annotation.DBRouter)")
    public void aopPoint(){}
    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint jp, DBRouter dbRouter) throws Throwable {
        //1.获取路由字段
        String dbKey = dbRouter.key();
        if (StringUtils.isBlank(dbKey) || StringUtils.isBlank(dbRouterConfig.getRouterKey())){
            throw new RuntimeException("annotation DBRouter is not null!");
        }
        dbKey = StringUtils.isNotBlank(dbKey) ? dbKey : dbRouterConfig.getRouterKey();
        //2.获取字段属性值
        String dbKeyAttr = getAttrValue(dbKey, jp.getArgs());
        //3.执行路由策略
        dbRouterStrategy.doRouter(dbKeyAttr);
        try {
            return jp.proceed();
        } finally {
            dbRouterStrategy.clean();
        }
    }
    public String getAttrValue(String attr, Object[] args){
        if (args.length == 1){
            Object arg = args[0];
            if (arg instanceof String){
                return arg.toString();
            }
        }
        String fieldValue = null;
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(fieldValue)){
                    break;
                }
                fieldValue = BeanUtils.getProperty(arg,attr);
            } catch (Exception e){
                logger.error("获取路由属性值失败 attr：{}", arg, e);
            }
        }
        return fieldValue;
    }
}
