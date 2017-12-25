package org.mybatis.generator.xsili.plugins.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.XsiliJavaBeansUtil;

public class PluginUtils {

    // 如果生成主键类, 该常量值会被controller引用, 出现在方法参数上
    public static final String PRIMARY_KEY_PARAMETER_NAME = "primaryKey";

    public static String getPropertyNotNull(Context context, String key) {
        String value = context.getProperty(key);
        return value == null ? "" : value;
    }
    
    /**
     * 检查是否包含主键
     * @param introspectedTable
     * @throws RuntimeException if the table without primary key
     */
    public static void checkPrimaryKey(IntrospectedTable introspectedTable) {
        if(introspectedTable.getPrimaryKeyColumns().isEmpty()) {
            throw new RuntimeException(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName().toLowerCase() + " without primary key");
        }
    }
    
    /**
     * 如果column有对应枚举类, 就构建枚举类型参数, 否则构建对应数据类型的参数
     * 
     * @param introspectedColumn
     * @param topLevelClass
     * @param fields
     * @return
     */
    public static Parameter buildParameter(IntrospectedColumn introspectedColumn,
                                           TopLevelClass topLevelClass,
                                           List<Field> fields) {
        String javaProperty = introspectedColumn.getJavaProperty();
        // 枚举字段
        Field enumField = null;
        for (Field field : fields) {
            if (javaProperty.equals(field.getName()) && XsiliJavaBeansUtil.isEnumType(field.getType())) {
                enumField = field;
                break;
            }
        }

        if (enumField != null) {
            topLevelClass.addImportedType(enumField.getType());
            return new Parameter(enumField.getType(), javaProperty);
        } else {
            return new Parameter(introspectedColumn.getFullyQualifiedJavaType(), javaProperty);
        }
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
     * 生成 <b>obj.getter(params)[;]</b> call
     * @param obj
     * @param property
     * @param params
     * @param withSemicolon 是否包含分号
     * @return
     */
    public static String generateGetterCall(String obj, String property, String params, boolean withSemicolon) {
        return generateGetterSetterCall(obj, "get", property, params, withSemicolon);
    }

    /**
     * 生成 <b>obj.setter(params)[;]</b> call
     * @param obj
     * @param property
     * @param params
     * @param withSemicolon 是否包含分号
     * @return
     */
    public static String generateSetterCall(String obj, String property, String params, boolean withSemicolon) {
        return generateGetterSetterCall(obj, "set", property, params, withSemicolon);
    }

    private static String generateGetterSetterCall(String obj,
                                                   String getterSetter,
                                                   String property,
                                                   String params,
                                                   boolean withSemicolon) {
        if(params == null) {
            params = "";
        }
        return obj + "." + getterSetter + upperCaseFirstLetter(property) + "(" + params + ")"
               + (withSemicolon ? ";" : "");
    }

    /**
     * 根据method参数填充model.setXxx代码行
     * 
     * @param modelParamName
     * @param method
     */
    public static void generateModelSetterBodyLine(String modelParamName, Method method, List<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            method.addBodyLine(generateSetterCall(modelParamName, parameter.getName(), parameter.getName(), true));
        }
    }
    
    /**
     * 优先返回fields.field字段类型
     * 
     * @param introspectedColumn
     * @param fields
     * @return
     */
    public static FullyQualifiedJavaType calculateParameterType(IntrospectedColumn introspectedColumn, List<Field> fields) {
        // 枚举字段
        String javaProperty = introspectedColumn.getJavaProperty();
        for (Field field : fields) {
            if (javaProperty.equals(field.getName()) && XsiliJavaBeansUtil.isEnumType(field.getType())) {
                return field.getType();
            }
        }
        return introspectedColumn.getFullyQualifiedJavaType();
    }
    
    /**
     * 如果有单独创建key, 则返回key类型<br>
     * 否则,如果是单主键, 则返回该列对应的主键类型<br>
     * 否则,返回 all field model (Rules#calculateAllFieldsClass())
     * 
     * @return not null
     */
    public static FullyQualifiedJavaType calculateKeyType(IntrospectedTable introspectedTable) {
        // 检查是否包含主键
        checkPrimaryKey(introspectedTable);
        
        if(introspectedTable.getRules().generatePrimaryKeyClass()) {
            return new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
        }

        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        if(primaryKeyColumns.size() == 1) {
            return primaryKeyColumns.get(0).getFullyQualifiedJavaType();
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
        // 检查是否包含主键
        checkPrimaryKey(introspectedTable);

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
