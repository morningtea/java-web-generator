package org.mybatis.generator.xsili.plugins;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.xsili.plugins.util.PluginUtils;

/**
 * Service代码生成插件
 * 
 * @since 2.0.0
 * @author 叶鹏
 * @date 2017年8月25日
 */
public class ServicePlugin extends PluginAdapter {

    private String servicePackage;
    private String serviceImplPackage;
    private String project;
    private String modelPackage;

    private FullyQualifiedJavaType pagerType;
    private FullyQualifiedJavaType pagerUtilType;
    private FullyQualifiedJavaType idGeneratorType;

    private FullyQualifiedJavaType slf4jLogger;
    private FullyQualifiedJavaType slf4jLoggerFactory;

    private FullyQualifiedJavaType serviceInterfaceType;
    private FullyQualifiedJavaType serviceType;
    private FullyQualifiedJavaType mapperType;
    private FullyQualifiedJavaType modelType;
    /**
     * 如果有WithBlob, 则modelWithBLOBsType赋值为BlobModel, 否则赋值为BaseModel
     */
    private FullyQualifiedJavaType modelWithBLOBsType;
    private FullyQualifiedJavaType modelCriteriaType;
    private FullyQualifiedJavaType modelSubCriteriaType;
    private FullyQualifiedJavaType BusinessExceptionType;

    private FullyQualifiedJavaType listType;

    private FullyQualifiedJavaType annotationResource;
    private FullyQualifiedJavaType annotationService;

    public ServicePlugin() {
        super();
        slf4jLogger = new FullyQualifiedJavaType("org.slf4j.Logger");
        slf4jLoggerFactory = new FullyQualifiedJavaType("org.slf4j.LoggerFactory");
    }

    @Override
    public boolean validate(List<String> warnings) {
        String pager = properties.getProperty("pager");
        if (StringUtils.isBlank(pager)) {
            throw new RuntimeException("property pager is null");
        } else {
            pagerType = new FullyQualifiedJavaType(pager);
        }

        String pagerUtil = properties.getProperty("pagerUtil");
        if (StringUtils.isBlank(pagerUtil)) {
            throw new RuntimeException("property pagerUtil is null");
        } else {
            pagerUtilType = new FullyQualifiedJavaType(pagerUtil);
        }

        String idGenerator = properties.getProperty("idGenerator");
        if (StringUtils.isNotBlank(idGenerator)) {
            idGeneratorType = new FullyQualifiedJavaType(idGenerator);
        }

        String businessExceptionName = properties.getProperty("businessException");
        if (StringUtils.isNotBlank(businessExceptionName)) {
            BusinessExceptionType = new FullyQualifiedJavaType(businessExceptionName);
        } else {
            BusinessExceptionType = new FullyQualifiedJavaType("java.lang.RuntimeException");
        }

        servicePackage = properties.getProperty("targetPackage");
        serviceImplPackage = properties.getProperty("targetPackageImpl");
        project = properties.getProperty("targetProject");
        modelPackage = context.getJavaModelGeneratorConfiguration().getTargetPackage();

        annotationResource = new FullyQualifiedJavaType("javax.annotation.Resource");
        annotationService = new FullyQualifiedJavaType("org.springframework.stereotype.Service");

        listType = new FullyQualifiedJavaType("java.util.List");

        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        String table = introspectedTable.getBaseRecordType();
        String tableName = table.replaceAll(this.modelPackage + ".", "");

        modelWithBLOBsType = modelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            modelWithBLOBsType = new FullyQualifiedJavaType(introspectedTable.getRecordWithBLOBsType());
        }
        mapperType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        serviceInterfaceType = new FullyQualifiedJavaType(servicePackage + "." + tableName + "Service");
        serviceType = new FullyQualifiedJavaType(serviceImplPackage + "." + tableName + "ServiceImpl");
        modelCriteriaType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        modelSubCriteriaType = new FullyQualifiedJavaType(introspectedTable.getExampleType() + ".Criteria");

        List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
        Interface serviceInterface = new Interface(serviceInterfaceType);
        TopLevelClass topLevelClass = new TopLevelClass(serviceType);
        
        // 导入必要的类
        addImport(serviceInterface, topLevelClass, introspectedTable);
        // 接口
        generateService(serviceInterface, topLevelClass, introspectedTable, files);
        // 实现类
        generateServiceImpl(topLevelClass, introspectedTable, files);

