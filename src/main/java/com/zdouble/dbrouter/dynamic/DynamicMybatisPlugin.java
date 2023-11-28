package com.zdouble.dbrouter.dynamic;

import com.mysql.jdbc.Connection;
import com.zdouble.dbrouter.DBContextHolder;
import com.zdouble.dbrouter.annotation.DBRouterStrategy;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class DynamicMybatisPlugin implements Interceptor {
    private Pattern pattern = Pattern.compile("(from|into|update)[\\s]{1,}(\\w{1,})");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = MetaObject.forObject(statementHandler,
                SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                new DefaultReflectorFactory()
        );
        //判断是否需要分表
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        String id = mappedStatement.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        Class<?> clazz = Class.forName(className);
        DBRouterStrategy dbRouterStrategy = clazz.getAnnotation(DBRouterStrategy.class);
        if (null == dbRouterStrategy || !dbRouterStrategy.splitTable()){
            return invocation.proceed();
        }
        //执行分表操作
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        if (matcher.find()){
            tableName = matcher.group().trim();
        }
        assert tableName != null;
        //修改sql语句
        String replaceSql = matcher.replaceAll(tableName + "_" + DBContextHolder.getTbKey());
        Field field = boundSql.getClass().getField("sql");
        field.setAccessible(true);
        field.set(boundSql, replaceSql);
        field.setAccessible(false);

        return invocation.proceed();
    }
}
