package org.mybatis.generator.xsili.outputdependence.exception;

/**
 * 系统异常<br>
 * 该异常指非用户操作造成，程序在运算过程中发生的异常
 */
public class SystemException extends RuntimeException {

    private static final long serialVersionUID = 7581695155270610988L;

    private SystemExceptionEnum exceptionEnum;

    public SystemException() {
        super();
    }

    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }

    public SystemException(SystemExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMsg());
        this.exceptionEnum = exceptionEnum;
    }

    public SystemException(SystemExceptionEnum exceptionEnum, Throwable e) {
        super(exceptionEnum.getMsg(), e);
        this.exceptionEnum = exceptionEnum;
    }

    public SystemExceptionEnum getExceptionEnum() {
        return exceptionEnum;
    }

    public void setExceptionEnum(SystemExceptionEnum exceptionEnum) {
        this.exceptionEnum = exceptionEnum;
    }
}
