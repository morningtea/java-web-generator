package org.mybatis.generator.xsili.outputdependence.model;

import org.mybatis.generator.xsili.outputdependence.I18NConstant;

/**
 * 结果状态
 */
public enum ResultErrorCodeEnum {
                           /** 成功 */
                           SUCCESS(0, ""),
                           /** 失败 */
                           FAILURE(1, I18NConstant.SYSTEM_EX_MSG_PREFIX),
                           /** 未登录/登录超时 */
                           SESSION_TIMEOUT(2, I18NConstant.SYSTEM_SESSION_TIMEOUT),
                           /** 无权访问 */
                           AUTH_ACCOUNT_FORBIDDEN(3, I18NConstant.AUTH_ACCOUNT_FORBIDDEN),
                           /** 用户被禁用 */
                           AUTH_ACCOUNT_DISABLED(4, I18NConstant.AUTH_ACCOUNT_DISABLED);

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
