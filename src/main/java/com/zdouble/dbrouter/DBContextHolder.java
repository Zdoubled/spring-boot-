package com.zdouble.dbrouter;

public class DBContextHolder {
    private static final ThreadLocal<String> dbKey = new ThreadLocal<>();
    private static final ThreadLocal<String> tbKey = new ThreadLocal<>();

    public static void setDbKey(String dbKeyIdx){
        dbKey.set(dbKeyIdx);
    }
    public static String getDbKey(){
        return dbKey.get();
    }
    public static void setTbKey(String dbKeyIdx){
        tbKey.set(dbKeyIdx);
    }
    public static String getTbKey(){
        return tbKey.get();
    }
    public static void cleanDbKey(){
        dbKey.remove();
    }
    public static void cleanTbKey(){
        tbKey.remove();
    }
}
