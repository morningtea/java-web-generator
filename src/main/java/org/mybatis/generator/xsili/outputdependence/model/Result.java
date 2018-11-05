package org.mybatis.generator.xsili.outputdependence.model;

import org.mybatis.generator.xsili.outputdependence.I18NUtils;

/**
 * 返回结果模型对象
 */
public class Result {

	/** 状态码, 正常0, 失败1, 系统错误2, 未登录3, 个位数是保留状态码, 其他业务相关状态码10以上 */
	private int code = ResultErrorCodeEnum.SUCCESS.getCode();

	/** 消息 */
	private String errorMsg;

	/** debug模式下, 同时返回错误堆栈 */
	private String exMsg;

	/** 业务数据 */
	private Object data;

	public Result() {
	}

	public Result(int code, String errorMsg) {
		this.code = code;
		this.errorMsg = errorMsg;
	}

	public Result(int code, String errorMsg, Object data) {
		this(code, errorMsg);
		this.data = data;
	}

	/**
	 * 操作成功
	 * 
	 * @return
	 */
	public static Result success() {
		return new Result(ResultErrorCodeEnum.SUCCESS.getCode(),
				I18NUtils.getText(ResultErrorCodeEnum.SUCCESS.getKey()));
	}

	/**
	 * 操作成功
	 * 
	 * @param data
	 * @return
	 */
	public static Result success(Object data) {
		return new Result(ResultErrorCodeEnum.SUCCESS.getCode(),
				I18NUtils.getText(ResultErrorCodeEnum.SUCCESS.getKey()), data);
	}

	/**
	 * 请求失败
	 * 
	 * @return
	 */
	public static Result fail() {
		return new Result(ResultErrorCodeEnum.FAIL.getCode(), I18NUtils.getText(ResultErrorCodeEnum.FAIL.getKey()));
	}

	/**
	 * 请求失败
	 * 
	 * @param errorMsg
	 * @return
	 */
	public static Result fail(String errorMsg) {
		return new Result(ResultErrorCodeEnum.FAIL.getCode(), errorMsg);
	}
	
	/**
	 * 请求失败
	 * 
	 * @param code
	 * @param errorMsg
	 * @return
	 */
	public static Result fail(int code, String errorMsg) {
		return new Result(code, errorMsg);
	}

	/**
	 * 系统错误
	 * 
	 * @return
	 */
	public static Result error() {
		return new Result(ResultErrorCodeEnum.SYSTEM_ERROR.getCode(),
				I18NUtils.getText(ResultErrorCodeEnum.SYSTEM_ERROR.getKey()));
	}

	/**
	 * 系统错误
	 * 
	 * @param errorMsg
	 * @return
	 */
	public static Result error(String errorMsg) {
		return new Result(ResultErrorCodeEnum.SYSTEM_ERROR.getCode(), errorMsg);
	}
	
	/**
	 * 系统错误
	 * 
	 * @param code
	 * @param errorMsg
	 * @return
	 */
	public static Result error(int code, String errorMsg) {
		return new Result(code, errorMsg);
	}

	/**
	 * session超时/未登录
	 * 
	 * @return
	 */
	public static Result sessionTimeout() {
		return new Result(ResultErrorCodeEnum.SESSION_TIMEOUT.getCode(),
				I18NUtils.getText(ResultErrorCodeEnum.SESSION_TIMEOUT.getKey()));
	}

	/**
	 * session超时/未登录
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
	 * 用户被禁用
	 * 
	 * @return
	 */
	public static Result accountDisabled() {
		return new Result(ResultErrorCodeEnum.AUTH_ACCOUNT_DISABLED.getCode(),
				I18NUtils.getText(ResultErrorCodeEnum.AUTH_ACCOUNT_DISABLED.getKey()));
	}

	/**
	 * 权限不足
	 * 
	 * @return
	 */
	public static Result forbidden() {
		return new Result(ResultErrorCodeEnum.AUTH_ACCOUNT_FORBIDDEN.getCode(),
				I18NUtils.getText(ResultErrorCodeEnum.AUTH_ACCOUNT_FORBIDDEN.getKey()));
	}

	// getter setter

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getExMsg() {
		return exMsg;
	}

	public void setExMsg(String exMsg) {
		this.exMsg = exMsg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
