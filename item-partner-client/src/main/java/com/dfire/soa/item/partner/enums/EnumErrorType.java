package com.dfire.soa.item.partner.enums;

/**
 * 异常错误码
 * Created by zhishi on 2018/8/8 0008.
 */
public enum EnumErrorType {
    SYSTEM_DEFAULT_ERROR("SYSTEM_DEFAULT_ERROR", "服务器内部错误"),
    SYSTEM_BUSINESS_ERROR("SYSTEM_BUSINESS_ERROR", "业务异常"),
    THIRD_BUSINESS_ERROR("THIRD_BUSINESS_ERROR", "第三方业务异常"),
    ;


    private String errorCode;
    private String errorMsg;

    EnumErrorType(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
