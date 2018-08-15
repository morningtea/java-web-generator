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
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.xsili.GenHelper;
import org.mybatis.generator.xsili.plugins.util.PluginUtils;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Spring MVC Controller 代码生成插件
 * 
 * @since 2.0.0
 * @author 叶鹏
 * @date 2017年8月26日
 */
public class SpringMvcControllerPlugin extends PluginAdapter {

    private boolean enableRestful = true;
    private boolean enablePut = true;
    private boolean enablePatch = false;
    private boolean enableSwaggerAnnotation = true;
    private FullyQualifiedJavaType abstractControllerType;
    private FullyQualifiedJavaType resultModelType;
    private FullyQualifiedJavaType validatorUtilType;
    private FullyQualifiedJavaType pageType;

    private FullyQualifiedJavaType controllerType;
    private FullyQualifiedJavaType serviceType;
    private FullyQualifiedJavaType baseModelType;
    /**
     * 如果有WithBlob, 则modelType赋值为BlobModel, 否则赋值为BaseModel
     */
    private FullyQualifiedJavaType allFieldModelType;

    private String targetPackage;
    private String targetPackageService;
    private String project;
    private String modelPackage;

    private FullyQualifiedJavaType annotationResource;
    private FullyQualifiedJavaType annotationController;
    private FullyQualifiedJavaType annotationRequestMapping;
    private FullyQualifiedJavaType annotationRequestMethod;
    private FullyQualifiedJavaType annotationPathVariable;
    private FullyQualifiedJavaType annotationRequestParam;
    private FullyQualifiedJavaType annotationApiOperation;

    public SpringMvcControllerPlugin() {
        super();
    }

    @Override
    public boolean validate(List<String> warnings) {
        String enableRestfulStr = properties.getProperty("enableRestful");
        if (StringUtils.isBlank(enableRestfulStr)) {
            throw new RuntimeException("缺少配置项enableRestful");
        } else {
            this.enableRestful = Boolean.parseBoolean(enableRestfulStr);
        }
        
        String enablePutStr = properties.getProperty("enablePut");
        if (StringUtils.isBlank(enablePutStr)) {
            throw new RuntimeException("缺少配置项enablePut");
        } else {
            this.enablePut = Boolean.parseBoolean(enablePutStr);
        }
        
        String enablePatchStr = properties.getProperty("enablePatch");
        if (StringUtils.isBlank(enablePatchStr)) {
            throw new RuntimeException("缺少配置项enablePatch");
        } else {
            this.enablePatch = Boolean.parseBoolean(enablePatchStr);
        }
        
        String enableSwaggerAnnotationStr = properties.getProperty("enableSwaggerAnnotation");
        if (StringUtils.isBlank(enableSwaggerAnnotationStr)) {
            throw new RuntimeException("缺少配置项enableSwaggerAnnotation");
        } else {
            this.enableSwaggerAnnotation = Boolean.parseBoolean(enableSwaggerAnnotationStr);
        }

        String abstractController = properties.getProperty("abstractController");
        if (StringUtils.isBlank(abstractController)) {
            throw new RuntimeException("property abstractController is null");
        } else {
            this.abstractControllerType = new FullyQualifiedJavaType(abstractController);
        }

        String resultModel = properties.getProperty("resultModel");
        if (StringUtils.isBlank(resultModel)) {
            throw new RuntimeException("property resultModel is null");
        }
        this.resultModelType = new FullyQualifiedJavaType(resultModel);

        String page = properties.getProperty("page");
        if (StringUtils.isBlank(page)) {
            throw new RuntimeException("property page is null");
        } else {
            this.pageType = new FullyQualifiedJavaType(page);
        }

        String validatorUtil = properties.getProperty("validatorUtil");
        if (StringUtils.isNotBlank(validatorUtil)) {
            this.validatorUtilType = new FullyQualifiedJavaType(validatorUtil);
        }

        this.targetPackage = properties.getProperty("targetPackage");
        this.targetPackageService = properties.getProperty("targetPackageService");
        this.project = properties.getProperty("targetProject");
        this.modelPackage = context.getJavaModelGeneratorConfiguration().getTargetPackage();

        this.annotationResource = new FullyQualifiedJavaType("javax.annotation.Resource");
        this.annotationController = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RestController");
        this.annotationRequestMapping = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMapping");
        this.annotationRequestMethod = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMethod");
        this.annotationPathVariable = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.PathVariable");
        this.annotationRequestParam = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestParam");
        this.annotationApiOperation = new FullyQualifiedJavaType("io.swagger.annotations.ApiOperation");

        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable, List<TopLevelClass> modelClasses) {
        // 检查是否包含主键
        PluginUtils.checkPrimaryKey(introspectedTable);
        
        String table = introspectedTable.getBaseRecordType();
        String tableName = table.replaceAll(this.modelPackage + ".", "");

        baseModelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        allFieldModelType = introspectedTable.getRules().calculateAllFieldsClass();
        serviceType = new FullyQualifiedJavaType(targetPackageService + "." + tableName + "Service");
        controllerType = new FullyQualifiedJavaType(targetPackage + "." + tableName + "Controller");

        TopLevelClass topLevelClass = new TopLevelClass(controllerType);
        
        // 导入必要的类
        addImport(topLevelClass, introspectedTable);
        
        // 实现类
        List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
        generateController(topLevelClass, introspectedTable, modelClasses, files);

        return files;
    }

