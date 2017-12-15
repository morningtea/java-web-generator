/*
 * Copyright (c) 2016, XSILI and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package org.mybatis.generator.xsili;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.Context;

/**
 * @author 叶鹏
 * @date 2017年12月15日
 */
public class GenHelper {

    public static FullyQualifiedJavaType getBusinessExceptionType(Context context) {
        String businessExceptionName = context.getProperty(Constants.KEY_BUSINESS_EXCEPTION_QUALIFIED_NAME);
        if (StringUtils.isNotBlank(businessExceptionName)) {
            return new FullyQualifiedJavaType(businessExceptionName);
        } else {
            return new FullyQualifiedJavaType("java.lang.RuntimeException");
        }
    }

}
