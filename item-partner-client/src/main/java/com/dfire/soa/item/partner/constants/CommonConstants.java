package com.dfire.soa.item.partner.constants;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
public interface CommonConstants {

	interface IsValid {
		//无效
		int INVALID = 0;

		//有效
		int VALID = 1;
	}

	interface Status {
		// 停用
		int STOP = 0;

		// 启用
		int USING = 1;
	}

	interface UsePriceSwitch {
		//不一致
		int DIFF = 0;

		//一致
		int SAME = 1;
	}

	interface SyncStatus {
		//失败
		int FAIL = 0;

		//成功
		int SUCCESS = 1;
	}

}