    /**
     * 生成Controller
     * 
     * @param topLevelClass
     * @param introspectedTable
     * @param files
     */
    private void generateController(TopLevelClass topLevelClass,
                               IntrospectedTable introspectedTable,
                               List<TopLevelClass> modelClasses,
                               List<GeneratedJavaFile> files) {
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // 父类
        if (abstractControllerType != null) {
            topLevelClass.setSuperClass(abstractControllerType);
        }
        // 添加注解
        topLevelClass.addAnnotation("@RestController");
        topLevelClass.addAnnotation("@RequestMapping(\"/v1/" + baseModelType.getShortName().toLowerCase() + "s" + "\")");

        // 添加 Mapper引用
        addServiceField(topLevelClass);

        // 添加基础方法

        Method addMethod = createEntity(topLevelClass, introspectedTable, modelClasses);
        topLevelClass.addMethod(addMethod);

        if (this.enablePut) {
            Method updateMethod = updateEntity(topLevelClass, introspectedTable, modelClasses, false);
            topLevelClass.addMethod(updateMethod);
        }

        if (this.enablePatch) {
            Method updateSelectiveMethod = updateEntity(topLevelClass, introspectedTable, modelClasses, true);
            topLevelClass.addMethod(updateSelectiveMethod);
        }

        Method deleteMethod = deleteEntity(introspectedTable);
        topLevelClass.addMethod(deleteMethod);

        Method getMethod = getEntity(introspectedTable);
        topLevelClass.addMethod(getMethod);

        Method listMethod = listEntitys(topLevelClass, introspectedTable);
        topLevelClass.addMethod(listMethod);

        // 生成文件
        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project, context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
        files.add(file);
    }

    /**
     * 导入需要的类
     */
    private void addImport(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 导入key类型
        List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
        for (Parameter keyParameter : keyParameterList) {
            if (keyParameter.getName().equals(PluginUtils.PRIMARY_KEY_PARAMETER_NAME)) {
                topLevelClass.addImportedType(keyParameter.getType());
            }
        }

        topLevelClass.addImportedType(pageType);
        if (abstractControllerType != null) {
            topLevelClass.addImportedType(abstractControllerType);
        }
        if (validatorUtilType != null) {
            topLevelClass.addImportedType(validatorUtilType);
        }

        topLevelClass.addImportedType(resultModelType);
        topLevelClass.addImportedType(serviceType);
//        topLevelClass.addImportedType(baseModelType);
        topLevelClass.addImportedType(allFieldModelType);

        topLevelClass.addImportedType(annotationResource);
        topLevelClass.addImportedType(annotationController);
        topLevelClass.addImportedType(annotationRequestMapping);
        topLevelClass.addImportedType(annotationRequestMethod);
        topLevelClass.addImportedType(annotationRequestParam);
        if (enableRestful) {
            topLevelClass.addImportedType(annotationPathVariable);
        }
        if (enableSwaggerAnnotation) {
            topLevelClass.addImportedType(annotationApiOperation);
        }
    }

