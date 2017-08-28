package org.mybatis.generator.outputdependence.validation;

/**
 * 验证器模型，用于校验失败返回给前台的模型对象<br>
 * 2014年12月3日
 */
public class Validator {
	
	public Validator(){}
	
	public Validator(String name, String value, String msg) {
		super();
		this.name = name;
		this.value = value;
		this.msg = msg;
	}
	/** 参数名 */
	private String name;
	/** 参数值 */
	private String value;
	/** 验证结果信息 */
	private String msg;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
