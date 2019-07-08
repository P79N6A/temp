package com.dfire.soa.item.partner.common.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 *
 * @param <K>
 * @param <V>
 */
public abstract class BaseGuavaCache<K, V> {

    /**
     * 最大容量
     */
    protected int maximumSize = 10000;

    /**
     * 初始容量
     */
    protected int initialCapacity = 50;

    /**
     * 并发写数量
     */
    protected int concurrencyLevel = 16;

    /**
     * 缓存自动刷新周期
     */
    protected int refreshDuration = 5;

    /**
     * 缓存自动刷新周期时间格式
     */
    protected TimeUnit refreshTimeUnit = TimeUnit.MINUTES;

    /**
     * 缓存过期周期（负数代表永不过期）
     */
    public int expirationDuration = -1;

    public Cache<K,V> cache;

    public BaseGuavaCache() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(maximumSize).initialCapacity(initialCapacity).concurrencyLevel(concurrencyLevel)
                .expireAfterWrite(refreshDuration, refreshTimeUnit)
                .build();
    }

    public BaseGuavaCache(int maximumSize, int initialCapacity, int concurrencyLevel, int refreshDuration, TimeUnit refreshTimeUnit) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(maximumSize).initialCapacity(initialCapacity).concurrencyLevel(concurrencyLevel)
                .expireAfterWrite(refreshDuration, refreshTimeUnit)
                .build();
    }
}
