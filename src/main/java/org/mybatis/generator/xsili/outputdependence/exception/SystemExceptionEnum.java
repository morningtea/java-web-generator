package org.mybatis.generator.xsili.outputdependence.exception;

/**
 * 异常枚举类，定义了基础的系统异常类型
 */
public enum SystemExceptionEnum {

                                 server_internal("服务器发生内部异常"),

                                 db("数据库异常"),

                                 db_unique_constraint("数据库唯一约束异常"),

                                 // cache("缓存异常"),

                                 to_json_convert("toJson数据转换异常"),

                                 from_json_convert("fromJson数据转换异常"),

                                 to_xml_convert("toJson数据转换异常"),

                                 from_xml_convert("fromJson数据转换异常"),

                                 encryption_encode_decode("加密编码/解码异常"),

                                 invalid_arg("无效的参数"),;

    private String code;
    private String msg;

    private SystemExceptionEnum(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
