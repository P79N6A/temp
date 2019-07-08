package com.dfire.soa.item.partner.service;

import java.util.List;
import java.util.Map;

/**
 * Created by yupian on 17/9/12.
 */
public interface IItemCacheService {

    /**
     * 缓存结果
     *
     * @param namespace
     * @param data
     * @param expireSecond
     */
    void putCache(String namespace, Object data, int expireSecond);

    /**
     * 缓存结果
     *
     * @param map
     * @param expireSecond
     */
    void putCache(Map<String, Object> map, int expireSecond);

    /**
     * 获取缓存
     *
     * @param namespace
     * @return
     */
    Object getCache(String namespace);

    /**
     * 获取缓存
     *
     * @param namespaces
     * @return
     */
    <T> List<T> getCache(List<String> namespaces);

    /**
     * 清除缓存
     *
     * @param namespace
     */
    void clearCache(String namespace);

    /**
     * 清除缓存
     *
     * @param namespace
     */
    void clearCache(String... namespace);
}
