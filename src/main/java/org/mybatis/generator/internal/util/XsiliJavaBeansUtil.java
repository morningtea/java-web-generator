/**
 *    Copyright 2006-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.java.TopLevelEnumeration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.xsili.GenHelper;
import org.mybatis.generator.xsili.plugins.util.PluginUtils;

/**
 * 
 * @author 叶鹏
 * @date 2017年12月15日
 */
public class XsiliJavaBeansUtil extends JavaBeansUtil {

    private XsiliJavaBeansUtil() {
        super();
    }

    private static final Pattern ENUM_PATTERN = Pattern.compile("\\{\\s*?enum:(.*)\\}");
    private static final Pattern ENUM_ITEM_PATTERN = Pattern.compile("(.*?)\\((.*?)\\)");
    private static final String ENUM_ITEM_SEPARATOR = ",|，";
    
    public static final String ENUM_CLASS_SUFFIX = "Enum";

    public static boolean isEnumType(FullyQualifiedJavaType javaType) {
        return javaType.getFullyQualifiedName().endsWith(XsiliJavaBeansUtil.ENUM_CLASS_SUFFIX);
    }

    /**
     * 去除匹配枚举格式的注释
     * 
     * @param remarks
     * @return not null
     */
    public static String excludeEnumRemarks(String remarks) {
        if (remarks == null) {
            return "";
        }

        Matcher m = ENUM_PATTERN.matcher(remarks);
        if (m.find()) {
            String fullMatchedRemarks = m.group(0);
            return remarks.replace(fullMatchedRemarks, "");
        }

        return remarks;
    }
    
