package com.dfire.soa.item.partner.rocketmq.interceptor;

import com.alibaba.fastjson.JSON;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.constants.CommonConstants;
import com.dfire.soa.item.partner.constants.MessageTag;
import com.dfire.soa.item.partner.dao.ICookDetailDAO;
import com.dfire.soa.item.partner.domain.CookDetailDO;
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
import java.util.Map;

/**
 * create by xiaoji on 2018/8/30
 */
@Aspect
@Component
public class CookDetailRmqInterceptor {

	private final Logger logger = LoggerFactory.getLogger(CookRmqInterceptor.class);

	@Autowired
	private ApplicationContext context;
	@Resource
	private ICookDetailDAO cookDetailDAO;

	@Pointcut("execution(* com.dfire.soa.item.partner.dao.ICookDetailDAO.insert(..))")
	public void insert() {
	}

	@Pointcut("execution(* com.dfire.soa.item.partner.dao.ICookDetailDAO.updateById(..))")
	public void update() {
	}

	@Pointcut("execution(* com.dfire.soa.item.partner.dao.ICookDetailDAO.batchInsert(..))")
	public void batchInsert() {
	}

	@Pointcut("execution(* com.dfire.soa.item.partner.dao.ICookDetailDAO.deleteById(..))")
	public void deleteById() {
	}

	@Pointcut("execution(* com.dfire.soa.item.partner.dao.ICookDetailDAO.deleteByMenuId(..))")
	public void deleteByMenuId() {
	}

	@Pointcut("execution(* com.dfire.soa.item.partner.dao.ICookDetailDAO.batchDeleteByIdList(..))")
	public void batchDeleteByIdList() {
	}

	@Pointcut("execution(* com.dfire.soa.item.partner.dao.ICookDetailDAO.deleteByMenuIdList(..))")
	public void deleteByMenuIdList() {
	}

