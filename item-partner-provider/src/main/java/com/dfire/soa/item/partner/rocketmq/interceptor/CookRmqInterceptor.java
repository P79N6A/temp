package com.dfire.soa.item.partner.rocketmq.interceptor;

import com.alibaba.fastjson.JSON;
import com.dfire.soa.item.partner.bo.query.CookQuery;
import com.dfire.soa.item.partner.constants.CommonConstants;
import com.dfire.soa.item.partner.constants.MessageTag;
import com.dfire.soa.item.partner.dao.ICookDAO;
import com.dfire.soa.item.partner.domain.CookDO;
import com.dfire.soa.item.partner.rocketmq.MyTransactionEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * create by xiaoji on 2018/8/30
 */
@Aspect
@Component
public class CookRmqInterceptor {

	private final Logger logger = LoggerFactory.getLogger(CookRmqInterceptor.class);

	@Autowired
	private ApplicationContext context;
	@Resource
	private ICookDAO cookDAO;

	@Pointcut("execution(* com.dfire.soa.item.partner.dao.ICookDAO.insert(..))")
	public void insert() {
	}

	@Pointcut("execution(* com.dfire.soa.item.partner.dao.ICookDAO.updateById(..))")
	public void update() {
	}

	@Pointcut("execution(* com.dfire.soa.item.partner.dao.ICookDAO.batchInsert(..))")
	public void batchInsert() {
	}

	@Pointcut("execution(* com.dfire.soa.item.partner.dao.ICookDAO.deleteById(..))")
	public void deleteById() {
	}

