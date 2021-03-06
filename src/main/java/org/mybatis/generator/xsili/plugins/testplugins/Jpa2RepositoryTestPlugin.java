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
package org.mybatis.generator.xsili.plugins.testplugins;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.XsiliJavaBeansUtil;
import org.mybatis.generator.xsili.GenHelper;
import org.mybatis.generator.xsili.plugins.ServicePlugin;
import org.mybatis.generator.xsili.plugins.util.PluginUtils;

/**
 * jpa Repository测试代码生成插件
 * 
 * @since 2.0.0
 * @author 叶鹏
 * @date 2017年12月12日
 */
public class Jpa2RepositoryTestPlugin extends PluginAdapter {

    private FullyQualifiedJavaType junit;
    private FullyQualifiedJavaType assertType;
    private FullyQualifiedJavaType annotationAutowired;
    private FullyQualifiedJavaType listType;

    private FullyQualifiedJavaType idGeneratorType;

//    private FullyQualifiedJavaType baseModelType;
    /**
     * 如果有WithBlob, 则modelWithBLOBsType赋值为BlobModel, 否则赋值为BaseModel
     */
    private FullyQualifiedJavaType allFieldModelType;
    private FullyQualifiedJavaType mapperType;
    private FullyQualifiedJavaType mapperTestType;

    private String targetProject;
    private String targetPackage;
    private String modelPackage;
    private String superTestCase;

    @Override
    public boolean validate(List<String> warnings) {
        targetProject = properties.getProperty("targetProject");
        targetPackage = properties.getProperty("targetPackage");
        superTestCase = properties.getProperty("superTestCase");
        if (StringUtils.isBlank(superTestCase)) {
            throw new RuntimeException("property superTestCase is null");
        }
        String idGenerator = properties.getProperty("idGenerator");
        if (StringUtils.isNotBlank(idGenerator)) {
            idGeneratorType = new FullyQualifiedJavaType(idGenerator);
        }
        modelPackage = context.getJavaModelGeneratorConfiguration().getTargetPackage();
        return true;
    }

    // 初始化
    public Jpa2RepositoryTestPlugin() {
        super();
        junit = new FullyQualifiedJavaType("org.junit.Test");
        assertType = new FullyQualifiedJavaType("static org.junit.Assert.assertEquals");
        annotationAutowired = new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired");
        listType = new FullyQualifiedJavaType("java.util.List");
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable, List<TopLevelClass> modelClasses) {
        List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
        String table = introspectedTable.getBaseRecordType();
        String tableName = table.replaceAll(this.modelPackage + ".", "");

//        baseModelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        allFieldModelType = introspectedTable.getRules().calculateAllFieldsClass();
        mapperType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        mapperTestType = new FullyQualifiedJavaType(targetPackage + "." + tableName + "RepositoryTest");

        TopLevelClass topLevelClass = new TopLevelClass(mapperTestType);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        topLevelClass.setSuperClass(superTestCase);

        // 导入必要的类
        addImport(topLevelClass, introspectedTable);

        // 添加 Mapper引用
        addMapperField(topLevelClass);

        // 添加基础方法
        topLevelClass.addMethod(addCRUD(topLevelClass, introspectedTable, modelClasses));

        // 生成文件
        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, targetProject, context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
        files.add(file);
        return files;
    }