    /**
     * add
     * 
     * @param topLevelClass
     * @param introspectedTable
     * @param modelClasses
     * @return
     */
    private Method createEntity(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, List<TopLevelClass> modelClasses) {
        String modelParamName = PluginUtils.getTypeParamName(allFieldModelType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("create");
        method.setReturnType(resultModelType);
        if (this.enableRestful) {
            method.addAnnotation(getRestfulRequestMappingAnnotation(null, RequestMethod.POST));
        } else {
            method.addAnnotation("@RequestMapping(value = \"/create\", method = RequestMethod.POST)");
        }

        // swagger
        if (enableSwaggerAnnotation) {
            method.addAnnotation("@ApiOperation(value = \"新增\", notes = \"\", response = "
                                 + resultModelType.getShortName() + ".class)");
        }

        // 添加方法参数
        String createdTimeField = GenHelper.getCreatedTimeField(introspectedTable);
        String updatedTimeField = GenHelper.getUpdatedTimeField(introspectedTable);
        String logicDeletedField = GenHelper.getLogicDeletedField(introspectedTable);
        List<Field> fields = new ArrayList<>();
        for (TopLevelClass modelClass : modelClasses) {
            fields.addAll(modelClass.getFields());
        }
        
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            String javaProperty = introspectedColumn.getJavaProperty();
            // 排除主键, 创建时间, 更新时间, 逻辑删除
            if (!PluginUtils.isPrimaryKey(introspectedTable, introspectedColumn) 
            		&& !createdTimeField.equals(javaProperty)
            		&& !updatedTimeField.equals(javaProperty) 
            		&& !logicDeletedField.equals(javaProperty)) {
                Parameter parameter = PluginUtils.buildParameter(introspectedColumn, topLevelClass, fields);
                parameter.addAnnotation("@RequestParam(required = false)");
                method.addParameter(parameter);
            }
        }

        // 填充参数
        method.addBodyLine("// 填充参数");
        StringBuilder sb = new StringBuilder();
        sb.append(allFieldModelType.getShortName() + " " + modelParamName);
        sb.append(" = ");
        sb.append("new ").append(allFieldModelType.getShortName()).append("();");
        method.addBodyLine(sb.toString());
        PluginUtils.generateModelSetterBodyLine(modelParamName, method, method.getParameters());

        if (validatorUtilType != null) {
            method.addBodyLine("");
            method.addBodyLine("// 校验参数");
            method.addBodyLine("ValidatorUtil.checkParams(" + modelParamName + ");");
        }

        method.addBodyLine("");
        method.addBodyLine(modelParamName + " = this." + getService() + "create(" + modelParamName + ");");
        method.addBodyLine("return super.success(" + modelParamName + ");");

        return method;
    }
    
