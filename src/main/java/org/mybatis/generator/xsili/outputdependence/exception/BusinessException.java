package org.mybatis.generator.xsili.outputdependence.exception;

import org.mybatis.generator.xsili.outputdependence.model.Result;

/**
 * 业务异常类<br>
 * 该类型异常为用户行为导致运算出错，或违反业务操作逻辑造成的异常
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 替换点位符的参数 */
    private Object[] args;

    private Result result;

    public BusinessException() {
        super();
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String message, Throwable cause, Object... args) {
        super(message, cause);
        this.args = args;
    }

    public BusinessException(String message, Object... args) {
        super(message);
        this.args = args;
    }

    public BusinessException(Result result) {
        super(result.getErrorMsg());
        this.result = result;
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

}
