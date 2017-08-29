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
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.plugins.PluginUtils;

/**
 * Spring MVC Controller 代码生成插件
 * 
 * @since 2.0.0
 * @author 叶鹏
 * @date 2017年8月26日
 */
public class SpringMvcControllerPlugin extends PluginAdapter {

	private FullyQualifiedJavaType abstractControllerType;
	private FullyQualifiedJavaType resultModelType;
	private FullyQualifiedJavaType validatorUtilType;
	private FullyQualifiedJavaType pagerType;

	private FullyQualifiedJavaType controllerType;
	private FullyQualifiedJavaType serviceType;
	private FullyQualifiedJavaType modelType;
	/**
	 * 如果有WithBlob, 则modelWithBLOBsType赋值为BlobModel, 否则赋值为BaseModel
	 */
	private FullyQualifiedJavaType modelWithBLOBsType;

	private String targetPackage;
	private String targetPackageService;
	private String project;
	private String modelPackage;

	private FullyQualifiedJavaType annotationResource;
	private FullyQualifiedJavaType annotationController;
	private FullyQualifiedJavaType annotationRequestMapping;
	private FullyQualifiedJavaType annotationRequestMethod;
	private FullyQualifiedJavaType annotationRequestParam;
	private FullyQualifiedJavaType annotationApiOperation;

	public SpringMvcControllerPlugin() {
		super();
	}

	@Override
	public boolean validate(List<String> warnings) {
		String resultModel = properties.getProperty("resultModel");
		if (StringUtils.isBlank(resultModel)) {
			throw new IllegalArgumentException("property resultModel is null");
		}
		resultModelType = new FullyQualifiedJavaType(resultModel);
		
		String pager = properties.getProperty("pager");
        if(StringUtils.isBlank(pager)) {
            throw new RuntimeException("property pager is null");
        } else {
            pagerType = new FullyQualifiedJavaType(pager);
        }

		String abstractController = properties.getProperty("abstractController");
		if (StringUtils.isBlank(abstractController)) {
		    throw new RuntimeException("property abstractController is null");
		} else {
		    abstractControllerType = new FullyQualifiedJavaType(abstractController);
		}
        
        String validatorUtil = properties.getProperty("validatorUtil");
        if (StringUtils.isNotBlank(validatorUtil)) {
            validatorUtilType = new FullyQualifiedJavaType(validatorUtil);
        }

		targetPackage = properties.getProperty("targetPackage");
		targetPackageService = properties.getProperty("targetPackageService");
		project = properties.getProperty("targetProject");
		modelPackage = context.getJavaModelGeneratorConfiguration().getTargetPackage();

		annotationResource = new FullyQualifiedJavaType("javax.annotation.Resource");
		annotationController = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RestController");
		annotationRequestMapping = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMapping");
		annotationRequestMethod = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMethod");
		annotationRequestParam = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestParam");
		annotationApiOperation = new FullyQualifiedJavaType("io.swagger.annotations.ApiOperation");

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
		serviceType = new FullyQualifiedJavaType(targetPackageService + "." + tableName + "Service");
		controllerType = new FullyQualifiedJavaType(targetPackage + "." + tableName + "Controller");

		TopLevelClass topLevelClass = new TopLevelClass(controllerType);
		// 导入必要的类
		addImport(topLevelClass, introspectedTable);
		// 实现类
		List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
		addController(topLevelClass, introspectedTable, files);

		return files;
	}