    /**
     * CRUD测试方法
     * 
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    private Method addCRUD(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, List<TopLevelClass> modelClasses) {
        Method method = new Method();
        method.addAnnotation("@Test");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("void"));
        method.setName("crudTest");

        String modelParamName = PluginUtils.getTypeParamName(allFieldModelType);
        method.addBodyLine(allFieldModelType.getShortName() + " " + modelParamName + " = new "
                           + allFieldModelType.getShortName() + "();");

        String createdTimeField = GenHelper.getCreatedTimeField(introspectedTable);
        String updatedTimeField = GenHelper.getUpdatedTimeField(introspectedTable);
        List<Field> fields = new ArrayList<>();
        for (TopLevelClass modelClass : modelClasses) {
            fields.addAll(modelClass.getFields());
        }
                
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            String javaProperty = introspectedColumn.getJavaProperty();
            // 非自增主键, 默认使用UUID
            if (PluginUtils.isPrimaryKey(introspectedTable, introspectedColumn)) {
                if (!introspectedColumn.isIdentity() && !introspectedColumn.isAutoIncrement()) {
                    String params = "";
                    if (introspectedColumn.isStringColumn()) {
                        if (idGeneratorType != null) {
                            topLevelClass.addImportedType(idGeneratorType);
                            params = idGeneratorType.getShortName() + ".generateId()";
                        } else {
                            params = "";
                        }
                    } else {
                        // 字符串以外的类型, 设置为null, 需要用户手动修改
                        params = "null";
                    }
                    method.addBodyLine(PluginUtils.generateSetterCall(modelParamName, javaProperty, params, true));
                }
            } else if (createdTimeField.equals(javaProperty)) {
                topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
                method.addBodyLine(modelParamName + ".set" + PluginUtils.upperCaseFirstLetter(createdTimeField) + "(new Date());");
            } else if (updatedTimeField.equals(javaProperty)) {
                topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
                method.addBodyLine(modelParamName + ".set" + PluginUtils.upperCaseFirstLetter(updatedTimeField) + "(new Date());");
            } else if (introspectedColumn.isStringColumn()) {
                setStringOrEnumField(introspectedColumn, fields, topLevelClass, method, modelParamName);
            }
        }

        method.addBodyLine("// insertRecord Test");
        method.addBodyLine(getMapper() + "save(" + modelParamName + ");");

        method.addBodyLine("");
        method.addBodyLine("// updateRecord Test");
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            String javaProperty = introspectedColumn.getJavaProperty();
            if (introspectedColumn.isStringColumn() && !javaProperty.toUpperCase().equals("ID")) {
                setStringOrEnumField(introspectedColumn, fields, topLevelClass, method, modelParamName);
            }
            if (introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJDBCTimeColumn()) {
                topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
                method.addBodyLine(modelParamName + ".set" + PluginUtils.upperCaseFirstLetter(javaProperty) + "(new Date());");
            }
        }
        method.addBodyLine(getMapper() + "saveAndFlush(" + modelParamName + ");");
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            String javaProperty = introspectedColumn.getJavaProperty();
            if (introspectedColumn.isStringColumn() && !javaProperty.toUpperCase().equals("ID")) {
                String param = "";
                FullyQualifiedJavaType parameterType = PluginUtils.calculateParameterType(introspectedColumn, fields);
                if (XsiliJavaBeansUtil.isEnumType(parameterType)) {
                    topLevelClass.addImportedType(parameterType);
                    param = parameterType.getShortName() + ".values()[0]";
                } else {
                    param = "\"" + PluginUtils.upperCaseFirstLetter(javaProperty) + "\"";
                }
                
                method.addBodyLine("assertEquals(" + param + ", "
                    + modelParamName + ".get" + PluginUtils.upperCaseFirstLetter(javaProperty) + "());");
            }
        }

        method.addBodyLine("");
        method.addBodyLine("// findById Test");
        // 构建调用方法的key参数
        String keyCallParams = "";
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            FullyQualifiedJavaType keyType = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
            keyCallParams = "(" + keyType.getShortName() + ") " + modelParamName;
        } else {
            List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
            // 复合主键
            String entityAsKey = prepareCallByEntityAsKey(introspectedTable, method, keyParameterList);
            if(StringUtils.isNotBlank(entityAsKey)) {
                keyCallParams = entityAsKey;
            } else {
                for (Parameter keyParameter : keyParameterList) {
                    String keyPart = modelParamName + ".get" + PluginUtils.upperCaseFirstLetter(keyParameter.getName()) + "()";
                    keyCallParams += keyPart + ", ";
                }
                if(keyCallParams.length() > 0) {
                    keyCallParams = keyCallParams.substring(0, keyCallParams.lastIndexOf(","));
                }
            }
        }
        
        method.addBodyLine("assertEquals(" + modelParamName + ".getId(), " + getMapper() + "findById(" + keyCallParams + ").get().getId());");

        method.addBodyLine("");
        method.addBodyLine("// findAll Test");
        method.addBodyLine("List<" + allFieldModelType.getShortName() + "> list = " + getMapper()
                           + "findAll();");
        method.addBodyLine("assertEquals(1, list.size());");

        method.addBodyLine("");
        method.addBodyLine("// count Test");
        method.addBodyLine("long connt = " + getMapper() + "count();");
        method.addBodyLine("assertEquals(1, connt);");

        method.addBodyLine("");
        method.addBodyLine("// page Test");
        method.addBodyLine("list = " + getMapper() + "findAll();");
        method.addBodyLine("assertEquals(1, " + "list.size());");

        method.addBodyLine("");
        method.addBodyLine("// deleteById Test");
        method.addBodyLine(getMapper() + "deleteById(" + keyCallParams + ");");
        method.addBodyLine("connt = " + getMapper() + "count();");
        method.addBodyLine("assertEquals(0, connt);");
        return method;
    }
    
    /**
     * 代码生成 - table1.setAuditStatus(AuditStatusEnum.values()[0]);
     * 
     * @param introspectedColumn
     * @param fields
     * @param topLevelClass
     * @param method
     * @param modelParamName
     */
    private void setStringOrEnumField(IntrospectedColumn introspectedColumn,
                               List<Field> fields,
                               TopLevelClass topLevelClass,
                               Method method,
                               String modelParamName) {
        String javaProperty = introspectedColumn.getJavaProperty();
        
        String param = "";
        FullyQualifiedJavaType parameterType = PluginUtils.calculateParameterType(introspectedColumn, fields);
        if (XsiliJavaBeansUtil.isEnumType(parameterType)) {
            topLevelClass.addImportedType(parameterType);
            param = parameterType.getShortName() + ".values()[0]";
        } else {
            param = "\"" + javaProperty + "\"";
        }

        method.addBodyLine(modelParamName + ".set" + PluginUtils.upperCaseFirstLetter(javaProperty) + "(" + param + ");");
    }

