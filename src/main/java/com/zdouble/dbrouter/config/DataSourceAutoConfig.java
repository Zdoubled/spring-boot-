package com.zdouble.dbrouter.config;

import com.zdouble.dbrouter.DBRouterConfig;
import com.zdouble.dbrouter.DBRouterJoinPoint;
import com.zdouble.dbrouter.dynamic.DynamicDataSource;
import com.zdouble.dbrouter.dynamic.DynamicMybatisPlugin;
import com.zdouble.dbrouter.strategy.IDBRouterStrategy;
import com.zdouble.dbrouter.strategy.strategyImp.DBRouterStrategy;
import com.zdouble.dbrouter.util.PropertyUtil;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceAutoConfig implements EnvironmentAware {
    private Map<String,Map<String, Object>> dataSourceMap = new HashMap<>();//配置数据源
    private Map<String, Object> defaultDataSourceConfig = new HashMap<>();//配置默认数据源
    private int dbCount;//分库数量
    private int tbCount;//分表数量
    private String routerKey;//路由字段

    @Bean
    public DBRouterConfig dbRouterConfig(){
        return new DBRouterConfig(dbCount,tbCount,routerKey);
    }

    @Bean
    public IDBRouterStrategy dbRouterStrategy(DBRouterConfig dbRouterConfig){
        return new DBRouterStrategy(dbRouterConfig);
    }

    @ConditionalOnMissingBean
    public DBRouterJoinPoint dbRouterJoinPoint(DBRouterConfig dbRouterConfig,IDBRouterStrategy dbRouterStrategy){
        return new DBRouterJoinPoint(dbRouterConfig,dbRouterStrategy);
    }

    @Bean
    public Interceptor plugin(){
        return new DynamicMybatisPlugin();
    }

    @Bean
    public DataSource dataSource(){
        Map<Object,Object> targetDataSources = new HashMap<>();
        for (String dbInfo : dataSourceMap.keySet()) {
            Map<String, Object> objectMap = dataSourceMap.get(dbInfo);
            targetDataSources.put(dbInfo,new DriverManagerDataSource(
                    objectMap.get("url").toString(),
                    objectMap.get("username").toString(),
                    objectMap.get("password").toString()
            ));
        }
        //设置数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(new DriverManagerDataSource(
                defaultDataSourceConfig.get("url").toString(),
                defaultDataSourceConfig.get("username").toString(),
                defaultDataSourceConfig.get("password").toString()
        ));
        return dynamicDataSource;
    }

    @Override
    public void setEnvironment(Environment environment) {
        //读取yml文件的数据源信息
        String prefix = "";
        //获取库表数量
        dbCount = Integer.valueOf(environment.getProperty(prefix + "dbCount"));
        tbCount = Integer.valueOf(environment.getProperty(prefix + "tbCount"));
        routerKey = environment.getProperty(prefix + "routerKey");
        //分库分表数据源
        String dataSources = environment.getProperty(prefix + "list");
        assert dataSources != null;
        for (String dbInfo:dataSources.split(",")){
            Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dbInfo, Map.class);
            dataSourceMap.put(dbInfo, dataSourceProps);
        }
        String defaultData = environment.getProperty(prefix + "default");
        defaultDataSourceConfig = PropertyUtil.handle(environment, prefix + defaultData, Map.class);
    }
}
