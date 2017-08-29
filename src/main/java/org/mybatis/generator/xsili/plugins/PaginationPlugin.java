package org.mybatis.generator.xsili.plugins;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
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
        // XmlElement pageElement = new XmlElement("if");
        // pageElement.addAttribute(new Attribute("test", "limit>0"));
        // pageElement.addElement(new TextElement("limit ${start} , ${limit}"));
        // element.addElement(pageElement);

        return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

}