	/**
	 * 生成Controller
	 * 
	 * @param topLevelClass
	 * @param introspectedTable
	 * @param files
	 */
	private void addController(TopLevelClass topLevelClass, IntrospectedTable introspectedTable,
			List<GeneratedJavaFile> files) {
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		// 父类
		if (abstractControllerType != null) {
			topLevelClass.setSuperClass(abstractControllerType);
		}
		// 添加注解
		topLevelClass.addAnnotation("@RestController");
		topLevelClass.addAnnotation("@RequestMapping(\"/v1/" + PluginUtils.humpToEnDash(modelType.getShortName()) + "/\")");

		// 添加 Mapper引用
		addServiceField(topLevelClass);

		// 添加基础方法

		Method addMethod = addEntity(topLevelClass, introspectedTable);
		topLevelClass.addMethod(addMethod);

		Method updateMethod = updateEntity(topLevelClass, introspectedTable);
		topLevelClass.addMethod(updateMethod);

		Method deleteMethod = deleteEntity(introspectedTable);
		topLevelClass.addMethod(deleteMethod);

		Method getMethod = getEntity(introspectedTable);
		topLevelClass.addMethod(getMethod);

		Method listMethod = listEntitys(topLevelClass, introspectedTable);
		topLevelClass.addMethod(listMethod);

		// 生成文件
		GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project,
				context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
		files.add(file);
	}

	/**
	 * 导入需要的类
	 */
	private void addImport(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
	    // 导入key类型
        List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
        for (Parameter parameter : keyParameterList) {
            if(parameter.getName().equals(PluginUtils.PRIMARY_KEY_PARAMETER_NAME)) {
                topLevelClass.addImportedType(parameter.getType());
                topLevelClass.addImportedType(parameter.getType());
            }
        }
        
	    topLevelClass.addImportedType(pagerType);
		if (abstractControllerType != null) {
			topLevelClass.addImportedType(abstractControllerType);
		}
		if(validatorUtilType != null) {
		    topLevelClass.addImportedType(validatorUtilType);
		}

		topLevelClass.addImportedType(resultModelType);
		topLevelClass.addImportedType(serviceType);
		topLevelClass.addImportedType(modelType);
		topLevelClass.addImportedType(modelWithBLOBsType);

		topLevelClass.addImportedType(annotationResource);
		topLevelClass.addImportedType(annotationController);
		topLevelClass.addImportedType(annotationRequestMapping);
		topLevelClass.addImportedType(annotationRequestMethod);
		topLevelClass.addImportedType(annotationRequestParam);
		topLevelClass.addImportedType(annotationApiOperation);
	}