    /**
     * update
     * 
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    private Method updateEntity(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, List<TopLevelClass> modelClasses, boolean isSelective) {
        String methodName = isSelective ? "updateSelective" : "update";
        String modelParamName = PluginUtils.getTypeParamName(allFieldModelType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(methodName);
        method.setReturnType(resultModelType);
        if (this.enableRestful) {
            if (isSelective) {
                method.addAnnotation(getRestfulRequestMappingAnnotation(introspectedTable, RequestMethod.PATCH));
            } else {
                method.addAnnotation(getRestfulRequestMappingAnnotation(introspectedTable, RequestMethod.PUT));
            }
        } else {
            method.addAnnotation("@RequestMapping(value = \"/" + PluginUtils.humpToEnDash(methodName) + "\", method = RequestMethod.POST)");
        }

        // swagger
        if (enableSwaggerAnnotation) {
            String doc = isSelective ? "更新部分字段" : "更新全部字段";
            method.addAnnotation("@ApiOperation(value = \"" + doc + "\", notes = \"\", response = "
                                 + resultModelType.getShortName() + ".class)");
        }

        // 添加方法参数(主键)
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        List<Parameter> keyParameters = addKeyParameters(method, primaryKeyColumns);

        // 添加方法参数
        String createdTimeField = GenHelper.getCreatedTimeField(introspectedTable);
        String updatedTimeField = GenHelper.getUpdatedTimeField(introspectedTable);
        String logicDeletedField = GenHelper.getLogicDeletedField(introspectedTable);
        List<Field> fields = new ArrayList<>();
        for (TopLevelClass modelClass : modelClasses) {
            fields.addAll(modelClass.getFields());
        }
        
        List<Parameter> notKeyParameters = new ArrayList<>();
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            String javaProperty = introspectedColumn.getJavaProperty();
            if (!PluginUtils.isPrimaryKey(introspectedTable, introspectedColumn) 
            		&& !createdTimeField.equals(javaProperty) 
            		&& !updatedTimeField.equals(javaProperty)
            		&& !logicDeletedField.equals(javaProperty)) {
                Parameter parameter = PluginUtils.buildParameter(introspectedColumn, topLevelClass, fields);
                parameter.addAnnotation("@RequestParam(required = false)");
                method.addParameter(parameter);
                notKeyParameters.add(parameter);
            }
        }

        // 填充参数
        if(introspectedTable.getTargetRuntime() == TargetRuntime.JPA2) {
            // 查询记录, jpa2默认更新entity的全部字段, 所以先查询数据库后赋值
            method.addBodyLine("// 查询记录");
            String keyParams = prepareCallByKey(introspectedTable, method, keyParameters);
            method.addBodyLine(allFieldModelType.getShortName() + " " + modelParamName + " = this." + getService() + "get(" + keyParams + ");");
            method.addBodyLine("if(" + modelParamName + " == null) {");
            method.addBodyLine("return super.error(\"记录不存在\");");
            method.addBodyLine("}");
            
            method.addBodyLine("");
            method.addBodyLine("// 填充参数");
            // 填充参数 notKey
            if(isSelective) {
                PluginUtils.generateModelSetterBodyLineNullVerify(modelParamName, method, notKeyParameters, topLevelClass);
            } else {
                PluginUtils.generateModelSetterBodyLine(modelParamName, method, notKeyParameters);
            }
        } else {
            method.addBodyLine("// 填充参数");
            method.addBodyLine(allFieldModelType.getShortName() + " " + modelParamName + " = new " + allFieldModelType.getShortName() + "();");
            // 填充参数 key
            PluginUtils.generateModelSetterBodyLine(modelParamName, method, keyParameters);
            // 填充参数 notKey
            PluginUtils.generateModelSetterBodyLine(modelParamName, method, notKeyParameters);
        }

        // 校验参数
        if (validatorUtilType != null) {
            method.addBodyLine("");
            method.addBodyLine("// 校验参数");
            method.addBodyLine("ValidatorUtil.checkParams(" + modelParamName + ");");
        }
        // 校验所有者
        IntrospectedColumn ownerColumn = GenHelper.getOwnerColumn(introspectedTable);
        if(ownerColumn != null) {
            // checkOwner
            method.addBodyLine("// 校验所有者");
            method.addBodyLine("super.checkOwner(" + PluginUtils.generateGetterCall(modelParamName, ownerColumn.getJavaProperty(), null, false) + ");");
        }
        
        // 调用Service
        method.addBodyLine("");
        if (isSelective) {
            String serviceMethodName = "";
            if(introspectedTable.getTargetRuntime() == TargetRuntime.JPA2) {// jpa2 统一调用service.update
                serviceMethodName = "update";
            } else {
                serviceMethodName = "updateSelective";
            }
            method.addBodyLine(modelParamName + " = this." + getService() + serviceMethodName + "(" + modelParamName + ");");
        } else {
            method.addBodyLine(modelParamName + " = this." + getService() + "update(" + modelParamName + ");");
        }
        method.addBodyLine("return super.success(" + modelParamName + ");");

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
        method.setReturnType(resultModelType);

        if (this.enableRestful) {
            method.addAnnotation(getRestfulRequestMappingAnnotation(introspectedTable, RequestMethod.DELETE));
        } else {
            method.addAnnotation("@RequestMapping(value = \"/delete\", method = RequestMethod.POST)");
        }

        // swagger
        if (enableSwaggerAnnotation) {
            method.addAnnotation("@ApiOperation(value = \"删除\", notes = \"\", response = "
                                 + resultModelType.getShortName() + ".class)");
        }

        // 添加方法参数(主键)
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        List<Parameter> keyParameters = addKeyParameters(method, primaryKeyColumns);

        // 填充key
        String keyParams = prepareCallByKey(introspectedTable, method, keyParameters);
        
        // 判断是否存在(调用Service get)
        String modelParamName = PluginUtils.getTypeParamName(allFieldModelType);
        method.addBodyLine(allFieldModelType.getShortName() + " " + modelParamName + " = this." + getService() + "get(" + keyParams + ");");
        method.addBodyLine("if(" + modelParamName + " == null) {");
        method.addBodyLine("return super.gone();");
        method.addBodyLine("}");
        
        // 校验所有者
        IntrospectedColumn ownerColumn = GenHelper.getOwnerColumn(introspectedTable);
        if(ownerColumn != null) {
            // checkOwner
            method.addBodyLine("");
            method.addBodyLine("// 校验所有者");
            method.addBodyLine("super.checkOwner(" + PluginUtils.generateGetterCall(modelParamName, ownerColumn.getJavaProperty(), null, false) + ");");
            method.addBodyLine("");
        }
        
        // 调用Service delete
        method.addBodyLine("this." + getService() + "delete(" + keyParams + ");");
        
//        if (GenHelper.hasLogicDeletedField(introspectedTable)) {
//            method.addBodyLine("this." + getService() + "deleteLogically(" + keyParams + ");");
//        } else {
//            method.addBodyLine("this." + getService() + "deletePhysically(" + keyParams + ");");
//        }
        
        method.addBodyLine("return super.success();");

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
        method.setReturnType(resultModelType);

        if (this.enableRestful) {
            method.addAnnotation(getRestfulRequestMappingAnnotation(introspectedTable, RequestMethod.GET));
        } else {
            method.addAnnotation("@RequestMapping(value = \"/get\", method = RequestMethod.GET)");
        }

        // swagger
        if (enableSwaggerAnnotation) {
            method.addAnnotation("@ApiOperation(value = \"详情\", notes = \"\", response = "
                                 + resultModelType.getShortName() + ".class)");
        }
        
        // 添加方法参数(主键)
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        List<Parameter> keyParameters = addKeyParameters(method, primaryKeyColumns);

        // 填充key
        String keyParams = prepareCallByKey(introspectedTable, method, keyParameters);

        // 调用Service
        String modelParamName = PluginUtils.getTypeParamName(allFieldModelType);
        method.addBodyLine(allFieldModelType.getShortName() + " " + modelParamName + " = this." + getService() + "get(" + keyParams + ");");
        method.addBodyLine("if(" + modelParamName + " == null) {");
        method.addBodyLine("return super.error(\"记录不存在\");");
        method.addBodyLine("}");
        method.addBodyLine("return super.success(" + modelParamName + ");");
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
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("list");
        method.setReturnType(resultModelType);
        if (this.enableRestful) {
            method.addAnnotation(getRestfulRequestMappingAnnotation(null, RequestMethod.GET));
        } else {
            method.addAnnotation("@RequestMapping(value = \"/list\", method = RequestMethod.GET)");
        }

        // swagger
        if (enableSwaggerAnnotation) {
            method.addAnnotation("@ApiOperation(value = \"列表\", notes = \"\", response = "
                                 + resultModelType.getShortName() + ".class)");
        }

        // 添加方法分页参数
        Parameter pageParameter = new Parameter(new FullyQualifiedJavaType("int"), "pageNum");
        pageParameter.addAnnotation("@RequestParam(required = true)");
        method.addParameter(pageParameter);
        Parameter limitParameter = new Parameter(new FullyQualifiedJavaType("int"), "pageSize");
        limitParameter.addAnnotation("@RequestParam(required = true)");
        method.addParameter(limitParameter);

        // 调用Service
        method.addBodyLine(pageType.getShortName() + "<" + allFieldModelType.getShortName() + "> page = " + getService()
                           + "list(pageNum, pageSize);");
        method.addBodyLine("return super.success(page);");
        return method;
    }

    /**
     * 添加 Mapper依赖字段
     */
    private void addServiceField(TopLevelClass topLevelClass) {
        topLevelClass.addImportedType(serviceType);

        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(serviceType);
        field.setName(PluginUtils.lowerCaseFirstLetter(serviceType.getShortName()));
        field.addAnnotation("@Resource");
        topLevelClass.addField(field);
    }

