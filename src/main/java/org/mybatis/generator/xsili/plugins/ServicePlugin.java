package org.mybatis.generator.xsili.plugins;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.IntrospectedTable.TargetRuntime;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.xsili.Constants;
import org.mybatis.generator.xsili.GenHelper;
import org.mybatis.generator.xsili.plugins.testplugins.Jpa2RepositoryTestPlugin;
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

    private FullyQualifiedJavaType pageType;
    private FullyQualifiedJavaType idGeneratorType;

    private FullyQualifiedJavaType slf4jLogger;
    private FullyQualifiedJavaType slf4jLoggerFactory;

    private FullyQualifiedJavaType serviceInterfaceType;
    private FullyQualifiedJavaType serviceType;
    private FullyQualifiedJavaType mapperType;
//    private FullyQualifiedJavaType baseModelType;
    /**
     * 如果有WithBlob, 则modelType赋值为BlobModel, 否则赋值为BaseModel
     */
    private FullyQualifiedJavaType allFieldModelType;
    private FullyQualifiedJavaType modelCriteriaType;
    private FullyQualifiedJavaType modelSubCriteriaType;
    private FullyQualifiedJavaType businessExceptionType;

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
        String page = properties.getProperty("page");
        if (StringUtils.isBlank(page)) {
            throw new RuntimeException("property page is null");
        } else {
            pageType = new FullyQualifiedJavaType(page);
        }

        String idGenerator = properties.getProperty("idGenerator");
        if (StringUtils.isNotBlank(idGenerator)) {
            idGeneratorType = new FullyQualifiedJavaType(idGenerator);
        }

        businessExceptionType = GenHelper.getBusinessExceptionType(context);

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
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable, List<TopLevelClass> modelClasses) {
        String table = introspectedTable.getBaseRecordType();
        String tableName = table.replaceAll(this.modelPackage + ".", "");

//        baseModelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        allFieldModelType = introspectedTable.getRules().calculateAllFieldsClass();
        mapperType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        serviceInterfaceType = new FullyQualifiedJavaType(servicePackage + "." + tableName + "Service");
        serviceType = new FullyQualifiedJavaType(serviceImplPackage + "." + tableName + "ServiceImpl");
        modelCriteriaType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        modelSubCriteriaType = new FullyQualifiedJavaType(introspectedTable.getExampleType() + ".Criteria");

        List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
        Interface serviceInterface = new Interface(serviceInterfaceType);
        TopLevelClass serviceImplClass = new TopLevelClass(serviceType);
        
        // 导入必要的类
        addImport(serviceInterface, serviceImplClass, introspectedTable);
        // 接口 & 实现类
        generateFile(serviceInterface, serviceImplClass, introspectedTable, files);

        return files;
    }

    /**
     * 生成Service Interface & Impl
     * 
     * @param serviceInterface
     * @param serviceImplClass
     * @param introspectedTable
     * @param files
     */
    private void generateFile(Interface serviceInterface,
                                     TopLevelClass serviceImplClass,
                                     IntrospectedTable introspectedTable,
                                     List<GeneratedJavaFile> files) {
        // service接口
        serviceInterface.setVisibility(JavaVisibility.PUBLIC);
        
        // service实现类
        serviceImplClass.setVisibility(JavaVisibility.PUBLIC);
        // 设置实现的接口
        serviceImplClass.addSuperInterface(serviceInterfaceType);
        // 添加注解
        serviceImplClass.addAnnotation("@Service(\"" + PluginUtils.lowerCaseFirstLetter(serviceInterfaceType.getShortName()) + "\")");
        serviceImplClass.addImportedType(annotationService);
        // 日志
        addLoggerField(serviceImplClass);
        // 添加 Mapper引用
        addMapperField(serviceImplClass);

        // 方法
        // add
        Method addMethod = createEntity(serviceImplClass, introspectedTable);
        addMethod.removeBodyLines();
        serviceInterface.addMethod(addMethod);
        Method addMethodImpl = createEntity(serviceImplClass, introspectedTable);
        addMethodImpl.addAnnotation("@Override");
        serviceImplClass.addMethod(addMethodImpl);

        // update
        Method updateMethod = updateEntity(serviceImplClass, introspectedTable, false);
        updateMethod.removeBodyLines();
        serviceInterface.addMethod(updateMethod);
        Method updateMethodImpl = updateEntity(serviceImplClass, introspectedTable, false);
        updateMethodImpl.addAnnotation("@Override");
        serviceImplClass.addMethod(updateMethodImpl);
        
        // updateSelective
        if(introspectedTable.getTargetRuntime() != TargetRuntime.JPA2) {
            Method updateSelectiveMethod = updateEntity(serviceImplClass, introspectedTable, true);
            updateSelectiveMethod.removeBodyLines();
            serviceInterface.addMethod(updateSelectiveMethod);
            Method updateSelectiveMethodImpl = updateEntity(serviceImplClass, introspectedTable, true);
            updateSelectiveMethodImpl.addAnnotation("@Override");
            serviceImplClass.addMethod(updateSelectiveMethodImpl);
        }

        // deletePhysically
        Method deletePhysicallyMethod = deletePhysicallyEntity(introspectedTable);
        deletePhysicallyMethod.removeBodyLines();
        serviceInterface.addMethod(deletePhysicallyMethod);
        Method deletePhysicallyMethodImpl = deletePhysicallyEntity(introspectedTable);
        deletePhysicallyMethodImpl.addAnnotation("@Override");
        serviceImplClass.addMethod(deletePhysicallyMethodImpl);
        
        // deleteLogically
        Method deleteLogicallyMethod = deleteLogicallyEntity(introspectedTable, serviceImplClass);
        if (deleteLogicallyMethod != null) {
            deleteLogicallyMethod.removeBodyLines();
            serviceInterface.addMethod(deleteLogicallyMethod);
        }
        Method deleteLogicallyMethodImpl = deleteLogicallyEntity(introspectedTable, serviceImplClass);
        if (deleteLogicallyMethodImpl != null) {
            deleteLogicallyMethodImpl.addAnnotation("@Override");
            serviceImplClass.addMethod(deleteLogicallyMethodImpl);
        }

        // get
        Method getMethod = getEntity(introspectedTable);
        getMethod.removeBodyLines();
        serviceInterface.addMethod(getMethod);
        Method getMethodImpl = getEntity(introspectedTable);
        getMethodImpl.addAnnotation("@Override");
        serviceImplClass.addMethod(getMethodImpl);

        // list
        Method listMethod = listEntity(introspectedTable, serviceImplClass);
        listMethod.removeBodyLines();
        serviceInterface.addMethod(listMethod);
        Method listMethodImpl = listEntity(introspectedTable, serviceImplClass);
        listMethodImpl.addAnnotation("@Override");
        serviceImplClass.addMethod(listMethodImpl);

        // 生成文件

        GeneratedJavaFile interfaceFile = new GeneratedJavaFile(serviceInterface, project, context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
        files.add(interfaceFile);
        
        GeneratedJavaFile implFile = new GeneratedJavaFile(serviceImplClass, project, context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
        files.add(implFile);
    }
    

    /**
     * 导入需要的类
     */
    private void addImport(Interface serviceInterface,
                           TopLevelClass serviceImplClass,
                           IntrospectedTable introspectedTable) {
        // 导入key类型
        List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
        for (Parameter keyParameter : keyParameterList) {
            if (keyParameter.getName().equals(PluginUtils.PRIMARY_KEY_PARAMETER_NAME)) {
                serviceInterface.addImportedType(keyParameter.getType());
                serviceImplClass.addImportedType(keyParameter.getType());
            }
        }

        // 接口
//        serviceInterface.addImportedType(baseModelType);
        serviceInterface.addImportedType(allFieldModelType);
        serviceInterface.addImportedType(pageType);

        // 实现类
        serviceImplClass.addImportedType(slf4jLogger);
        serviceImplClass.addImportedType(slf4jLoggerFactory);

        serviceImplClass.addImportedType(serviceInterfaceType);
        serviceImplClass.addImportedType(mapperType);
//        serviceImplClass.addImportedType(baseModelType);
        serviceImplClass.addImportedType(allFieldModelType);

        serviceImplClass.addImportedType(annotationService);
        serviceImplClass.addImportedType(annotationResource);
    }

    
    /**
     * add
     * 
     * @param serviceImplClass
     * @param introspectedTable
     * @return
     */
    private Method createEntity(TopLevelClass serviceImplClass, IntrospectedTable introspectedTable) {
        String modelParamName = PluginUtils.getTypeParamName(allFieldModelType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("create");
        method.setReturnType(allFieldModelType);
        method.addParameter(new Parameter(allFieldModelType, modelParamName));

        String createdDateName = PluginUtils.getPropertyNotNull(getContext(), Constants.KEY_CREATED_DATE_NAME);
        String updatedDateName = PluginUtils.getPropertyNotNull(getContext(), Constants.KEY_UPDATED_DATE_NAME);
        
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            if (PluginUtils.isPrimaryKey(introspectedTable, introspectedColumn)) {
                // if (!introspectedColumn.isIdentity() && !introspectedColumn.isAutoIncrement()) {
                // }
                // 字符类型主键
                if (introspectedColumn.isStringColumn() && idGeneratorType != null) {
                    serviceImplClass.addImportedType(idGeneratorType);
                    String params = idGeneratorType.getShortName() + ".generateId()";
                    method.addBodyLine(modelParamName + PluginUtils.generateSetterCall(introspectedColumn, params));
                }
            } else if (createdDateName.equals(introspectedColumn.getJavaProperty())) {
                serviceImplClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
                method.addBodyLine(modelParamName + ".set" + PluginUtils.upperCaseFirstLetter(createdDateName) + "(new Date());");
            } else if (updatedDateName.equals(introspectedColumn.getJavaProperty())) {
                serviceImplClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
                method.addBodyLine(modelParamName + ".set" + PluginUtils.upperCaseFirstLetter(updatedDateName) + "(new Date());");
            }
        }
        
        if(introspectedTable.getTargetRuntime() == TargetRuntime.JPA2) {
            method.addBodyLine("return this." + getMapper() + getMapperMethodName(introspectedTable, "create") + "(" + modelParamName + ");");
        } else {
            serviceImplClass.addImportedType(businessExceptionType);
            
            method.addBodyLine("if(this." + getMapper() + getMapperMethodName(introspectedTable, "create") + "(" + modelParamName + ") == 0) {");
            method.addBodyLine("throw new " + businessExceptionType.getShortName() + "(\"插入数据库失败\");");
            method.addBodyLine("}");
            method.addBodyLine("return " + modelParamName + ";");
        }
        
        return method;
    }
    

    /**
     * update
     * 
     * @param serviceImplClass
     * @param introspectedTable
     * @return
     */
    private Method updateEntity(TopLevelClass serviceImplClass, IntrospectedTable introspectedTable, boolean isSelective) {
        String modelParamName = PluginUtils.getTypeParamName(allFieldModelType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        if(isSelective) {
            method.setName("updateSelective");
        } else {
            method.setName("update");
        }
        method.setReturnType(allFieldModelType);
        method.addParameter(new Parameter(allFieldModelType, modelParamName));

        String updatedDateName = PluginUtils.getPropertyNotNull(getContext(), Constants.KEY_UPDATED_DATE_NAME);
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            // mysql date introspectedColumn.isJDBCDateColumn()
            // mysql time introspectedColumn.isJDBCTimeColumn()
            // mysql dateTime ??

            if (updatedDateName.equals(introspectedColumn.getJavaProperty())) {
                serviceImplClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
                method.addBodyLine(modelParamName + ".set" + PluginUtils.upperCaseFirstLetter(updatedDateName) + "(new Date());");
            }
        }
        
        String mapperMethodName = "";
        if(isSelective) {
            mapperMethodName = getMapperMethodName(introspectedTable, "updateSelective");
        } else {
            mapperMethodName = getMapperMethodName(introspectedTable, "update");
        }
        
        if(introspectedTable.getTargetRuntime() == TargetRuntime.JPA2) {
            method.addBodyLine("return this." + getMapper() + mapperMethodName + "(" + modelParamName + ");");
        } else {
            serviceImplClass.addImportedType(businessExceptionType);
            
            method.addBodyLine("if(this." + getMapper() + mapperMethodName + "(" + modelParamName + ") == 0) {");
            method.addBodyLine("throw new " + businessExceptionType.getShortName() + "(\"记录不存在\");");
            method.addBodyLine("}");
            method.addBodyLine("return " + modelParamName + ";");
        }

        return method;
    }
    
    
    /**
     * 如果包含逻辑删除列, 则返回逻辑删除的方法, 否则返回null
     * 
     * @param introspectedTable
     * @return maybe null
     */
    private Method deleteLogicallyEntity(IntrospectedTable introspectedTable, TopLevelClass serviceImplClass) {
        IntrospectedColumn logicDeletedColumn = GenHelper.getLogicDeletedField(introspectedTable);
        if(logicDeletedColumn == null) {
            return null;
        }
        
        Method method = new Method();
        method.setName("deleteLogically");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addJavaDocLine("/** 逻辑删除 */");
        
        // 导入依赖类
        serviceImplClass.addImportedType(new FullyQualifiedJavaType("java.util.Optional"));
        
        List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
        for (Parameter keyParameter : keyParameterList) {
            method.addParameter(keyParameter);
        }
        String params = PluginUtils.getCallParameters(keyParameterList);
        
        // 填充key
        if(introspectedTable.getTargetRuntime() == TargetRuntime.JPA2) {
            String keyParams = prepareCallByKey(introspectedTable, method, method.getParameters());
            if(StringUtils.isNotBlank(keyParams)) {
                params = keyParams;
            }
        }
        
        String modelParamName = "exist";
        if (introspectedTable.getTargetRuntime() == TargetRuntime.JPA2) {
            method.addBodyLine("Optional<" + allFieldModelType.getShortName() + "> " + modelParamName + " = this."
                               + getMapper() + getMapperMethodName(introspectedTable, "get") + "(" + params + ");");
            method.addBodyLine(modelParamName + ".ifPresent((v) -> {");
            method.addBodyLine("v.setIsDeleted(true);");
            method.addBodyLine("this.update(v);");
            method.addBodyLine("});");
        } else {
            method.addBodyLine(allFieldModelType.getShortName() + " " + modelParamName + " = this." + getMapper()
                               + getMapperMethodName(introspectedTable, "get") + "(" + params + ");");
            method.addBodyLine("if(" + modelParamName + " != null) {");
            method.addBodyLine(modelParamName + ".setIsDeleted(true);");
            method.addBodyLine("this.update(" + modelParamName + ");");
            method.addBodyLine("}");
        }
        
        return method;
    }
    
    

    /**
     * deletePhysically
     * 
     * @param introspectedTable
     * @return
     */
    private Method deletePhysicallyEntity(IntrospectedTable introspectedTable) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("deletePhysically");
        method.addJavaDocLine("/** 物理删除 */");

        List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
        for (Parameter keyParameter : keyParameterList) {
            method.addParameter(keyParameter);
        }
        String params = PluginUtils.getCallParameters(keyParameterList);
        
        // 填充key
        if(introspectedTable.getTargetRuntime() == TargetRuntime.JPA2) {
            String keyParams = prepareCallByKey(introspectedTable, method, method.getParameters());
            if(StringUtils.isNotBlank(keyParams)) {
                params = keyParams;
            }
        }
        method.addBodyLine("this." + getMapper() + getMapperMethodName(introspectedTable, "delete") + "(" + params + ");");
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
        method.setReturnType(allFieldModelType);

        List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
        for (Parameter keyParameter : keyParameterList) {
            method.addParameter(keyParameter);
        }
        String params = PluginUtils.getCallParameters(keyParameterList);

        if(introspectedTable.getTargetRuntime() == TargetRuntime.JPA2) {
            String keyParams = prepareCallByKey(introspectedTable, method, method.getParameters());
            if(StringUtils.isNotBlank(keyParams)) {
                params = keyParams;
            }
            method.addBodyLine("return this." + getMapper() + getMapperMethodName(introspectedTable, "get") + "(" + params + ").orElse(null);");
        } else {
            method.addBodyLine("return this." + getMapper() + getMapperMethodName(introspectedTable, "get") + "(" + params + ");");
        }
        return method;
    }
    

    /**
     * list
     * 
     * @param serviceImplClass
     * @param introspectedTable
     * @return
     */
    private Method listEntity(IntrospectedTable introspectedTable, TopLevelClass serviceImplClass) {
        if(introspectedTable.getTargetRuntime() == TargetRuntime.JPA2) {
            return listEntityJpa2(introspectedTable, serviceImplClass);
        } else {
            return listEntityMybatis(introspectedTable, serviceImplClass);
        }
    }
    
    
    /**
     * list mybatis
     * 
     * @param serviceImplClass
     * @param introspectedTable
     * @return
     */
    private Method listEntityMybatis(IntrospectedTable introspectedTable, TopLevelClass serviceImplClass) {
        Method method = new Method();
        method.setName("list");
        method.setReturnType(new FullyQualifiedJavaType(pageType.getShortName() + "<" + allFieldModelType.getShortName()
                                                        + ">"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "pageNum"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "pageSize"));
        method.setVisibility(JavaVisibility.PUBLIC);

        // 导入类
        serviceImplClass.addImportedType(modelCriteriaType);
        serviceImplClass.addImportedType(modelSubCriteriaType);
        serviceImplClass.addImportedType(listType);
        serviceImplClass.addImportedType(pageType);
        
        method.addBodyLine(modelCriteriaType.getShortName() + " criteria = new " + modelCriteriaType.getShortName()
                           + "();");
        method.addBodyLine("criteria.setPage(pageNum);");
        method.addBodyLine("criteria.setLimit(pageSize);");
        method.addBodyLine("@SuppressWarnings(\"unused\")");
        method.addBodyLine(modelSubCriteriaType.getShortName() + " cri = criteria.createCriteria();");

        method.addBodyLine("List<" + allFieldModelType.getShortName() + "> list = " + getMapper()
                           + "selectByExample(criteria);");
        method.addBodyLine("return " + pageType.getShortName() + ".buildPage(list, criteria);");
        return method;
    }
    

    /**
     * list jpa2
     * 
     * @param serviceImplClass
     * @param introspectedTable
     * @return
     */
    private Method listEntityJpa2(IntrospectedTable introspectedTable, TopLevelClass serviceImplClass) {
        Method method = new Method();
        method.setName("list");
        method.setReturnType(new FullyQualifiedJavaType(pageType.getShortName() + "<" + allFieldModelType.getShortName()
                                                        + ">"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "pageNum"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "pageSize"));
        method.setVisibility(JavaVisibility.PUBLIC);

        // 导入类
        serviceImplClass.addImportedType(pageType);
        serviceImplClass.addImportedType(new FullyQualifiedJavaType("com.querydsl.core.types.Predicate"));
        serviceImplClass.addImportedType(new FullyQualifiedJavaType("org.springframework.data.domain.Page"));
        serviceImplClass.addImportedType(new FullyQualifiedJavaType("org.springframework.data.domain.PageRequest"));
        serviceImplClass.addImportedType(new FullyQualifiedJavaType("org.springframework.data.domain.Pageable"));
        serviceImplClass.addImportedType(new FullyQualifiedJavaType("org.springframework.data.domain.Sort"));
        serviceImplClass.addImportedType(new FullyQualifiedJavaType("org.springframework.data.domain.Sort.Direction"));
        
        method.addBodyLine("// 通过ExpressionUtils构建Predicate查询条件");
        method.addBodyLine("Predicate predicate = null;");
        
        method.addBodyLine("");
        method.addBodyLine("Pageable pageable = PageRequest.of(pageNum, pageSize, new Sort(Direction.DESC, \"id\"));");

        method.addBodyLine("");
        method.addBodyLine("Page<" + allFieldModelType.getShortName() + "> page = null;");
        method.addBodyLine("if(predicate != null) {");
        method.addBodyLine("page = " + getMapper() + "findAll(predicate, pageable);");
        method.addBodyLine("} else {");
        method.addBodyLine("page = " + getMapper() + "findAll(pageable);");
        method.addBodyLine("}");
        
        method.addBodyLine("return " + pageType.getShortName() + ".buildPage(page);");
        
        return method;
    }
    

    /**
     * 添加 LOGGER 字段
     */
    private void addLoggerField(TopLevelClass serviceImplClass) {
        Field field = new Field();
        field.addAnnotation("@SuppressWarnings(\"unused\")");
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setStatic(true);
        field.setFinal(true);
        field.setType(new FullyQualifiedJavaType("Logger"));
        field.setName("LOGGER");
        // 设置值
        field.setInitializationString("LoggerFactory.getLogger(" + serviceImplClass.getType().getShortName() + ".class)");
        serviceImplClass.addField(field);
    }
    

    /**
     * 添加 Mapper依赖字段
     */
    private void addMapperField(TopLevelClass serviceImplClass) {
        serviceImplClass.addImportedType(mapperType);

        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(mapperType);
        field.setName(PluginUtils.lowerCaseFirstLetter(mapperType.getShortName()));
        field.addAnnotation("@Resource");
        serviceImplClass.addField(field);
    }

    private String getMapper() {
        return PluginUtils.lowerCaseFirstLetter(mapperType.getShortName()) + ".";
    }
    
    
    /**
     * 
     * @param introspectedTable
     * @param type create update updateSelective delete get
     * @return
     */
    private String getMapperMethodName(IntrospectedTable introspectedTable, String type) {
        if (type.equals("create")) {
            return introspectedTable.getTargetRuntime() == TargetRuntime.JPA2 ? "save" : "insertSelective";
        } else if (type.equals("update")) {
            if (introspectedTable.getTargetRuntime() == TargetRuntime.JPA2) {
                return "saveAndFlush";
            } else {
                if (PluginUtils.hasBLOBColumns(introspectedTable)) {
                    return "updateByPrimaryKeyWithBLOBs";
                } else {
                    return "updateByPrimaryKey";
                }
            }
        } else if (type.equals("updateSelective")) {
            if (introspectedTable.getTargetRuntime() == TargetRuntime.JPA2) {
                // TODO jpa2 updateSelective
                return "saveAndFlush";
            } else {
                return "updateByPrimaryKeySelective";
            }
        } else if (type.equals("delete")) {
            return introspectedTable.getTargetRuntime() == TargetRuntime.JPA2 ? "deleteById" : "deleteByPrimaryKey";
        } else if (type.equals("get")) {
            return introspectedTable.getTargetRuntime() == TargetRuntime.JPA2 ? "findById" : "selectByPrimaryKey";
        } else {
            throw new IllegalArgumentException("参数type错误, type: " + type);
        }
    }
    

    /**
     * 如果没有生成主键类, 并且是复合主键, 则以allFieldModel填充复合主键, 并返回调用参数<br>
     * {@link Jpa2RepositoryTestPlugin#prepareCallByKey}
     * @param introspectedTable
     * @param caller
     * @return
     */
    private String prepareCallByKey(IntrospectedTable introspectedTable, Method caller, List<Parameter> keyParameters) {
        // 检查是否包含主键
        PluginUtils.checkPrimaryKey(introspectedTable);
        
        if (!introspectedTable.getRules().generatePrimaryKeyClass()) {
            List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
            if (primaryKeyColumns.size() > 1) {
                String modelParamName = PluginUtils.getTypeParamName(allFieldModelType);
                // 填充key参数
                caller.addBodyLine(allFieldModelType.getShortName() + " " + modelParamName + " = new "
                                   + allFieldModelType.getShortName() + "();");
                PluginUtils.generateModelSetterBodyLine(modelParamName, caller, keyParameters);
                // call param
                return modelParamName;
            }
        }
        return null;
    }
    
}
