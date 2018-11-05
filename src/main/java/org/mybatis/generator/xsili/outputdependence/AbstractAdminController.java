package org.mybatis.generator.xsili.outputdependence;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.mybatis.generator.xsili.outputdependence.exception.BusinessException;
import org.mybatis.generator.xsili.outputdependence.user.UpmsUser;

/**
 * 抽象的控制类
 */
public abstract class AbstractAdminController extends AbstractController {

	protected UpmsUser getLoginUser() {
		Subject subject = SecurityUtils.getSubject();
		try {
			return (UpmsUser) subject.getPrincipal();
		} catch (ClassCastException e) {// 类型不对, 表示登录了其他类型的用户, 那么就会出现该异常
			// ignore
			return null;
		}
	}

	protected void checkOwner(Long userId) {
		UpmsUser upmsUser = getLoginUser();
		if (upmsUser != null && !upmsUser.getId().equals(userId)) {
			throw new BusinessException("当前用户不是数据项的所有者");
		}
	}
}
