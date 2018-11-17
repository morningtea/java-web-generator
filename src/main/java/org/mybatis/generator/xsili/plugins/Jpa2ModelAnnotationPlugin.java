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
package org.mybatis.generator.xsili.plugins;

import java.util.List;
import java.util.Properties;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.PrimitiveTypeWrapper;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.XsiliJavaBeansUtil;
import org.mybatis.generator.xsili.GenHelper;
import org.mybatis.generator.xsili.plugins.util.PluginUtils;

/**
 * 
 * @author 叶鹏
 * @date 2017年12月12日
 */
public class Jpa2ModelAnnotationPlugin extends PluginAdapter {

    private static final FullyQualifiedJavaType ANNOTATION_CLASS_ENTITY = new FullyQualifiedJavaType("javax.persistence.Entity");
    private static final FullyQualifiedJavaType ANNOTATION_CLASS_TABLE = new FullyQualifiedJavaType("javax.persistence.Table");
    private static final FullyQualifiedJavaType ANNOTATION_CLASS_COLUMN = new FullyQualifiedJavaType("javax.persistence.Column");
    private static final FullyQualifiedJavaType ANNOTATION_CLASS_ID = new FullyQualifiedJavaType("javax.persistence.Id");

    private static final FullyQualifiedJavaType ANNOTATION_CLASS_GENERATED_VALUE = new FullyQualifiedJavaType("javax.persistence.GeneratedValue");
    private static final FullyQualifiedJavaType CLASS_GENERATION_TYPE = new FullyQualifiedJavaType("javax.persistence.GenerationType");

    private static final FullyQualifiedJavaType ANNOTATION_CLASS_ENUMERATED = new FullyQualifiedJavaType("javax.persistence.Enumerated");
    private static final FullyQualifiedJavaType CLASS_ENUM_TYPE = new FullyQualifiedJavaType("javax.persistence.EnumType");

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 添加注解
        topLevelClass.addImportedType(ANNOTATION_CLASS_ENTITY);
        topLevelClass.addAnnotation("@" + ANNOTATION_CLASS_ENTITY.getShortName());

        topLevelClass.addImportedType(ANNOTATION_CLASS_TABLE);
        // introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()
        // introspectedTable.getFullyQualifiedTableNameAtRuntime()
        topLevelClass.addAnnotation("@" + ANNOTATION_CLASS_TABLE.getShortName() + "(name = \""
                                    + introspectedTable.getFullyQualifiedTable().getIntrospectedTableName().toLowerCase()
                                    + "\")");

        // 添加父类继承
        FullyQualifiedJavaType superClassType = GenHelper.getBaseEntityType(getContext());
        if(superClassType != null) {
            topLevelClass.addImportedType(superClassType);
            topLevelClass.setSuperClass(superClassType);
        }
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass,
                                                      IntrospectedTable introspectedTable) {
        return super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelFieldGenerated(Field field,
                                       TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn,
                                       IntrospectedTable introspectedTable,
                                       ModelClassType modelClassType) {
        String createdTimeField = GenHelper.getCreatedTimeField(introspectedTable);
        String updatedTimeField = GenHelper.getUpdatedTimeField(introspectedTable);
        
        if (PluginUtils.isPrimaryKey(introspectedTable, introspectedColumn)) {// 主键
            topLevelClass.addImportedType(ANNOTATION_CLASS_ID);
            field.addAnnotation("@" + ANNOTATION_CLASS_ID.getShortName());

            // 如果主键是Short Integer Long 则添加自增长注解
            // GenerationType.AUTO 由Hibernate通过hibernate_sequence表实现序列自增, 每次创建记录前都需要查询获取当前ID
            // GenerationType.IDENTITY 由数据库本身提供自增长支持
            FullyQualifiedJavaType keyType = PluginUtils.calculateKeyType(introspectedTable);
            if (keyType.equals(PrimitiveTypeWrapper.getShortInstance())
                || keyType.equals(PrimitiveTypeWrapper.getIntegerInstance())
                || keyType.equals(PrimitiveTypeWrapper.getLongInstance())) {

                topLevelClass.addImportedType(ANNOTATION_CLASS_GENERATED_VALUE);
                topLevelClass.addImportedType(CLASS_GENERATION_TYPE);
                field.addAnnotation("@" + ANNOTATION_CLASS_GENERATED_VALUE.getShortName()
                                    + "(strategy = GenerationType.IDENTITY)");
            }
        } else if (createdTimeField.equals(introspectedColumn.getJavaProperty())) {// 创建日期
            topLevelClass.addImportedType(ANNOTATION_CLASS_COLUMN);
            field.addAnnotation("@" + ANNOTATION_CLASS_COLUMN.getShortName() + "(nullable = false, updatable = false)");
        } else if (updatedTimeField.equals(introspectedColumn.getJavaProperty())) {// 更新日期
            topLevelClass.addImportedType(ANNOTATION_CLASS_COLUMN);
            field.addAnnotation("@" + ANNOTATION_CLASS_COLUMN.getShortName() + "(nullable = false)");
        } else if (!introspectedColumn.isNullable()) {// 非空
            topLevelClass.addImportedType(ANNOTATION_CLASS_COLUMN);
            field.addAnnotation("@" + ANNOTATION_CLASS_COLUMN.getShortName() + "(nullable = false)");
        }
        
        // 添加枚举注解
        if (XsiliJavaBeansUtil.isEnumType(field.getType())) {
            topLevelClass.addImportedType(ANNOTATION_CLASS_ENUMERATED);
            topLevelClass.addImportedType(CLASS_ENUM_TYPE);
            field.addAnnotation("@" + ANNOTATION_CLASS_ENUMERATED.getShortName() + "(" + CLASS_ENUM_TYPE.getShortName()
                                + ".STRING)");
        }

        return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

}
