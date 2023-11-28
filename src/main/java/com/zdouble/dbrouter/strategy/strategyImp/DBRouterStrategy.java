package com.zdouble.dbrouter.strategy.strategyImp;

import com.zdouble.dbrouter.DBContextHolder;
import com.zdouble.dbrouter.DBRouterConfig;
import com.zdouble.dbrouter.strategy.IDBRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBRouterStrategy implements IDBRouterStrategy {
    private Logger logger = LoggerFactory.getLogger(DBRouterStrategy.class);
    private DBRouterConfig dbRouterConfig;

    public DBRouterStrategy(DBRouterConfig dbRouterConfig) {
        this.dbRouterConfig = dbRouterConfig;
    }

    @Override
    public void doRouter(String dbKeyAttr) {
        int size = dbCount()*tbCount();
        int idx = (size - 1) & dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16);

        int dbIdx = idx/dbRouterConfig.getTbCount() + 1;
        int tbIdx = idx - dbIdx * dbRouterConfig.getTbCount();

        setDBKey(dbIdx);
        setTBKey(tbIdx);
        logger.debug("数据库路由 dbIdx：{} tbIdx：{}",  dbIdx, tbIdx);
    }

    @Override
    public void setDBKey(int dbIdx) {
        DBContextHolder.setDbKey(String.format("%02d",dbIdx));
    }

    @Override
    public void setTBKey(int tbIdx) {
        DBContextHolder.setTbKey(String.format("%03d",tbIdx));
    }

    @Override
    public int dbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int tbCount() {
        return dbRouterConfig.getTbCount();
    }

    @Override
    public void clean() {
        DBContextHolder.cleanDbKey();
        DBContextHolder.cleanTbKey();
    }
}
