package io.dataease.service.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author gin
 * @Date 2021/4/13 12:32 下午
 */
public class CacheUtil {
    private static CacheUtil cacheUtil;
    private static Map<String, Dataset<Row>> cacheMap;

    private CacheUtil(){
        cacheMap = new HashMap<String, Dataset<Row>>();
    }

    public static CacheUtil getInstance(){
        if (cacheUtil == null){
            cacheUtil = new CacheUtil();
        }
        return cacheUtil;
    }

    /**
     * 添加缓存
     * @param key
     * @param obj
     */
    public void addCacheData(String key,Dataset<Row> obj){
        cacheMap.put(key,obj);
    }

    /**
     * 取出缓存
     * @param key
     * @return
     */
    public Dataset<Row> getCacheData(String key){
        return cacheMap.get(key);
    }

    /**
     * 清楚缓存
     * @param key
     */
    public void removeCacheData(String key){
        cacheMap.remove(key);
    }
}
