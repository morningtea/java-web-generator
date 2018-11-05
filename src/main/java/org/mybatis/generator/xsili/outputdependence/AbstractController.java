package org.mybatis.generator.xsili.outputdependence;

import java.beans.PropertyEditorSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.mybatis.generator.xsili.outputdependence.exception.BusinessException;
import org.mybatis.generator.xsili.outputdependence.model.Result;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 抽象的控制类
 */
public abstract class AbstractController {

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String value) {
				if (StringUtils.isBlank(value)) {
					setValue(null);
				}

				try {
					setValue(new Date(Long.parseLong(value)));
				} catch (NumberFormatException e) {
					throw new BusinessException("字符串转换日期异常", e);
				}
			}
		});
	}

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

	/**
	 * 获取国际化资源文件
	 * 
	 * @param key
	 *            属性文件key
	 * @param args
	 *            替换占位符的参数
	 * @return
	 */
	protected String getText(String key, Object... args) {
		return I18NUtils.getText(key, args);
	}

	protected Result success() {
		return Result.success();
	}

	protected Result success(Object data) {
		return Result.success(data);
	}

	protected Result fail(String key, Object... objects) {
		return Result.fail(this.getText(key, objects));
	}

	protected Result fail(int errorCode, String key, Object... objects) {
		return Result.fail(errorCode, this.getText(key, objects));
	}

	protected Result error(String key, Object... objects) {
		return Result.error(this.getText(key, objects));
	}

	protected Result error(int errorCode, String key, Object... objects) {
		return Result.error(errorCode, this.getText(key, objects));
	}

	public Result gone() {
		return Result.fail("记录不存在");
	}

}
