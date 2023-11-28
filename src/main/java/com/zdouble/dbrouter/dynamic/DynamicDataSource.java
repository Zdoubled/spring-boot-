package com.zdouble.dbrouter.dynamic;

import com.zdouble.dbrouter.DBContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return "db" + DBContextHolder.getDbKey();
    }
}