    private void addImport(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 导入key类型
        List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
        for (Parameter keyParameter : keyParameterList) {
            if (keyParameter.getName().equals(PluginUtils.PRIMARY_KEY_PARAMETER_NAME)) {
                topLevelClass.addImportedType(keyParameter.getType());
            }
        }

        topLevelClass.addImportedType(junit);
        topLevelClass.addImportedType(assertType);
        topLevelClass.addImportedType(annotationAutowired);
        topLevelClass.addImportedType(listType);

//        topLevelClass.addImportedType(baseModelType);
        topLevelClass.addImportedType(allFieldModelType);
        topLevelClass.addImportedType(mapperType);
        topLevelClass.addImportedType(superTestCase);
    }

    /**
     * 添加 Mapper依赖字段
     */
    private void addMapperField(TopLevelClass topLevelClass) {
        topLevelClass.addImportedType(mapperType);

        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(mapperType);
        field.setName(PluginUtils.lowerCaseFirstLetter(mapperType.getShortName()));
        field.addAnnotation("@Autowired");
        topLevelClass.addField(field);
    }

    private String getMapper() {
        return PluginUtils.lowerCaseFirstLetter(mapperType.getShortName()) + ".";
    }
    
    /**
     * 如果没有生成主键类, 并且是复合主键, 则以allFieldModel填充复合主键, 并返回调用参数<br>
     * {@link ServicePlugin#prepareCallByEntityAsKey}
     * @param introspectedTable
     * @param caller
     * @return
     */
    private String prepareCallByEntityAsKey(IntrospectedTable introspectedTable, Method caller, List<Parameter> keyParameters) {
        // 检查是否包含主键
        PluginUtils.checkPrimaryKey(introspectedTable);

        if (!introspectedTable.getRules().generatePrimaryKeyClass()) {
            List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
            if (primaryKeyColumns.size() > 1) {
                String modelKeyParamName = PluginUtils.getTypeParamName(allFieldModelType) + "Key";
                // 填充key参数
                caller.addBodyLine(allFieldModelType.getShortName() + " " + modelKeyParamName + " = new "
                                   + allFieldModelType.getShortName() + "();");
                for (Parameter parameter : keyParameters) {
                    String keyPart = modelKeyParamName + ".get" + PluginUtils.upperCaseFirstLetter(parameter.getName()) + "()";
                    caller.addBodyLine(modelKeyParamName + ".set" + PluginUtils.upperCaseFirstLetter(parameter.getName()) + "("
                                       + PluginUtils.lowerCaseFirstLetter(keyPart) + ");");
                }
                
                // call param
                return modelKeyParamName;
            }
        }
        return null;
    }
}
