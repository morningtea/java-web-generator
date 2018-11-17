package org.mybatis.generator.xsili.outputdependence;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
//import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class NotNullBeanUtils extends BeanUtils {

    /**
     * Copy the property values of the given source bean into the given target bean, ignoring the given
     * "ignoreProperties".
     * <p>
     * Note: The source and target classes do not have to match or even be derived from each other, as long as the
     * properties match. Any bean properties that the source bean exposes but the target bean does not will silently be
     * ignored.
     * <p>
     * This is just a convenience method. For more complex transfer needs, consider using a full BeanWrapper.
     * 
     * @param source the source bean
     * @param target the target bean
     * @param ignoreProperties array of property names to ignore
     * @throws BeansException if the copying failed
     * @see BeanWrapper
     */
    public static void copyNotNullProperties(Object source,
                                             Object target,
                                             // @Nullable Class<?> editable,
                                             /*@Nullable*/ String... ignoreProperties) throws BeansException {

        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        Class<?> actualEditable = target.getClass();
        // 注释editable相关代码
        // if (editable != null) {
        // if (!editable.isInstance(target)) {
        // throw new IllegalArgumentException("Target class [" + target.getClass().getName()
        // + "] not assignable to Editable class [" + editable.getName() + "]");
        // }
        // actualEditable = editable;
        // }
        PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
        List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);

        for (PropertyDescriptor targetPd : targetPds) {
            Method writeMethod = targetPd.getWriteMethod();
            if (writeMethod != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()))) {
                PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null) {
                    Method readMethod = sourcePd.getReadMethod();
                    if (readMethod != null
                        && ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
                        try {
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                readMethod.setAccessible(true);
                            }
                            Object value = readMethod.invoke(source);
                            if (value != null) {// 添加判断, 如果值不为null, 才进行赋值
                                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                    writeMethod.setAccessible(true);
                                }
                                writeMethod.invoke(target, value);
                            }
                        } catch (Throwable ex) {
                            throw new FatalBeanException("Could not copy property '" + targetPd.getName()
                                                         + "' from source to target", ex);
                        }
                    }
                }
            }
        }
    }

}