	/**
	 * add
	 * 
	 * @param topLevelClass
	 * @param introspectedTable
	 * @return
	 */
	private Method addEntity(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		String modelParamName = PluginUtils.getTypeParamName(modelWithBLOBsType);

		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName("add");
		method.setReturnType(resultModelType);
		method.addAnnotation("@RequestMapping(value = \"/add\", method = RequestMethod.POST)");
		method.addAnnotation(
				"@ApiOperation(value = \"新增\", notes = \"\", response = " + resultModelType.getShortName() + ".class)");

		List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
		List<IntrospectedColumn> introspectedColumnsList = introspectedTable.getAllColumns();
		for (IntrospectedColumn introspectedColumn : introspectedColumnsList) {
			boolean isPrimaryKey = primaryKeyColumns.contains(introspectedColumn);
			String javaProperty = introspectedColumn.getJavaProperty();
			// 排除主键, 创建时间, 更新时间
			if (!isPrimaryKey && !"createTime".equals(javaProperty) && !"updateTime".equals(javaProperty)) {
				Parameter parameter = new Parameter(introspectedColumn.getFullyQualifiedJavaType(), javaProperty);
				parameter.addAnnotation("@RequestParam(required = true)");
				method.addParameter(parameter);
			}
		}

		method.addBodyLine("// 填充参数");
		StringBuilder sb = new StringBuilder();
		sb.append(modelWithBLOBsType.getShortName() + " " + modelParamName);
		sb.append(" = ");
		sb.append("new ").append(modelWithBLOBsType.getShortName()).append("();");
		method.addBodyLine(sb.toString());
		PluginUtils.addSetFieldBodyLine(modelParamName, method);
		
		if(validatorUtilType != null) {
		    method.addBodyLine("// 校验参数");
		    method.addBodyLine("ValidatorUtil.checkParams(" + modelParamName + ");");
		}

		method.addBodyLine("");
		method.addBodyLine(modelParamName + " = this." + getService() + "add(" + modelParamName + ");");
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
	private Method updateEntity(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		String modelParamName = PluginUtils.getTypeParamName(modelWithBLOBsType);

		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName("update");
		method.setReturnType(resultModelType);
		method.addAnnotation("@RequestMapping(value = \"/update\", method = RequestMethod.POST)");
		method.addAnnotation(
				"@ApiOperation(value = \"更新\", notes = \"\", response = " + resultModelType.getShortName() + ".class)");

		List<IntrospectedColumn> introspectedColumnsList = introspectedTable.getAllColumns();
		for (IntrospectedColumn introspectedColumn : introspectedColumnsList) {
			String javaProperty = introspectedColumn.getJavaProperty();
			if (!"createTime".equals(javaProperty) && !"updateTime".equals(javaProperty)) {
				Parameter parameter = new Parameter(introspectedColumn.getFullyQualifiedJavaType(), javaProperty);
				parameter.addAnnotation("@RequestParam(required = true)");
				method.addParameter(parameter);
			}
		}

		method.addBodyLine("// 填充参数");
		StringBuilder sb = new StringBuilder();
		sb.append(modelWithBLOBsType.getShortName() + " " + modelParamName);
		sb.append(" = ");
		sb.append("new ").append(modelWithBLOBsType.getShortName()).append("();");
		method.addBodyLine(sb.toString());
		PluginUtils.addSetFieldBodyLine(modelParamName, method);

		if(validatorUtilType != null) {
		    method.addBodyLine("// 校验参数");
		    method.addBodyLine("ValidatorUtil.checkParams(" + modelParamName + ");");
		}

		method.addBodyLine("");
		method.addBodyLine(modelParamName + " = this." + getService() + "update(" + modelParamName + ");");
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
		method.addAnnotation("@RequestMapping(value = \"/delete\", method = RequestMethod.POST)");
		method.addAnnotation(
				"@ApiOperation(value = \"删除\", notes = \"\", response = " + resultModelType.getShortName() + ".class)");

		List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
		for (Parameter parameter : keyParameterList) {
			// parameter.addAnnotation("@RequestParam(required = true)");
			method.addParameter(parameter);
		}
		String params = PluginUtils.getCallParameters(keyParameterList);

		method.addBodyLine("boolean successful = this." + getService() + "delete(" + params + ");");
		method.addBodyLine("if (!successful) {");
		method.addBodyLine("return super.error(\"记录不存在或已被删除\");");
		method.addBodyLine("} else {");
		method.addBodyLine("return super.success();");
		method.addBodyLine("}");
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
		method.addAnnotation("@RequestMapping(value = \"/get\", method = RequestMethod.GET)");
		method.addAnnotation(
				"@ApiOperation(value = \"详情\", notes = \"\", response = " + resultModelType.getShortName() + ".class)");

		List<Parameter> keyParameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
		for (Parameter parameter : keyParameterList) {
			// parameter.addAnnotation("@RequestParam(required = true)");
			method.addParameter(parameter);
		}
		String params = PluginUtils.getCallParameters(keyParameterList);

		String modelParamName = PluginUtils.getTypeParamName(modelWithBLOBsType);

		StringBuilder sb = new StringBuilder();
		sb.append(modelWithBLOBsType.getShortName() + " " + modelParamName);
		sb.append(" = ");
		sb.append("this.").append(getService()).append("get").append("(").append(params).append(");");
		method.addBodyLine(sb.toString());
		method.addBodyLine("if(" + modelParamName + " == null){");
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
		method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "page"));
		method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "limit"));
		method.addAnnotation("@RequestMapping(value = \"/list\", method = RequestMethod.GET)");
		method.addAnnotation(
				"@ApiOperation(value = \"列表\", notes = \"\", response = " + resultModelType.getShortName() + ".class)");

		method.addBodyLine(pagerType.getShortName() + "<" + modelType.getShortName() + "> pager = " + getService() + "list(page, limit);");
		method.addBodyLine("return super.success(pager);");
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

}
