package org.mybatis.generator.xsili.outputdependence;

import java.text.MessageFormat;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

public class I18NUtils {

    /**
     * 
     * @return 没有request, 则返回null. 比如junit测试中调用本方法
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes == null) {
            return null;
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request;
    }

    /**
     * 获取国际化资源信息
     * 
     * @param request
     * @param key 属性文件key
     * @param args 替换占位符的参数
     * @return
     */
    public static String getText(String key, Object... args) {
        HttpServletRequest request = getRequest();
        Locale locale = null;
        if (request == null) {
            locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            locale = (Locale) request.getAttribute(CookieLocaleResolver.LOCALE_REQUEST_ATTRIBUTE_NAME);
            if (locale == null) {
                locale = Locale.SIMPLIFIED_CHINESE;
            }
        }
        return getText(locale, key, args);
    }

    public static String getText(Locale locale, String key, Object... args) {
        return getMessage(locale, key, args);
    }

    private static String getMessage(Locale locale, String key, Object... args) {
        String text = SpringContextHolder.getApplicationContext().getMessage(key, args, locale);
        if (text.equals(key)) {
            return MessageFormat.format(key, args);
        }
        return text;
    }

}
