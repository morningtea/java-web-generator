package org.mybatis.generator.xsili.plugins.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.config.Context;

public class PluginUtils {

    // 如果生成主键类, 该常量值会被controller引用, 出现在方法参数上
    public static final String PRIMARY_KEY_PARAMETER_NAME = "primaryKey";

    public static String getPropertyNotNull(Context context, String key) {
        String value = context.getProperty(key);
        return value == null ? "" : value;
    }
    
    /**
     * 
     * @param introspectedTable
     * @return
     */
    public static boolean hasBLOBColumns(IntrospectedTable introspectedTable) {
        return CollectionUtils.isNotEmpty(introspectedTable.getBLOBColumns());
    }
    
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
     * 根据method参数填充model.setXxx代码行
     * 
     * @param modelParamName
     * @param method
     */
    public static void generateModelSetterBodyLine(String modelParamName, Method method, List<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            method.addBodyLine(modelParamName + ".set" + upperCaseFirstLetter(parameter.getName()) + "("
                               + parameter.getName() + ");");
        }
    }
    
    /**
     * 如果有单独创建key, 则返回key类型<br>
     * 否则,如果是单主键, 则返回该列对应的主键类型<br>
     * 否则,返回 all field model (Rules#calculateAllFieldsClass())
     * 
     * @return not null
     */
    public static FullyQualifiedJavaType calculateKeyType(IntrospectedTable introspectedTable) {
        if(introspectedTable.getRules().generatePrimaryKeyClass()) {
            return new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
        }

        List<IntrospectedColumn> columns = introspectedTable.getPrimaryKeyColumns();
        if(columns.size() == 1) {
            return columns.get(0).getFullyQualifiedJavaType();
        } else {
            return introspectedTable.getRules().calculateAllFieldsClass();
        }
    }

    /**
     * 获取主键参数<br>
     * 生成的主键类, 或者列主键
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
     * 通过equals进行判断
     * @param introspectedTable
     * @param introspectedColumn
     * @return
     */
    public static boolean isPrimaryKey(IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        // introspectedColumn.isIdentity() 并不是主键的意思
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        return primaryKeyColumns.contains(introspectedColumn);
    }

    /**
     * 从声明参数拼接调用参数, 如: (String name, int age) --> name, age
     * 
     * @param list
     * @return
     */
    public static String getCallParameters(List<Parameter> list) {
    	if(CollectionUtils.isEmpty(list)) {
    		return "";
    	}
    	
        StringBuffer paramsBuf = new StringBuffer();
        for (Parameter parameter : list) {
            paramsBuf.append(parameter.getName());
            paramsBuf.append(", ");
        }
        paramsBuf.setLength(paramsBuf.lastIndexOf(","));
        return paramsBuf.toString();
    }

    /**
     * java.lang.Object --> object
     * @param javaType
     * @return
     */
    public static String getTypeParamName(FullyQualifiedJavaType javaType) {
        return PluginUtils.lowerCaseFirstLetter(javaType.getShortName());
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

}
