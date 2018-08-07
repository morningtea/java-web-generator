/*
 * Copyright (c) 2016, XSILI and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package org.mybatis.generator.xsili;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.xsili.plugins.util.PluginUtils;

/**
 * @author 叶鹏
 * @date 2017年12月15日
 */
public class GenHelper {
    
    /**
     * 是否包含逻辑删除列
     * 
     * @param introspectedTable
     * @return
     */
    public static boolean hasLogicDeletedField(IntrospectedTable introspectedTable) {
    	String fieldName = getLogicDeletedField(introspectedTable);
        return StringUtils.isNotBlank(fieldName);
    }

    /**
     * 获取逻辑删除列
     * 
     * @param introspectedTable
     * @return maybe null
     */
    public static IntrospectedColumn getLogicDeletedColumn(IntrospectedTable introspectedTable) {
        String fieldName = getLogicDeletedField(introspectedTable);
        return getFieldByName(introspectedTable, fieldName);
    }
    
    /**
     * 获取逻辑删除列
     * 
     * @param introspectedTable
     * @return maybe null
     */
    public static IntrospectedColumn getOwnerColumn(IntrospectedTable introspectedTable) {
        String fieldName = PluginUtils.getPropertyNotNull(introspectedTable.getContext(), Constants.KEY_OWNER_FIELD);
        return getFieldByName(introspectedTable, fieldName);
    }
    
    /**
     * 
     * @param introspectedTable
     * @param fieldName 允许为空, 为空则返回null
     * @return
     */
    private static IntrospectedColumn getFieldByName(IntrospectedTable introspectedTable, String fieldName) {
        if (StringUtils.isBlank(fieldName)) {
            return null;
        }

        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.getJavaProperty().equals(fieldName)) {
                return column;
            }
        }
        return null;
    }

    public static String getLogicDeletedField(IntrospectedTable introspectedTable) {
    	return PluginUtils.getPropertyNotNull(introspectedTable.getContext(), Constants.KEY_LOGIC_DELETED_FIELD);
    }

    public static String getCreatedTimeField(IntrospectedTable introspectedTable) {
        return PluginUtils.getPropertyNotNull(introspectedTable.getContext(), Constants.KEY_CREATED_TIME_FIELD);
    }

    public static String getUpdatedTimeField(IntrospectedTable introspectedTable) {
        return PluginUtils.getPropertyNotNull(introspectedTable.getContext(), Constants.KEY_UPDATED_TIME_FIELD);
    }

    public static FullyQualifiedJavaType getBusinessExceptionType(Context context) {
        String businessExceptionName = context.getProperty(Constants.KEY_BUSINESS_EXCEPTION_QUALIFIED_NAME);
        if (StringUtils.isNotBlank(businessExceptionName)) {
            return new FullyQualifiedJavaType(businessExceptionName);
        } else {
            return new FullyQualifiedJavaType("java.lang.RuntimeException");
        }
    }
    
    

}
