package com.dfire.soa.item.partner.service.impl;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.dfire.soa.item.partner.constant.CacheConstants;
import com.dfire.soa.item.partner.service.IItemCacheService;
import com.dfire.soa.item.platform.constants.CommonConstants;
import com.twodfire.redis.CodisService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yupian on 17/9/12.
 */
@Service("itemCacheService")
public class ItemCacheServiceImpl implements IItemCacheService {

	@Resource
	private CodisService codisService;

//	@Resource
//	private CodisService codisService;

	private Logger logger = LoggerFactory.getLogger(CommonConstants.Logger.BIZ_ERROR);

	/**
	 * 缓存结果
	 *
	 * @param namespace
	 * @param data
	 */
	@Override
	public void putCache(String namespace, Object data, int expireSecond) {
		if (data != null) {
			try {
				codisService.setObjectByZip(buildCacheKey(namespace), data, expireSecond);
			} catch (Exception e) {
				logger.error("redis putCache error:", e);
			}
		}
	}

	@Override
	public void putCache(Map<String, Object> map, int expireSecond) {
		if (map != null) {
			Map<String, Object> buildCacheMap = new HashMap<>();
			for (String namespace : map.keySet()){
				buildCacheMap.put(buildCacheKey(namespace), map.get(namespace));
			}

			try {
				codisService.msetObj(buildCacheMap, expireSecond);
			} catch (Exception e) {
				logger.error("redis getCache error:", e);
			}
		}
	}

	/**
	 * 获取缓存
	 *
	 * @param namespace
	 * @return
	 */
	@Override
	public Object getCache(String namespace) {
		try {
			return codisService.getObjectByZip(buildCacheKey(namespace));
		} catch (Exception e) {
			logger.error("redis getCache error:", e);
		}
		return null;
	}

	/**
	 * 获取缓存
	 *
	 * @param namespaces
	 * @return
	 */
	@Override
	public <T> List<T> getCache(List<String> namespaces) {
		try {
			byte[][] buildCacheNamespaces = new byte[namespaces.size()][];
			for (int i=0; i<namespaces.size(); i++){
				buildCacheNamespaces[i] = buildCacheKey(namespaces.get(i)).getBytes();
			}
			return codisService.mget(buildCacheNamespaces);
		} catch (Exception e) {
			logger.error("redis getCache error:", e);
		}
		return null;
	}

	/**
	 * 清除缓存
	 *
	 * @param namespace
	 */
	@Override
	public void clearCache(String namespace) {
		String delKey = buildCacheKey(namespace);
		try {
            codisService.del(delKey);
		} catch (Exception e) {
			logger.error("redis clearCache error:", e);
		}
	}

	@Override
	public void clearCache(String... namespace) {
		if (namespace == null || namespace.length == 0) {
			return;
		}
		String[] arr = new String[namespace.length];
		for (int i = 0; i < namespace.length; i++) {
			arr[i] = buildCacheKey(namespace[i]);
		}
		try {
            codisService.del(arr);
		} catch (Exception e) {
			logger.error("redis clearCache error:", e);
		}
	}

	private String buildCacheKey(String namespace) {
		//生成缓存key
		StringBuilder keyBuilder = new StringBuilder();
		keyBuilder.append(CacheConstants.ITEM_PARTNER_PREFIX).append(namespace);
		return keyBuilder.toString();
	}

}
