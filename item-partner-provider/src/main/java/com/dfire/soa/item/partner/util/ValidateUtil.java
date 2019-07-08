package com.dfire.soa.item.partner.util;

import com.dfire.soa.item.constants.ItemMessageConstant;
import com.dfire.soa.item.platform.util.ExceptionUtil;
import com.twodfire.exception.BizException;
import com.twodfire.share.result.Result;
import com.twodfire.share.result.ResultSupport;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by huixiangdou on 2016/10/19.
 * 对象校验工具类
 */
public class ValidateUtil {
    private static final Validator validator = new Validator();

    /**
     * @see com.dfire.soa.item.constants.OvalProfile profile
     */
    public static Result validate(Object validatedObject, String profile) {
        Result result = new ResultSupport(true);
        List<ConstraintViolation> constraintViolations = null;
        if (StringUtils.isNotBlank(profile)) {
            constraintViolations = validator.validate(validatedObject, profile);
        } else {
            constraintViolations = validator.validate(validatedObject);
        }
        if (constraintViolations.size() > 0) { //校验失败
            result.setSuccess(false);
            result.setMessage("参数错误:" + constraintViolations.get(0).getMessage());
        }
        return result;
    }

    public static Result validate(Object validatedObject) {
        return validate(validatedObject, null);
    }

    public static boolean validateConstant(Object... constants) {
        for (Object constant : constants) {
            if (constant instanceof String) {
                if (StringUtils.isBlank((String) constant)) {
                    return false;
                }
            } else if (constant == null) {
                return false;
            }

            if (constant instanceof Collection) {
                if (((Collection) constant).size() == 0) {
                    return false;
                }
            }

            if (constant instanceof Map) {
                if (((Map) constant).size() == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void validateThrowException(Object validatedObject) {
        Result result = validate(validatedObject);
        if (!result.isSuccess()) {
            throwValidateException(result.getMessage());
        }
    }

    public static void validateThrowException(Object validatedObject, String profile) {
        Result result = validate(validatedObject, profile);
        if (!result.isSuccess()) {
            throwValidateException(result.getMessage());
        }
    }

    public static void validateConstantThrowException(Object... constants) {
        if (!validateConstant(constants)) {
            throwValidateException(null);
        }
    }

    private static void throwValidateException(String message) {
        if (StringUtils.isBlank(message)) {
            throw ExceptionUtil.createBizException(ItemMessageConstant.ERROR_MESSAGE_1000, ItemMessageConstant.ERROR_CODE_1000);
        } else {
            throw new BizException(message);
        }
    }
}
