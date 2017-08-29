package org.mybatis.generator.xsili.outputdependence.model;

import org.mybatis.generator.xsili.outputdependence.I18NUtils;

/**
 * 返回结果模型对象
 */
public class Result {

    /** 状态码, 正常0, 失败1, 未登录2, 个位数是保留状态码, 其他业务相关状态码10以上 */
    private int errorCode = ResultErrorCodeEnum.SUCCESS.getCode();

    /** 消息 */
    private String msg;

    /** 业务数据 */
    private Object data;

    public Result() {
    }

    /**
     * status 默认成功 1
     */
    public Result(String msg) {
        this.msg = msg;
    }

    /**
     * status 默认成功 1
     */
    public Result(Object data) {
        this.data = data;
    }

    public Result(int errorCode, String msg) {
        this(msg);
        this.errorCode = errorCode;
    }

    public Result(int errorCode, String msg, Object data) {
        this(msg);
        this.errorCode = errorCode;
        this.data = data;
    }

    /**
     * 构建成功结果模型
     * 
     * @param data
     * @return
     */
    public static Result success(Object data) {
        return new Result(data);
    }

    /**
     * 构建错误结果模型
     * 
     * @return
     */
    public static Result error() {
        return new Result(ResultErrorCodeEnum.FAILURE.getCode(), I18NUtils.getText(ResultErrorCodeEnum.FAILURE.getKey()));
    }

    /**
     * 构建错误结果模型
     * 
     * @param msg
     * @return
     */
    public static Result error(String msg) {
        return new Result(ResultErrorCodeEnum.FAILURE.getCode(), msg);
    }

    /**
     * 构建session超时/未登录 结果模型
     * 
     * @return
     */
    public static Result sessionTimeout() {
        return new Result(ResultErrorCodeEnum.SESSION_TIMEOUT.getCode(), I18NUtils.getText(ResultErrorCodeEnum.SESSION_TIMEOUT.getKey()));
    }

    /**
     * 构建session超时/未登录 结果模型
     * 
     * @param loginUrl
     * @return
     */
    public static Result sessionTimeout(String loginUrl) {
        Result result = sessionTimeout();
        result.setData(loginUrl);
        return result;
    }

    /**
     * 构建无权限 结果模型
     * 
     * @return
     */
    public static Result forbidden() {
        return new Result(ResultErrorCodeEnum.AUTH_ACCOUNT_FORBIDDEN.getCode(), I18NUtils.getText(ResultErrorCodeEnum.AUTH_ACCOUNT_FORBIDDEN.getKey()));
    }

    /**
     * 构建用户被禁用 结果模型
     * 
     * @return
     */
    public static Result accountDisabled() {
        return new Result(ResultErrorCodeEnum.AUTH_ACCOUNT_DISABLED.getCode(), I18NUtils.getText(ResultErrorCodeEnum.AUTH_ACCOUNT_DISABLED.getKey()));
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