    /**
     * 针对以下格式的注释生成枚举类，支持中/英文逗号分隔<br>
     * {enum: UNAUDITED(待审核), AUDIT_PASS(审核通过), AUDIT_NOT_PASS(审核未通过)}
     * 
     * @param introspectedColumn
     * @return maybe null
     */
    public static TopLevelEnumeration getJavaBeansFieldEnum(TopLevelClass topLevelClass,
                                                            IntrospectedColumn introspectedColumn,
                                                            Context context,
                                                            IntrospectedTable introspectedTable) {
        String remarks = introspectedColumn.getRemarks();
        if (remarks == null) {
            return null;
        }

        Matcher m = ENUM_PATTERN.matcher(remarks);
        if (m.find()) {
            String matchedRemarks = m.group(1);
            String[] rowArr = matchedRemarks.split(ENUM_ITEM_SEPARATOR);
            List<String> enumItemList = new ArrayList<>();
            for (int i = 0; i < rowArr.length; i++) {
                String enumItem = rowArr[i].trim();
                if (StringUtils.isNotBlank(enumItem)) {
                    enumItemList.add(enumItem);
                }
            }

            // 定义枚举类
            if (!enumItemList.isEmpty()) {
                String enumPackage = "";
                String modelPackage = topLevelClass.getType().getPackageName();
                if (StringUtils.isBlank(modelPackage)) {
                    enumPackage = "enums";
                } else {
                    if (modelPackage.lastIndexOf(".") == -1) {
                        enumPackage = modelPackage + ".enums";
                    } else {
                        enumPackage = modelPackage.substring(0, modelPackage.lastIndexOf(".")) + ".enums";
                    }
                }

                String enumClassName = enumPackage + "."
                                       + PluginUtils.upperCaseFirstLetter(introspectedColumn.getJavaProperty())
                                       + ENUM_CLASS_SUFFIX;
                FullyQualifiedJavaType enumClassType = new FullyQualifiedJavaType(enumClassName);
                TopLevelEnumeration topLevelEnumeration = new TopLevelEnumeration(enumClassType);
                topLevelEnumeration.setVisibility(JavaVisibility.PUBLIC);
                
                CommentGenerator commentGenerator = context.getCommentGenerator();
                commentGenerator.addEnumComment(topLevelEnumeration, introspectedTable);

                for (String item : enumItemList) {
                    Matcher macherItem = ENUM_ITEM_PATTERN.matcher(item);
                    if (macherItem.find()) {
                        String name = macherItem.group(1).trim();
                        // TODO 添加枚举name注释
                        String remark = macherItem.group(2).trim();
                        topLevelEnumeration.addEnumConstant(name);
                    }

                }

                Method getMethod = new Method();
                getMethod.setName("getEnum");
                getMethod.setVisibility(JavaVisibility.PUBLIC);
                getMethod.setStatic(true);
                getMethod.setReturnType(enumClassType);
                getMethod.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "name"));
                getMethod.addBodyLine("for (" + enumClassType.getShortName() + " type : " + enumClassType.getShortName()
                                      + ".values()) {");
                getMethod.addBodyLine("if (type.name().equals(name)) {");
                getMethod.addBodyLine("return type;");
                getMethod.addBodyLine("}");
                getMethod.addBodyLine("}");
                getMethod.addBodyLine("return null;");
                topLevelEnumeration.addMethod(getMethod);

                FullyQualifiedJavaType businessExceptionType = GenHelper.getBusinessExceptionType(context);
                topLevelEnumeration.addImportedType(businessExceptionType);
                Method checkMethod = new Method();
                checkMethod.setName("checkEnum");
                checkMethod.setVisibility(JavaVisibility.PUBLIC);
                checkMethod.setStatic(true);
                checkMethod.setReturnType(enumClassType);
                checkMethod.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "name"));
                checkMethod.addBodyLine(enumClassType.getShortName() + " enums = " + "getEnum(name);");
                checkMethod.addBodyLine("if (enums == null) {");
                checkMethod.addBodyLine("throw new " + businessExceptionType.getShortName() + "(\"请选择正确的枚举类型, "
                                        + enumItemList + "\");");
                checkMethod.addBodyLine("}");
                checkMethod.addBodyLine("return enums;");

                topLevelEnumeration.addMethod(checkMethod);

                return topLevelEnumeration;
            }
        }

        return null;
    }
    
    /**
     * 
     * @param introspectedColumn
     * @param context
     * @param introspectedTable
     * @param fieldEnumeration 如果非空, 则把字段type声明成该枚举
     * @return
     */
    public static Field getJavaBeansField(IntrospectedColumn introspectedColumn,
                                          Context context,
                                          IntrospectedTable introspectedTable,
                                          TopLevelEnumeration fieldEnumeration) {
        FullyQualifiedJavaType fqjt = null;
        if(fieldEnumeration != null) {
            fqjt = fieldEnumeration.getType();
        } else {
            fqjt = introspectedColumn.getFullyQualifiedJavaType();
        }
        String property = introspectedColumn.getJavaProperty();

        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(fqjt);
        field.setName(property);
        context.getCommentGenerator().addFieldComment(field, introspectedTable, introspectedColumn);

        return field;
    }
    
    /**
     * 
     * @param introspectedColumn
     * @param context
     * @param introspectedTable
     * @param fieldEnumeration 如果非空, 则把字段type声明成该枚举
     * @return
     */
    public static Method getJavaBeansGetter(IntrospectedColumn introspectedColumn,
                                            Context context,
                                            IntrospectedTable introspectedTable,
                                            TopLevelEnumeration fieldEnumeration) {
        FullyQualifiedJavaType fqjt = null;
        if(fieldEnumeration != null) {
            fqjt = fieldEnumeration.getType();
        } else {
            fqjt = introspectedColumn.getFullyQualifiedJavaType();
        }
        String property = introspectedColumn.getJavaProperty();

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(fqjt);
        method.setName(getGetterMethodName(property, fqjt));
        context.getCommentGenerator().addGetterComment(method, introspectedTable, introspectedColumn);

        StringBuilder sb = new StringBuilder();
        sb.append("return "); //$NON-NLS-1$
        sb.append(property);
        sb.append(';');
        method.addBodyLine(sb.toString());

        return method;
    }

    /**
     * 
     * @param introspectedColumn
     * @param context
     * @param introspectedTable
     * @param fieldEnumeration 如果非空, 则把字段type声明成该枚举
     * @return
     */
    public static Method getJavaBeansSetter(IntrospectedColumn introspectedColumn,
                                            Context context,
                                            IntrospectedTable introspectedTable,
                                            TopLevelEnumeration fieldEnumeration) {
        FullyQualifiedJavaType fqjt = null;
        if(fieldEnumeration != null) {
            fqjt = fieldEnumeration.getType();
        } else {
            fqjt = introspectedColumn.getFullyQualifiedJavaType();
        }
        String property = introspectedColumn.getJavaProperty();

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(getSetterMethodName(property));
        method.addParameter(new Parameter(fqjt, property));
        context.getCommentGenerator().addSetterComment(method, introspectedTable, introspectedColumn);

        StringBuilder sb = new StringBuilder();
        if (fieldEnumeration == null && introspectedColumn.isStringColumn() && isTrimStringsEnabled(introspectedColumn)) {
            sb.append("this."); //$NON-NLS-1$
            sb.append(property);
            sb.append(" = "); //$NON-NLS-1$
            sb.append(property);
            sb.append(" == null ? null : "); //$NON-NLS-1$
            sb.append(property);
            sb.append(".trim();"); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
        } else {
            sb.append("this."); //$NON-NLS-1$
            sb.append(property);
            sb.append(" = "); //$NON-NLS-1$
            sb.append(property);
            sb.append(';');
            method.addBodyLine(sb.toString());
        }

        return method;
    }

}
