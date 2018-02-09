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
import org.mybatis.generator.xsili.plugins.util.PluginUtils;

/**
 * Mapper测试代码生成插件
 * 
 * @since 2.0.0
 * @author 叶鹏
 * @date 2017年8月25日
 */
public class MybatisMapperTestPlugin extends PluginAdapter {

    private FullyQualifiedJavaType junit;
    private FullyQualifiedJavaType assertType;
    private FullyQualifiedJavaType annotationResource;
    private FullyQualifiedJavaType listType;

    private FullyQualifiedJavaType idGeneratorType;

    private FullyQualifiedJavaType baseModelType;
    /**
     * 如果有WithBlob, 则modelWithBLOBsType赋值为BlobModel, 否则赋值为BaseModel
     */
    private FullyQualifiedJavaType allFieldModelType;
    private FullyQualifiedJavaType modelCriteriaType;
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
    public MybatisMapperTestPlugin() {
        super();
        junit = new FullyQualifiedJavaType("org.junit.Test");
        assertType = new FullyQualifiedJavaType("static org.junit.Assert.assertEquals");
        annotationResource = new FullyQualifiedJavaType("javax.annotation.Resource");
        listType = new FullyQualifiedJavaType("java.util.List");
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable, List<TopLevelClass> modelClasses) {
        List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
        String table = introspectedTable.getBaseRecordType();
        String tableName = table.replaceAll(this.modelPackage + ".", "");

        baseModelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        allFieldModelType = introspectedTable.getRules().calculateAllFieldsClass();
        mapperType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        modelCriteriaType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        mapperTestType = new FullyQualifiedJavaType(targetPackage + "." + tableName + "MapperTest");

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
        // 构建调用方法的key参数
        String keyCallParams = "";
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            FullyQualifiedJavaType keyType = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
            keyCallParams = "(" + keyType.getShortName() + ") " + modelParamName;
        } else {
            List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
            for (Parameter keyParameter : keyParameterList) {
                keyCallParams += modelParamName + ".get" + PluginUtils.upperCaseFirstLetter(keyParameter.getName()) + "(), ";
            }
            if(keyCallParams.length() > 0) {
                keyCallParams = keyCallParams.substring(0, keyCallParams.lastIndexOf(","));
            }
        }

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
        method.addBodyLine("");
        method.addBodyLine("// insertRecord Test");
        method.addBodyLine("assertEquals(1, " + getMapper() + "insertSelective(" + modelParamName + "));");

        method.addBodyLine("");
        method.addBodyLine("// updateRecord Test");
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            String property = introspectedColumn.getJavaProperty();
            if (introspectedColumn.isStringColumn() && !property.toUpperCase().equals("ID")) {
                setStringOrEnumField(introspectedColumn, fields, topLevelClass, method, modelParamName);
            }
            if (introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJDBCTimeColumn()) {
                topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
                method.addBodyLine(modelParamName + ".set" + PluginUtils.upperCaseFirstLetter(property) + "(new Date());");
            }
        }
        method.addBodyLine(getMapper() + "updateByPrimaryKeySelective(" + modelParamName + ");");
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
        method.addBodyLine("// selectRecord Test");
        method.addBodyLine("assertEquals(" + modelParamName + ".getId(), " + getMapper() + "selectByPrimaryKey(" + keyCallParams
                           + ").getId());");

        method.addBodyLine("");
        method.addBodyLine("// selectByExample Test");
        method.addBodyLine(modelCriteriaType.getShortName() + " criteria = new " + modelCriteriaType.getShortName()
                           + "();");
        method.addBodyLine("criteria.createCriteria().andIdEqualTo(" + modelParamName + ".getId());");
        method.addBodyLine("List<" + baseModelType.getShortName() + "> " + baseModelType.getShortName() + "s = " + getMapper()
                           + "selectByExample(criteria);");
        method.addBodyLine("assertEquals(1, " + baseModelType.getShortName() + "s.size());");

        method.addBodyLine("");
        method.addBodyLine("// countByExample Test");
        method.addBodyLine("long connt = " + getMapper() + "countByExample(criteria);");
        method.addBodyLine("assertEquals(1, connt);");

        method.addBodyLine("");
        method.addBodyLine("// page Test");
        method.addBodyLine("criteria.setPage(1);");
        method.addBodyLine("criteria.setLimit(10);");
        method.addBodyLine(baseModelType.getShortName() + "s = " + getMapper() + "selectByExample(criteria);");
        method.addBodyLine("assertEquals(1, " + baseModelType.getShortName() + "s.size());");

        method.addBodyLine("");
        method.addBodyLine("// deleteRecord Test");
        method.addBodyLine("assertEquals(1, " + getMapper() + "deleteByPrimaryKey(" + keyCallParams + "));");
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
        topLevelClass.addImportedType(annotationResource);
        topLevelClass.addImportedType(listType);

        topLevelClass.addImportedType(baseModelType);
        topLevelClass.addImportedType(allFieldModelType);
        topLevelClass.addImportedType(modelCriteriaType);
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
        field.addAnnotation("@Resource");
        topLevelClass.addField(field);
    }

    private String getMapper() {
        return PluginUtils.lowerCaseFirstLetter(mapperType.getShortName()) + ".";
    }

}
