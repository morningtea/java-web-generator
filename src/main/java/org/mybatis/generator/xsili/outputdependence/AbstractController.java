package org.mybatis.generator.xsili.outputdependence;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.mybatis.generator.xsili.outputdependence.exception.BusinessException;
import org.mybatis.generator.xsili.outputdependence.model.Result;
import org.mybatis.generator.xsili.outputdependence.user.AdminUser;
import org.mybatis.generator.xsili.outputdependence.user.CommonUser;
import org.mybatis.generator.xsili.outputdependence.user.User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 抽象的控制类
 */
public abstract class AbstractController {

    public static final String RESPONSE_HEADER_SESSION_STATUS = "sessionstatus";

    public static final String RESPONSE_HEADER_SESSION_STATUS_TIMEOUT = "timeout";

    protected HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    protected HttpServletResponse getHttpServletResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    protected Session getSession() {
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        return session;
    }

    protected String readRequest() throws IOException {
        HttpServletRequest request = getHttpServletRequest();

        BufferedReader br = request.getReader();
        try {
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    protected AdminUser getLoginAdminUser() {
        Subject subject = SecurityUtils.getSubject();
        return (AdminUser) subject.getPrincipal();
    }

    protected User getLoginPortalUser() {
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        return user;
    }
    
    protected CommonUser getLoginCommonUser() {
        Subject subject = SecurityUtils.getSubject();
        return (CommonUser) subject.getPrincipal();
    }

    protected void checkOwner(Integer userId) {
        if (!getLoginCommonUser().getId().equals(userId)) {
            throw new BusinessException("当前用户不是数据项的所有者");
        }
    }

    /**
     * 获取国际化资源文件
     * 
     * @param key 属性文件key
     * @param args 替换占位符的参数
     * @return
     */
    protected String getText(String key, Object... args) {
        return I18NUtils.getText(key, args);
    }

    protected Result success() {
        return new Result();
    }

    protected Result success(Object data) {
        return new Result(data);
    }

    protected Result error(String key, Object... objects) {
        return Result.error(this.getText(key, objects));
    }

    protected Result errorData(String key, Object data) {
        Result result = Result.error(this.getText(key));
        result.setData(data);
        return result;
    }

    protected Result successSave() {
        return new Result(this.getText("msg.save.success"));
    }

    protected Result successSave(Object data) {
        Result resultModel = new Result(this.getText("msg.save.success"));
        resultModel.setData(data);
        return resultModel;
    }

    protected Result successDelete() {
        return new Result(this.getText("msg.delete.success"));
    }

    protected Result successDelete(Object data) {
        Result resultModel = new Result(this.getText("msg.delete.success"));
        resultModel.setData(data);
        return resultModel;
    }

    protected Result sessionTimeout() {
        this.getHttpServletResponse().setHeader(RESPONSE_HEADER_SESSION_STATUS, RESPONSE_HEADER_SESSION_STATUS_TIMEOUT);
        return Result.sessionTimeout();
    }

    protected Result sessionTimeout(String loginUrl) {
        this.getHttpServletResponse().setHeader(RESPONSE_HEADER_SESSION_STATUS, RESPONSE_HEADER_SESSION_STATUS_TIMEOUT);
        return Result.sessionTimeout(loginUrl);
    }

}
