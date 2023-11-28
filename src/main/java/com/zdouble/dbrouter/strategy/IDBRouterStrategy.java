package com.zdouble.dbrouter.strategy;

public interface IDBRouterStrategy {
    /*
    * 执行路由策略
    * */
    void doRouter(String dbKeyAttr);
    /*
    * 设置分库库字段
    * */
    void setDBKey(int dbIdx);
    /*
    * 设置分表字段
    * */
    void setTBKey(int tbIdx);
    /*
    * 获取分库数量
    * */
    int dbCount();
    /*
     * 获取分表数量
     * */
    int tbCount();

    /*
    * 清空上下文缓存
    * */
    void clean();
}