	/**
	 * insert方法拦截
	 *
	 * @param pjp
	 * @return
	 */
	@Around("insert()")
	public Object changeInsert(ProceedingJoinPoint pjp) throws Throwable {
		CookDO cookDO = null;
		try {
			Object[] args = pjp.getArgs();
			cookDO = (CookDO) args[0];
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDO);
			logger.error("菜谱insert操作的消息处理失败（消息对象获取）！ param:" + param, e);

		}
		//数据库处理
		Object result = pjp.proceed();
		String tag = null;
		try {
			// 构建更新后的消息对象
			CookDO cookDOAfter = cookDAO.queryById(cookDO.getEntityId(), cookDO.getId());
			// 组装RMQ消息参数
			HashMap<String, String> dataMap = new HashMap<>();
			dataMap.put("entity_id", cookDOAfter.getEntityId());
			dataMap.put("cook_id", String.valueOf(cookDOAfter.getId()));
			dataMap.put("send_time", String.valueOf(System.currentTimeMillis()));
			dataMap.put("data_after", JSON.toJSONString(cookDOAfter));
			tag = MessageTag.TABLE_COOK.concat(MessageTag.SEPARATOR).concat(MessageTag.OPERATE_INSERT);

			// 消息发送
			MyTransactionEvent myTransactionEvent = new MyTransactionEvent(tag, dataMap, this);
			this.context.publishEvent(myTransactionEvent);
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDO);
			logger.error("菜谱insert操作的消息处理失败（消息发送）！ param:" + param, e);
		}
		return result;
	}

	@Around("update()")
	public Object changeUpdate(ProceedingJoinPoint pjp) throws Throwable {
		CookDO cookDO = null;
		CookDO cookDOBefore = null;
		try {
			// 获取方法参数
			Object[] args = pjp.getArgs();
			cookDO = (CookDO) args[0];
			// 构建更新前的消息对象
			cookDOBefore = cookDAO.queryById(cookDO.getEntityId(), cookDO.getId());
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDO);
			logger.error("菜谱update操作的消息处理失败（消息对象获取）！ param:" + param, e);
		}

		// 数据库处理
		Object result = pjp.proceed();
		String tag = null;
		try {
			if (cookDOBefore == null) {
				return result;
			}
			// 构建更新后的消息对象
			CookDO cookAfter = cookDAO.queryById(cookDO.getEntityId(), cookDO.getId());
			// 组装RMQ消息参数
			HashMap<String, String> dataMap = new HashMap<>();
			dataMap.put("entity_id", cookDO.getEntityId());
			dataMap.put("send_time", String.valueOf(System.currentTimeMillis()));
			dataMap.put("data_before", JSON.toJSONString(cookDOBefore));
			dataMap.put("data_after", JSON.toJSONString(cookAfter));
			tag = MessageTag.TABLE_COOK.concat(MessageTag.SEPARATOR).concat(MessageTag.OPERATE_UPDATE);

			// 消息发送
			MyTransactionEvent myTransactionEvent = new MyTransactionEvent(tag, dataMap, this);
			this.context.publishEvent(myTransactionEvent);
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDO);
			logger.error("菜谱update操作的消息处理失败（消息发送）！ param:" + param, e);
		}
		return result;
	}

	/**
	 * batchInsert方法拦截
	 *
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around("batchInsert()")
	public Object batchInsertChange(ProceedingJoinPoint pjp) throws Throwable {
		List<CookDO> cookDOList = null;
		try {
			// 获取方法参数
			Object[] args = pjp.getArgs();
			cookDOList = (List<CookDO>) args[0];
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDOList);
			logger.error("菜谱batchInsert操作的消息处理失败（消息对象获取）！ param:" + param, e);
		}

		// 数据库处理
		Object result = pjp.proceed();

		try {
			// 构建更新后的消息对象
			for (CookDO cookDO : cookDOList) {
				// 组装ONS消息参数
				HashMap<String, String> dataMap = new HashMap<>();
				dataMap.put("entity_id", cookDO.getEntityId());
				dataMap.put("cook_id", String.valueOf(cookDO.getId()));
				dataMap.put("send_time", String.valueOf(System.currentTimeMillis()));
				dataMap.put("data_after", JSON.toJSONString(cookDO));
				String tag = MessageTag.TABLE_COOK.concat(MessageTag.SEPARATOR).concat(MessageTag.OPERATE_INSERT);
				// 消息发送
				MyTransactionEvent myTransactionEvent = new MyTransactionEvent(tag, dataMap, this);
				this.context.publishEvent(myTransactionEvent);
			}
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDOList);
			logger.error("菜谱batchInsert操作的消息处理失败（消息发送）！ param:" + param, e);
		}
		return result;
	}

	@Around("deleteById()")
	public Object deleteByIdUpdate(ProceedingJoinPoint pjp) throws Throwable {
		CookDO cookDOBefore = null;
		String entityId = null;
		Long id = 0L;
		try {
			// 获取方法参数
			Object[] args = pjp.getArgs();
			entityId = (String) args[0];
			id = (Long) args[1];
			// 构建更新前的消息对象
			cookDOBefore = cookDAO.queryById(entityId, id);
		} catch (Exception e) {
			String param = JSON.toJSONString(id);
			logger.error("菜谱deleteById操作的消息处理失败（消息对象获取）！ param:" + param, e);
		}

		// 数据库处理
		Object result = pjp.proceed();

		String tag = null;
		try {
			if (cookDOBefore == null) {
				return result;
			}
			// 构建更新后的消息对象
			CookQuery cookQuery = new CookQuery(entityId);
			cookQuery.setId(id);
			cookQuery.setIsValid(CommonConstants.IsValid.INVALID);
			List<CookDO> cookAfter = cookDAO.selectByQuery(cookQuery);
			// 组装RMQ消息参数
			HashMap<String, String> dataMap = new HashMap<>();
			dataMap.put("entity_id", entityId);
			dataMap.put("send_time", String.valueOf(System.currentTimeMillis()));
			dataMap.put("data_before", JSON.toJSONString(cookDOBefore));
			dataMap.put("data_after", JSON.toJSONString(cookAfter.get(0)));
			tag = MessageTag.TABLE_COOK.concat(MessageTag.SEPARATOR).concat(MessageTag.OPERATE_DELETE);

			// 消息发送
			MyTransactionEvent myTransactionEvent = new MyTransactionEvent(tag, dataMap, this);
			this.context.publishEvent(myTransactionEvent);
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDOBefore);
			logger.error("菜谱deleteById操作的消息处理失败（消息发送）！ param:" + param, e);
		}
		return result;
	}

}