    private String getService() {
        return PluginUtils.lowerCaseFirstLetter(serviceType.getShortName()) + ".";
    }

    private String getRestfulRequestMappingAnnotation(IntrospectedTable introspectedTable,
                                                      RequestMethod method) {
        List<IntrospectedColumn> primaryKeyColumns = null;
        if(introspectedTable != null) {
            primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        }
        
        String methodStr = "";
        if (method == RequestMethod.POST) {
            methodStr = "RequestMethod.POST";
        } else if (method == RequestMethod.PUT) {
            methodStr = "RequestMethod.PUT";
        } else if (method == RequestMethod.PATCH) {
            methodStr = "RequestMethod.PATCH";
        } else if (method == RequestMethod.DELETE) {
            methodStr = "RequestMethod.DELETE";
        } else if (method == RequestMethod.GET) {
            methodStr = "RequestMethod.GET";
        }

        if (primaryKeyColumns == null) {
            return "@RequestMapping(method = " + methodStr + ")";
        } else {
        	// 复合ID的Path表现形式 --> "/id,id2"
            String pathStr = "";
            for (IntrospectedColumn primaryKeyColumn : primaryKeyColumns) {
                pathStr += "{" + primaryKeyColumn.getJavaProperty() + "}" + ",";
            }
            pathStr = "/" + pathStr.substring(0, pathStr.lastIndexOf(","));
            return "@RequestMapping(value = \"" + pathStr + "\", method = " + methodStr + ")";
        }
    }

