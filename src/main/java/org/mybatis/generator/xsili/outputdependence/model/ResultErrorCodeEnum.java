package org.mybatis.generator.xsili.outputdependence.model;

import org.mybatis.generator.xsili.outputdependence.I18NConstant;

/**
 * 结果状态
 */
public enum ResultErrorCodeEnum {
	
	/** 成功 */
	SUCCESS(0, ""),
	/** 失败. 不需要特殊处理，把错误显示出来即可。比如下单校验发现商品已下架，会提示"该商品已下架" */
	FAIL(1, I18NConstant.SYSTEM_ACTION_FAIL),
	/** 系统错误 */
	SYSTEM_ERROR(2, I18NConstant.SYSTEM_ERROR),
	/** 未登录/登录超时 */
	SESSION_TIMEOUT(3, I18NConstant.SYSTEM_SESSION_TIMEOUT),
	/** 用户被禁用 */
	AUTH_ACCOUNT_DISABLED(4, I18NConstant.AUTH_ACCOUNT_DISABLED),
	/** 权限不足 */
	AUTH_ACCOUNT_FORBIDDEN(5, I18NConstant.AUTH_ACCOUNT_FORBIDDEN);

    /** 状态码 */
    private int code;

    /** 信息 */
    private String key;

    private ResultErrorCodeEnum(int code, String key) {
        this.code = code;
        this.key = key;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