	/**
	 * insert方法拦截
	 *
	 * @param pjp
	 * @return
	 */
	@Around("insert()")
	public Object changeInsert(ProceedingJoinPoint pjp) throws Throwable {
		CookDetailDO cookDetailDO = null;
		try {
			Object[] args = pjp.getArgs();
			cookDetailDO = (CookDetailDO) args[0];
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDetailDO);
			logger.error("菜谱明细insert操作的消息处理失败（消息对象获取）！ param:" + param, e);

		}
		//数据库处理
		Object result = pjp.proceed();
		String tag = null;
		try {
			// 构建更新后的消息对象
			CookDetailDO cookDetailDOAfter = cookDetailDAO.queryById(cookDetailDO.getEntityId(), cookDetailDO.getId());
			// 组装RMQ消息参数
			HashMap<String, String> dataMap = new HashMap<>();
			dataMap.put("entity_id", cookDetailDOAfter.getEntityId());
			dataMap.put("menu_id", cookDetailDOAfter.getMenuId());
			dataMap.put("cook_id", String.valueOf(cookDetailDOAfter.getCookId()));
			dataMap.put("send_time", String.valueOf(System.currentTimeMillis()));
			dataMap.put("data_after", JSON.toJSONString(cookDetailDOAfter));
			tag = MessageTag.TABLE_COOK_DETAIL.concat(MessageTag.SEPARATOR).concat(MessageTag.OPERATE_INSERT);

			// 消息发送
			MyTransactionEvent myTransactionEvent = new MyTransactionEvent(tag, dataMap, this);
			this.context.publishEvent(myTransactionEvent);
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDetailDO);
			logger.error("菜谱明细insert操作的消息处理失败（消息发送）！ param:" + param, e);
		}
		return result;
	}

	@Around("update()")
	public Object changeUpdate(ProceedingJoinPoint pjp) throws Throwable {
		CookDetailDO cookDetailDO = null;
		CookDetailDO cookDetailDOBefore = null;
		try {
			// 获取方法参数
			Object[] args = pjp.getArgs();
			cookDetailDO = (CookDetailDO) args[0];
			// 构建更新前的消息对象
			cookDetailDOBefore = cookDetailDAO.queryById(cookDetailDO.getEntityId(), cookDetailDO.getId());
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDetailDO);
			logger.error("菜谱明细update操作的消息处理失败（消息对象获取）！ param:" + param, e);
		}

		// 数据库处理
		Object result = pjp.proceed();
		String tag = null;
		try {
			if (cookDetailDOBefore == null) {
				return result;
			}
			// 构建更新后的消息对象
			CookDetailDO cookDetailDOAfter = cookDetailDAO.queryById(cookDetailDO.getEntityId(), cookDetailDO.getId());
			// 组装RMQ消息参数
			HashMap<String, String> dataMap = new HashMap<>();
			dataMap.put("entity_id", cookDetailDO.getEntityId());
			dataMap.put("send_time", String.valueOf(System.currentTimeMillis()));
			dataMap.put("menu_id", cookDetailDOAfter.getMenuId());
			dataMap.put("cook_id", String.valueOf(cookDetailDOAfter.getCookId()));
			dataMap.put("data_before", JSON.toJSONString(cookDetailDOBefore));
			dataMap.put("data_after", JSON.toJSONString(cookDetailDOAfter));
			tag = MessageTag.TABLE_COOK_DETAIL.concat(MessageTag.SEPARATOR).concat(MessageTag.OPERATE_UPDATE);

			// 消息发送
			MyTransactionEvent myTransactionEvent = new MyTransactionEvent(tag, dataMap, this);
			this.context.publishEvent(myTransactionEvent);
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDetailDO);
			logger.error("菜谱明细update操作的消息处理失败（消息发送）！ param:" + param, e);
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
		List<CookDetailDO> cookDetailDOList = null;
		try {
			// 获取方法参数
			Object[] args = pjp.getArgs();
			cookDetailDOList = (List<CookDetailDO>) args[0];
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDetailDOList);
			logger.error("菜谱明细batchInsert操作的消息处理失败（消息对象获取）！ param:" + param, e);
		}

		// 数据库处理
		Object result = pjp.proceed();

		try {
			// 构建更新后的消息对象
			for (CookDetailDO cookDetailDO : cookDetailDOList) {
				// 组装ONS消息参数
				HashMap<String, String> dataMap = new HashMap<>();
				dataMap.put("entity_id", cookDetailDO.getEntityId());
				dataMap.put("menu_id", cookDetailDO.getMenuId());
				dataMap.put("cook_id", String.valueOf(cookDetailDO.getCookId()));
				dataMap.put("send_time", String.valueOf(System.currentTimeMillis()));
				dataMap.put("data_after", JSON.toJSONString(cookDetailDO));
				String tag = MessageTag.TABLE_COOK_DETAIL.concat(MessageTag.SEPARATOR).concat(MessageTag.OPERATE_INSERT);
				// 消息发送
				MyTransactionEvent myTransactionEvent = new MyTransactionEvent(tag, dataMap, this);
				this.context.publishEvent(myTransactionEvent);
			}
		} catch (Exception e) {
			String param = JSON.toJSONString(cookDetailDOList);
			logger.error("菜谱明细batchInsert操作的消息处理失败（消息发送）！ param:" + param, e);
		}
		return result;
	}

	/**
	 * deleteById方法拦截
	 *
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around("deleteById()")
	public Object deleteChange(ProceedingJoinPoint pjp) throws Throwable {
		String entityId = null;
		Long id = 0L;
		Object[] args = null;
		CookDetailDO cookDetailDOBefore = null;
		try {
			// 获取方法参数
			args = pjp.getArgs();
			entityId = (String) args[0];
			id = (Long) args[1];
			// 构建更新前的消息对象
			cookDetailDOBefore = cookDetailDAO.queryById(entityId, id);
		} catch (Exception e) {
			String param = JSON.toJSONString(args);
			logger.error("菜谱明细deleteById操作的消息处理失败（消息对象获取）！ param:" + param, e);
		}

		// 数据库处理
		Object result = pjp.proceed();

		try {
			if (cookDetailDOBefore == null) {
				return result;
			}
			// 构建更新后的消息对象
			CookDetailDO cookDetailDOAfter = cookDetailDAO.queryByIdWithoutValid(entityId, id);
			// 组装ONS消息参数
			HashMap<String, String> dataMap = new HashMap<>();
			dataMap.put("entity_id", entityId);
			dataMap.put("menu_id", cookDetailDOBefore.getMenuId());
			dataMap.put("cook_id", String.valueOf(cookDetailDOBefore.getCookId()));
			dataMap.put("data_before", JSON.toJSONString(cookDetailDOBefore));
			dataMap.put("data_after", JSON.toJSONString(cookDetailDOAfter));
			dataMap.put("send_time", String.valueOf(System.currentTimeMillis()));

			String tag = MessageTag.TABLE_COOK_DETAIL.concat(MessageTag.SEPARATOR).concat(MessageTag.OPERATE_DELETE);
			// 消息发送
			MyTransactionEvent myTransactionEvent = new MyTransactionEvent(tag, dataMap, this);
			this.context.publishEvent(myTransactionEvent);
		} catch (Exception e) {
			String param = JSON.toJSONString(args);
			logger.error("菜谱明细deleteById操作的消息处理失败（消息发送）！ param:" + param, e);
		}
		return result;
	}

	/**
	 * deleteById方法拦截
	 *
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around("deleteByMenuId()")
	public Object deleteByMenuIdChange(ProceedingJoinPoint pjp) throws Throwable {
		String entityId = null;
		String menuId = null;
		Object[] args = null;
		List<CookDetailDO> cookDetailDOListBefore = null;
		Map<Long, CookDetailDO> beforeMap = new HashMap<>();
		try {
			// 获取方法参数
			args = pjp.getArgs();
			entityId = (String) args[0];
			menuId = (String) args[1];
			// 构建更新前的消息对象
			CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
			cookDetailQuery.setMenuId(menuId);
			cookDetailQuery.setUsePage(true);
			cookDetailDOListBefore = cookDetailDAO.selectByQuery(cookDetailQuery);
			for (CookDetailDO cookDetailDO : cookDetailDOListBefore) {
				beforeMap.put(cookDetailDO.getId(), cookDetailDO);
			}
		} catch (Exception e) {
			String param = JSON.toJSONString(args);
			logger.error("菜谱明细deleteByMenuId操作的消息处理失败（消息对象获取）！ param:" + param, e);
		}

		// 数据库处理
		Object result = pjp.proceed();

		try {
			if (cookDetailDOListBefore == null) {
				return result;
			}
			// 构建更新后的消息对象
			CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
			cookDetailQuery.setMenuId(menuId);
			cookDetailQuery.setIsValid(CommonConstants.IsValid.INVALID);
			cookDetailQuery.setUsePage(true);
			List<CookDetailDO> cookDetailDOAfter = cookDetailDAO.selectByQuery(cookDetailQuery);
			// 组装ONS消息参数
			for (CookDetailDO cookDetailDO : cookDetailDOAfter) {
				HashMap<String, String> dataMap = new HashMap<>();
				dataMap.put("entity_id", entityId);
				dataMap.put("menu_id", cookDetailDO.getMenuId());
				dataMap.put("cook_id", String.valueOf(cookDetailDO.getCookId()));
				if (null == beforeMap.get(cookDetailDO.getId())) {
					continue;
				}
				dataMap.put("data_before", JSON.toJSONString(beforeMap.get(cookDetailDO.getId())));
				dataMap.put("data_after", JSON.toJSONString(cookDetailDO));
				dataMap.put("send_time", String.valueOf(System.currentTimeMillis()));
				String tag = MessageTag.TABLE_COOK_DETAIL.concat(MessageTag.SEPARATOR).concat(MessageTag.OPERATE_DELETE);
				// 消息发送
				MyTransactionEvent myTransactionEvent = new MyTransactionEvent(tag, dataMap, this);
				this.context.publishEvent(myTransactionEvent);
			}
		} catch (Exception e) {
			String param = JSON.toJSONString(args);
			logger.error("菜谱明细deleteByMenuId操作的消息处理失败（消息发送）！ param:" + param, e);
		}
		return result;
	}


	/**
	 * batchDeleteByIdList方法拦截
	 *
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around("batchDeleteByIdList()")
	public Object batchDeleteChange(ProceedingJoinPoint pjp) throws Throwable {

		Map<Long, CookDetailDO> dataBeforeMap = new HashMap<>();
		String entityId = null;
		List<Long> idList = null;
		List<CookDetailDO> cookDetailDOListBefore = null;
		Object[] args = null;
		try {
			// 获取方法参数
			args = pjp.getArgs();
			entityId = (String) args[0];
			idList = (List<Long>) args[1];
			// 构建更新前的消息对象
			CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
			cookDetailQuery.setIdList(idList);
			cookDetailQuery.setIsValid(CommonConstants.IsValid.VALID);
			cookDetailQuery.setUsePage(true);
			cookDetailDOListBefore = cookDetailDAO.selectByQuery(cookDetailQuery);
			if (cookDetailDOListBefore != null && cookDetailDOListBefore.size() > 0) {
				for (CookDetailDO cookDetailDO : cookDetailDOListBefore) {
					dataBeforeMap.put(cookDetailDO.getId(), cookDetailDO);
				}
			}
		} catch (Exception e) {
			String param = JSON.toJSONString(args);
			logger.error("菜谱明细batchDeleteByIdList操作的消息处理失败（消息对象获取）！ param:" + param, e);
		}
		// 数据库处理
		Object result = pjp.proceed();

		try {
			if (dataBeforeMap.size() == 0) {
				return result;
			}
			CookDetailDO dataBefore;
			// 构建更新后的消息对象
			CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
			cookDetailQuery.setIdList(idList);
			cookDetailQuery.setIsValid(CommonConstants.IsValid.INVALID);
			cookDetailQuery.setUsePage(true);
			List<CookDetailDO> cookDetailDOListSAfter = cookDetailDAO.selectByQuery(cookDetailQuery);
			for (CookDetailDO cookDetailDO : cookDetailDOListSAfter) {
				dataBefore = dataBeforeMap.get(cookDetailDO.getId());
				if (dataBefore == null) {
					continue;
				}
				// 组装消息参数
				HashMap<String, String> dataMap = new HashMap<>();
				dataMap.put("entity_id", entityId);
				dataMap.put("menu_id", cookDetailDO.getMenuId());
				dataMap.put("cook_id", String.valueOf(cookDetailDO.getCookId()));
				dataMap.put("data_before", JSON.toJSONString(dataBefore));
				dataMap.put("data_after", JSON.toJSONString(cookDetailDO));
				dataMap.put("send_time", String.valueOf(System.currentTimeMillis()));
				String tag = MessageTag.TABLE_COOK_DETAIL.concat(MessageTag.SEPARATOR).concat(MessageTag.OPERATE_DELETE);
				// 消息发送
				MyTransactionEvent myTransactionEvent = new MyTransactionEvent(tag, dataMap, this);
				this.context.publishEvent(myTransactionEvent);
			}
		} catch (Exception e) {
			String param = JSON.toJSONString(args);
			logger.error("菜谱明细batchDeleteByIdList操作的消息处理失败（消息发送）！ param:" + param, e);
		}
		return result;
	}

	/**
	 * deleteByMenuIdList方法拦截
	 *
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around("deleteByMenuIdList()")
	public Object deleteByMenuIdListChange(ProceedingJoinPoint pjp) throws Throwable {

		Map<Long, CookDetailDO> dataBeforeMap = new HashMap<>();
		String entityId = null;
		List<String> menuIdList = null;
		List<CookDetailDO> cookDetailDOListBefore = null;
		Object[] args = null;
		try {
			// 获取方法参数
			args = pjp.getArgs();
			entityId = (String) args[0];
			menuIdList = (List<String>) args[1];
			// 构建更新前的消息对象
			CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
			cookDetailQuery.setMenuIdList(menuIdList);
			cookDetailQuery.setIsValid(CommonConstants.IsValid.VALID);
			cookDetailQuery.setUsePage(true);
			cookDetailDOListBefore = cookDetailDAO.selectByQuery(cookDetailQuery);
			if (cookDetailDOListBefore != null && cookDetailDOListBefore.size() > 0) {
				for (CookDetailDO cookDetailDO : cookDetailDOListBefore) {
					dataBeforeMap.put(cookDetailDO.getId(), cookDetailDO);
				}
			}
		} catch (Exception e) {
			String param = JSON.toJSONString(args);
			logger.error("菜谱明细deleteByMenuIdList操作的消息处理失败（消息对象获取）！ param:" + param, e);
		}
		// 数据库处理
		Object result = pjp.proceed();

		try {
			if (dataBeforeMap.size() == 0) {
				return result;
			}
			CookDetailDO dataBefore;
			// 构建更新后的消息对象
			CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
			cookDetailQuery.setMenuIdList(menuIdList);
			cookDetailQuery.setIsValid(CommonConstants.IsValid.INVALID);
			cookDetailQuery.setUsePage(true);
			List<CookDetailDO> cookDetailDOListSAfter = cookDetailDAO.selectByQuery(cookDetailQuery);
			for (CookDetailDO cookDetailDO : cookDetailDOListSAfter) {
				dataBefore = dataBeforeMap.get(cookDetailDO.getId());
				if (dataBefore == null) {
					continue;
				}
				// 组装消息参数
				HashMap<String, String> dataMap = new HashMap<>();
				dataMap.put("entity_id", entityId);
				dataMap.put("menu_id", cookDetailDO.getMenuId());
				dataMap.put("cook_id", String.valueOf(cookDetailDO.getCookId()));
				dataMap.put("data_before", JSON.toJSONString(dataBefore));
				dataMap.put("data_after", JSON.toJSONString(cookDetailDO));
				dataMap.put("send_time", String.valueOf(System.currentTimeMillis()));
				String tag = MessageTag.TABLE_COOK_DETAIL.concat(MessageTag.SEPARATOR).concat(MessageTag.OPERATE_DELETE);
				// 消息发送
				MyTransactionEvent myTransactionEvent = new MyTransactionEvent(tag, dataMap, this);
				this.context.publishEvent(myTransactionEvent);
			}
		} catch (Exception e) {
			String param = JSON.toJSONString(args);
			logger.error("菜谱明细deleteByMenuIdList操作的消息处理失败（消息发送）！ param:" + param, e);
		}
		return result;
	}

}