        return files;
    }

    /**
     * 添加接口
     * 
     * @param files
     */
    private void generateService(Interface serviceInterface,
                            TopLevelClass topLevelClass,
                            IntrospectedTable introspectedTable,
                            List<GeneratedJavaFile> files) {
        serviceInterface.setVisibility(JavaVisibility.PUBLIC);

        Method addMethod = createEntity(topLevelClass, introspectedTable);
        addMethod.removeBodyLines();
        serviceInterface.addMethod(addMethod);

        Method updateMethod = updateEntity(topLevelClass, introspectedTable, false);
        updateMethod.removeBodyLines();
        serviceInterface.addMethod(updateMethod);
        
        Method updateSelectiveMethod = updateEntity(topLevelClass, introspectedTable, true);
        updateSelectiveMethod.removeBodyLines();
        serviceInterface.addMethod(updateSelectiveMethod);

        Method deleteMethod = deleteEntity(introspectedTable);
        deleteMethod.removeBodyLines();
        serviceInterface.addMethod(deleteMethod);

        Method getMethod = getEntity(introspectedTable);
        getMethod.removeBodyLines();
        serviceInterface.addMethod(getMethod);

        Method listMethod = listEntitys(topLevelClass, introspectedTable);
        listMethod.removeBodyLines();
        serviceInterface.addMethod(listMethod);

        GeneratedJavaFile file = new GeneratedJavaFile(serviceInterface, project, context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
        files.add(file);
    }

    /**
     * 生成ServiceImpl
     * 
     * @param topLevelClass
     * @param introspectedTable
     * @param files
     */
    private void generateServiceImpl(TopLevelClass topLevelClass,
                                IntrospectedTable introspectedTable,
                                List<GeneratedJavaFile> files) {
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // 设置实现的接口
        topLevelClass.addSuperInterface(serviceInterfaceType);
        // 添加注解
        topLevelClass.addAnnotation("@Service(\""
                                    + PluginUtils.lowerCaseFirstLetter(serviceInterfaceType.getShortName()) + "\")");
        topLevelClass.addImportedType(annotationService);

        // 日志
        addLoggerField(topLevelClass);
        // 添加 Mapper引用
        addMapperField(topLevelClass);

        // 添加基础方法

        Method addMethod = createEntity(topLevelClass, introspectedTable);
        addMethod.addAnnotation("@Override");
        topLevelClass.addMethod(addMethod);

        Method updateMethod = updateEntity(topLevelClass, introspectedTable, false);
        updateMethod.addAnnotation("@Override");
        topLevelClass.addMethod(updateMethod);
        
        Method updateSelectiveMethod = updateEntity(topLevelClass, introspectedTable, true);
        updateSelectiveMethod.addAnnotation("@Override");
        topLevelClass.addMethod(updateSelectiveMethod);

        Method deleteMethod = deleteEntity(introspectedTable);
        deleteMethod.addAnnotation("@Override");
        topLevelClass.addMethod(deleteMethod);

        Method getMethod = getEntity(introspectedTable);
        getMethod.addAnnotation("@Override");
        topLevelClass.addMethod(getMethod);

        Method listMethod = listEntitys(topLevelClass, introspectedTable);
        listMethod.addAnnotation("@Override");
        topLevelClass.addMethod(listMethod);

        // 生成文件
        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project, context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
        files.add(file);
    }

    /**
     * 导入需要的类
     */
    private void addImport(Interface serviceInterface,
                           TopLevelClass topLevelClass,
                           IntrospectedTable introspectedTable) {
        // 导入key类型
        List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
        for (Parameter keyParameter : keyParameterList) {
            if (keyParameter.getName().equals(PluginUtils.PRIMARY_KEY_PARAMETER_NAME)) {
                serviceInterface.addImportedType(keyParameter.getType());
                topLevelClass.addImportedType(keyParameter.getType());
            }
        }

        // 接口
        serviceInterface.addImportedType(modelType);
        serviceInterface.addImportedType(modelWithBLOBsType);
        serviceInterface.addImportedType(pagerType);

        // 实现类
        topLevelClass.addImportedType(slf4jLogger);
        topLevelClass.addImportedType(slf4jLoggerFactory);

        topLevelClass.addImportedType(serviceInterfaceType);
        topLevelClass.addImportedType(mapperType);
        topLevelClass.addImportedType(modelType);
        topLevelClass.addImportedType(modelWithBLOBsType);
        topLevelClass.addImportedType(modelCriteriaType);
        topLevelClass.addImportedType(modelSubCriteriaType);
        topLevelClass.addImportedType(BusinessExceptionType);

        topLevelClass.addImportedType(listType);
        topLevelClass.addImportedType(pagerType);
        topLevelClass.addImportedType(pagerUtilType);

        topLevelClass.addImportedType(annotationService);
        topLevelClass.addImportedType(annotationResource);
    }

    /**
     * add
     * 
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    private Method createEntity(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String modelParamName = PluginUtils.getTypeParamName(modelWithBLOBsType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("create");
        method.setReturnType(modelWithBLOBsType);
        method.addParameter(new Parameter(modelWithBLOBsType, modelParamName));

        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            boolean isPrimaryKey = primaryKeyColumns.contains(introspectedColumn);

            // 非自增主键, 默认使用UUID
            if (isPrimaryKey) {
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
                    method.addBodyLine(modelParamName + PluginUtils.generateSetterCall(introspectedColumn, params));
                }
            } else if ("createTime".equals(introspectedColumn.getJavaProperty())) {
                topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
                method.addBodyLine(modelParamName + ".setCreateTime(new Date());");
            } else if ("updateTime".equals(introspectedColumn.getJavaProperty())) {
                topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
                method.addBodyLine(modelParamName + ".setUpdateTime(new Date());");
            }
        }
        method.addBodyLine("if(this." + getMapper() + "insertSelective(" + modelParamName + ") == 0) {");
        method.addBodyLine("throw new " + BusinessExceptionType.getShortName() + "(\"插入数据库失败\");");
        method.addBodyLine("}");
        method.addBodyLine("return " + modelParamName + ";");
        return method;
    }

    /**
     * update
     * 
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    private Method updateEntity(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, boolean isSelective) {
        String modelParamName = PluginUtils.getTypeParamName(modelWithBLOBsType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        if(isSelective) {
            method.setName("updateSelective");
        } else {
            method.setName("update");
        }
        method.setReturnType(modelWithBLOBsType);
        method.addParameter(new Parameter(modelWithBLOBsType, modelParamName));

        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            // mysql date introspectedColumn.isJDBCDateColumn()
            // mysql time introspectedColumn.isJDBCTimeColumn()
            // mysql dateTime ??

            if ("updateTime".equals(introspectedColumn.getJavaProperty())) {
                topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
                method.addBodyLine(modelParamName + ".setUpdateTime(new Date());");
            }
        }
        if(isSelective) {
            method.addBodyLine("if(this." + getMapper() + "updateByPrimaryKeySelective(" + modelParamName + ") == 0) {");
        } else {
            String mapperUpdateMethod = "";
            if (PluginUtils.hasBLOBColumns(introspectedTable)) {
                mapperUpdateMethod = "updateByPrimaryKeyWithBLOBs";
            } else {
                mapperUpdateMethod = "updateByPrimaryKey";
            }
            method.addBodyLine("if(this." + getMapper() + mapperUpdateMethod + "(" + modelParamName + ") == 0) {");
        }
        method.addBodyLine("throw new " + BusinessExceptionType.getShortName() + "(\"记录不存在\");");
        method.addBodyLine("}");
        method.addBodyLine("return " + modelParamName + ";");

        return method;
    }

    /**
     * delete
     * 
     * @param introspectedTable
     * @return
     */
    private Method deleteEntity(IntrospectedTable introspectedTable) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("delete");
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());

        List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
        for (Parameter keyParameter : keyParameterList) {
            method.addParameter(keyParameter);
        }
        String params = PluginUtils.getCallParameters(keyParameterList);

        method.addBodyLine("return this." + getMapper() + "deleteByPrimaryKey(" + params + ") == 1;");
        return method;
    }

    /**
     * get
     * 
     * @param introspectedTable
     * @return
     */
    private Method getEntity(IntrospectedTable introspectedTable) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("get");
        method.setReturnType(modelWithBLOBsType);

        List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
        for (Parameter keyParameter : keyParameterList) {
            method.addParameter(keyParameter);
        }
        String params = PluginUtils.getCallParameters(keyParameterList);

        StringBuilder sb = new StringBuilder();
        sb.append("return this.").append(getMapper());
        sb.append("selectByPrimaryKey").append("(").append(params).append(");");
        method.addBodyLine(sb.toString());
        return method;
    }

    /**
     * list
     * 
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    private Method listEntitys(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Method method = new Method();
        method.setName("list");
        method.setReturnType(new FullyQualifiedJavaType(pagerType.getShortName() + "<" + modelType.getShortName()
                                                        + ">"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "page"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "limit"));
        method.setVisibility(JavaVisibility.PUBLIC);

        method.addBodyLine(modelCriteriaType.getShortName() + " criteria = new " + modelCriteriaType.getShortName()
                           + "();");
        method.addBodyLine("criteria.setPage(page);");
        method.addBodyLine("criteria.setLimit(limit);");
        method.addBodyLine("@SuppressWarnings(\"unused\")");
        method.addBodyLine(modelSubCriteriaType.getShortName() + " cri = criteria.createCriteria();");

        method.addBodyLine("List<" + modelType.getShortName() + "> list = " + getMapper()
                           + "selectByExample(criteria);");
        method.addBodyLine("return " + pagerUtilType.getShortName() + ".getPager(list, criteria);");
        return method;
    }

    /**
     * count
     * 
     * @param introspectedTable
     * @return
     */
    // private Method countByExample() {
    // Method method = new Method();
    // method.setName("count");
    // method.setReturnType(FullyQualifiedJavaType.getIntInstance());
    // method.addParameter(new Parameter(modelCriteriaType, "criteria"));
    // method.setVisibility(JavaVisibility.PUBLIC);
    // StringBuilder sb = new StringBuilder();
    // sb.append("int count = this.").append(getMapper()).append("countByExample");
    // sb.append("(").append("criteria").append(");");
    // method.addBodyLine(sb.toString());
    // method.addBodyLine("return count;");
    // return method;
    // }

    /**
     * 添加 LOGGER 字段
     */
    private void addLoggerField(TopLevelClass topLevelClass) {
        Field field = new Field();
        field.addAnnotation("@SuppressWarnings(\"unused\")");
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setStatic(true);
        field.setFinal(true);
        field.setType(new FullyQualifiedJavaType("Logger"));
        field.setName("LOGGER");
        // 设置值
        field.setInitializationString("LoggerFactory.getLogger(" + topLevelClass.getType().getShortName() + ".class)");
        topLevelClass.addField(field);
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