    private List<Parameter> addKeyParameters(Method method, List<IntrospectedColumn> primaryKeyColumns) {
        List<Parameter> list = new ArrayList<>();
        for (IntrospectedColumn primaryKeyColumn : primaryKeyColumns) {
            String javaProperty = primaryKeyColumn.getJavaProperty();
            Parameter parameter = new Parameter(primaryKeyColumn.getFullyQualifiedJavaType(), javaProperty);
            if (enableRestful) {
                parameter.addAnnotation("@PathVariable(\"" + primaryKeyColumn.getJavaProperty() + "\")");
            } else {
                parameter.addAnnotation("@RequestParam(required = true)");
            }
            method.addParameter(parameter);
            list.add(parameter);
        }
        return list;
    }
    
    /**
     * 如果有生成主键类, 则填充主键类, 并返回调用参数
     * @param introspectedTable
     * @param caller
     * @return
     */
    private String prepareCallByKey(IntrospectedTable introspectedTable, Method caller, List<Parameter> keyParameters) {
        // 构造Service调用参数
        String params = "";
        String keyModelParamName = PluginUtils.PRIMARY_KEY_PARAMETER_NAME;
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            FullyQualifiedJavaType keyModeltype = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
            // 填充key参数
            caller.addBodyLine(keyModeltype.getShortName() + " " + keyModelParamName + " = new " + keyModeltype.getShortName() + "();");
            PluginUtils.generateModelSetterBodyLine(keyModelParamName, caller, keyParameters);
            // call param
            params = keyModelParamName;
        } else {
            List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
            params = PluginUtils.getCallParameters(keyParameterList);
        }
        
        return params;
    }

}
