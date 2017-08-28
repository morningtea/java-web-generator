package org.mybatis.generator.plugins;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * 分页插件,<br>
 * 添加生成代码到 Criteria 和 mapper.xml
 * 
 * @since 2.0.0
 * @author 叶鹏
 * @date 2017年8月25日
 */
public class PaginationPlugin extends PluginAdapter {

    private FullyQualifiedJavaType queryParamType;

    public boolean validate(List<String> warnings) {
        String queryParam = properties.getProperty("queryParam");
        if (StringUtils.isBlank(queryParam)) {
            throw new RuntimeException("property queryParam is null");
        } else {
            queryParamType = new FullyQualifiedJavaType(queryParam);
        }

        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 现在集成 xsili-mybatis-plugin-page 分页插件, change to extends QueryParam
        topLevelClass.addImportedType(queryParamType);
        topLevelClass.setSuperClass(queryParamType);

        // add field, getter, setter for Criteria
        // addLimit(topLevelClass, introspectedTable, "page");
        // addLimit(topLevelClass, introspectedTable, "limit");

        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
                                                                     IntrospectedTable introspectedTable) {
        // 现在集成 xsili-mybatis-plugin-page 分页插件, 不需要拼接sql

        // add start limit to sql
        // XmlElement isNotNullElement = new XmlElement("if");
        // isNotNullElement.addAttribute(new Attribute("test", "limit>0"));
        // isNotNullElement.addElement(new TextElement("limit ${start} , ${limit}"));
        // element.addElement(isNotNullElement);

        return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    @SuppressWarnings("unused")
    private void addLimit(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String name) {
        // property
        CommentGenerator commentGenerator = context.getCommentGenerator();
        Field field = new Field();
        field.setVisibility(JavaVisibility.PROTECTED);
        field.setType(FullyQualifiedJavaType.getIntInstance());
        field.setName(name);
        field.setInitializationString("-1");
        commentGenerator.addFieldComment(field, introspectedTable);
        topLevelClass.addField(field);
        
        char c = name.charAt(0);
        String camel = Character.toUpperCase(c) + name.substring(1);
        // set
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("set" + camel);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), name));
        method.addBodyLine("this." + name + "=" + name + ";");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);
        
        // get
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName("get" + camel);
        method.addBodyLine("return " + name + ";");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);
    }

}
