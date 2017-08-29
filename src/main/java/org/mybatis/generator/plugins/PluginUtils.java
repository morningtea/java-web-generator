package org.mybatis.generator.plugins;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

public class PluginUtils {

    public static final String PRIMARY_KEY_PARAMETER_NAME = "key";

    /**
     * 生成 <b>.getter(params);</b> call
     * 
     * @return
     */
    public static String generateGetterCall(IntrospectedColumn introspectedColumn, String params) {
        return generateGetterSetterCall("get", introspectedColumn, params);
    }

    /**
     * 生成 <b>.setter(params);</b> call
     * 
     * @return
     */
    public static String generateSetterCall(IntrospectedColumn introspectedColumn, String params) {
        return generateGetterSetterCall("set", introspectedColumn, params);
    }

    private static String generateGetterSetterCall(String getterSetter,
                                                   IntrospectedColumn introspectedColumn,
                                                   String params) {
        return "." + getterSetter + upperCaseFirstLetter(introspectedColumn.getJavaProperty()) + "(" + params + ");";
    }

    /**
     * 如果有单独创建key, 则返回key类型
     * 
     * @return
     */
    public static FullyQualifiedJavaType getKeyClass(IntrospectedTable introspectedTable) {
        // 导入key类型
        List<Parameter> keyParameterList = getPrimaryKeyParameters(introspectedTable);
        for (Parameter parameter : keyParameterList) {
            if (parameter.getName().equals(PluginUtils.PRIMARY_KEY_PARAMETER_NAME)) {
                return parameter.getType();
            }
        }
        return null;
    }

    /**
     * 获取主键参数
     * 
     * @param introspectedTable
     * @return
     */
    public static List<Parameter> getPrimaryKeyParameters(IntrospectedTable introspectedTable) {
        List<Parameter> list = new ArrayList<>();
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
            list.add(new Parameter(type, PRIMARY_KEY_PARAMETER_NAME));
        } else {
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                list.add(new Parameter(type, introspectedColumn.getJavaProperty()));
            }
        }
        return list;
    }

    /**
     * 从声明参数拼接调用参数, 如: (String name, int age) --> name,age
     * 
     * @param list
     * @return
     */
    public static String getCallParameters(List<Parameter> list) {
        StringBuffer paramsBuf = new StringBuffer();
        for (Parameter parameter : list) {
            paramsBuf.append(parameter.getName());
            paramsBuf.append(",");
        }
        paramsBuf.setLength(paramsBuf.length() - 1);
        return paramsBuf.toString();
    }

    /**
     * 根据method参数填充setXxx代码行
     * 
     * @param modelParamName
     * @param method
     */
    public static void addSetFieldBodyLine(String modelParamName, Method method) {
        for (Parameter parameter : method.getParameters()) {
            method.addBodyLine(modelParamName + ".set" + upperCaseFirstLetter(parameter.getName()) + "("
                               + parameter.getName() + ");");
        }
    }

    /**
     * 驼峰命名 转换成 连字符, <br>
     * firstName --> first-name
     * 
     * @param str
     * @return
     */
    public static String humpToEnDash(String hump) {
        String str = lowerCaseFirstLetter(hump);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append("-").append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 把首字母转换成小写
     * 
     * @param str
     * @return
     */
    public static String lowerCaseFirstLetter(String str) {
        StringBuilder sb = new StringBuilder(str);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * 把首字母转换成大写
     * 
     * @param str
     * @return
     */
    public static String upperCaseFirstLetter(String str) {
        StringBuilder sb = new StringBuilder(str);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    public static String getTypeParamName(FullyQualifiedJavaType javaType) {
        return PluginUtils.lowerCaseFirstLetter(javaType.getShortName());
    }

}
