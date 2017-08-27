package org.mybatis.generator.plugins;

import java.util.ArrayList;
import java.util.List;

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

/**
 * Service代码生成插件
 * 
 * @since 2.0.0
 * @author 叶鹏
 * @date 2017年8月25日
 */
public class MybatisServicePlugin extends PluginAdapter {

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

	private FullyQualifiedJavaType listType;
	private FullyQualifiedJavaType pagerType;
	private FullyQualifiedJavaType pagerUtilType;

	private FullyQualifiedJavaType annotationResource;
	private FullyQualifiedJavaType annotationService;

	private String servicePackage;
	private String serviceImplPackage;
	private String project;
	private String modelPackage;

	public MybatisServicePlugin() {
		super();
		slf4jLogger = new FullyQualifiedJavaType("org.slf4j.Logger");
		slf4jLoggerFactory = new FullyQualifiedJavaType("org.slf4j.LoggerFactory");
	}

	@Override
	public boolean validate(List<String> warnings) {
		servicePackage = properties.getProperty("targetPackage");
		serviceImplPackage = properties.getProperty("targetPackageImpl");
		project = properties.getProperty("targetProject");
		modelPackage = context.getJavaModelGeneratorConfiguration().getTargetPackage();

		listType = new FullyQualifiedJavaType("java.util.List");
		pagerType = new FullyQualifiedJavaType("com.xsili.mybatis.plugin.page.model.Pager");
		pagerUtilType = new FullyQualifiedJavaType("com.xsili.mybatis.plugin.page.util.PagerUtil");

		annotationResource = new FullyQualifiedJavaType("javax.annotation.Resource");
		annotationService = new FullyQualifiedJavaType("org.springframework.stereotype.Service");

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
		addImport(topLevelClass, serviceInterface);
		// 接口
		addService(topLevelClass, serviceInterface, introspectedTable, files);
		// 实现类
		addServiceImpl(topLevelClass, introspectedTable, files);
		// 日志
		addLoggerField(topLevelClass);

		return files;
	}

	/**
	 * 添加接口
	 * 
	 * @param files
	 */
	private void addService(TopLevelClass topLevelClass, Interface serviceInterface,
			IntrospectedTable introspectedTable, List<GeneratedJavaFile> files) {
		serviceInterface.setVisibility(JavaVisibility.PUBLIC);

		Method addMethod = addEntity(topLevelClass, introspectedTable);
		addMethod.removeBodyLines();
		serviceInterface.addMethod(addMethod);

		Method updateMethod = updateEntity(topLevelClass, introspectedTable);
		updateMethod.removeBodyLines();
		serviceInterface.addMethod(updateMethod);

		Method deleteMethod = deleteEntity(introspectedTable);
		deleteMethod.removeBodyLines();
		serviceInterface.addMethod(deleteMethod);

		Method getMethod = getEntity(introspectedTable);
		getMethod.removeBodyLines();
		serviceInterface.addMethod(getMethod);

		Method listMethod = listEntitys(topLevelClass, introspectedTable);
		listMethod.removeBodyLines();
		serviceInterface.addMethod(listMethod);

		GeneratedJavaFile file = new GeneratedJavaFile(serviceInterface, project,
				context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
		files.add(file);
	}

	/**
	 * 生成ServiceImpl
	 * 
	 * @param topLevelClass
	 * @param introspectedTable
	 * @param files
	 */
	private void addServiceImpl(TopLevelClass topLevelClass, IntrospectedTable introspectedTable,
			List<GeneratedJavaFile> files) {
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		// 设置实现的接口
		topLevelClass.addSuperInterface(serviceInterfaceType);
		// 添加注解
		topLevelClass
				.addAnnotation("@Service(\"" + PluginUtils.lowerCaseFirstLetter(serviceInterfaceType.getShortName()) + "\")");
		topLevelClass.addImportedType(annotationService);

		// 添加 Mapper引用
		addMapperField(topLevelClass);

		// 添加基础方法

		Method addMethod = addEntity(topLevelClass, introspectedTable);
		addMethod.addAnnotation("@Override");
		topLevelClass.addMethod(addMethod);

		Method updateMethod = updateEntity(topLevelClass, introspectedTable);
		updateMethod.addAnnotation("@Override");
		topLevelClass.addMethod(updateMethod);

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
		GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project,
				context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
		files.add(file);
	}

	/**
	 * 导入需要的类
	 */
	private void addImport(TopLevelClass topLevelClass, Interface serviceInterface) {
		// 接口
		serviceInterface.addImportedType(modelType);
		serviceInterface.addImportedType(modelWithBLOBsType);
		serviceInterface.addImportedType(pagerType);
		serviceInterface.addImportedType(pagerUtilType);

		// 实现类
		topLevelClass.addImportedType(slf4jLogger);
		topLevelClass.addImportedType(slf4jLoggerFactory);

		topLevelClass.addImportedType(serviceInterfaceType);
		topLevelClass.addImportedType(mapperType);
		topLevelClass.addImportedType(modelType);
		topLevelClass.addImportedType(modelWithBLOBsType);
		topLevelClass.addImportedType(modelCriteriaType);
		topLevelClass.addImportedType(modelSubCriteriaType);

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
	private Method addEntity(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		String modelParamName = PluginUtils.getTypeParamName(modelWithBLOBsType);

		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName("add");
		method.setReturnType(modelWithBLOBsType);
		method.addParameter(new Parameter(modelWithBLOBsType, modelParamName));

		List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
		List<IntrospectedColumn> introspectedColumnsList = introspectedTable.getAllColumns();
		for (IntrospectedColumn introspectedColumn : introspectedColumnsList) {
			boolean isPrimaryKey = false;
			for (IntrospectedColumn primaryKeyColumn : primaryKeyColumns) {
				if (introspectedColumn == primaryKeyColumn) {
					isPrimaryKey = true;
					break;
				}
			}

			// 非自增主键, 默认使用UUID
			if (isPrimaryKey) {
				if (!introspectedColumn.isIdentity() && !introspectedColumn.isAutoIncrement()) {
					method.addBodyLine(modelParamName + ".setId(IDGenerator.getUUID());");
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
		method.addBodyLine("throw new RuntimeException(\"插入数据库失败\");");
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
	private Method updateEntity(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		String modelParamName = PluginUtils.getTypeParamName(modelWithBLOBsType);

		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName("update");
		method.setReturnType(modelWithBLOBsType);
		method.addParameter(new Parameter(modelWithBLOBsType, modelParamName));

		List<IntrospectedColumn> introspectedColumnsList = introspectedTable.getAllColumns();
		for (IntrospectedColumn introspectedColumn : introspectedColumnsList) {
			// mysql date introspectedColumn.isJDBCDateColumn()
			// mysql time introspectedColumn.isJDBCTimeColumn()
			// mysql dateTime ??

			if ("updateTime".equals(introspectedColumn.getJavaProperty())) {
				topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
				method.addBodyLine(modelParamName + ".setUpdateTime(new Date());");
			}
		}
		method.addBodyLine("if(this." + getMapper() + "updateByPrimaryKeySelective(" + modelParamName + ") == 0) {");
		method.addBodyLine("throw new RuntimeException(\"记录不存在\");");
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

		List<Parameter> parameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
		for (Parameter parameter : parameterList) {
			method.addParameter(parameter);
		}
		String params = PluginUtils.getCallParameters(parameterList);

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

		List<Parameter> parameterList = PluginUtils.getPrimaryKeyParameters(introspectedTable);
		for (Parameter parameter : parameterList) {
			method.addParameter(parameter);
		}
		String params = PluginUtils.getCallParameters(parameterList);

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
		topLevelClass.addImportedType(pagerUtilType);
		Method method = new Method();
		method.setName("list");
		method.setReturnType(new FullyQualifiedJavaType("Pager<" + modelType.getShortName() + ">"));
		method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "page"));
		method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "limit"));
		method.setVisibility(JavaVisibility.PUBLIC);

		method.addBodyLine(
				modelCriteriaType.getShortName() + " criteria = new " + modelCriteriaType.getShortName() + "();");
		method.addBodyLine("criteria.setPage(page);");
		method.addBodyLine("criteria.setLimit(limit);");
		method.addBodyLine(modelSubCriteriaType.getShortName() + " cri = criteria.createCriteria();");

		method.addBodyLine(
				"List<" + modelType.getShortName() + "> list = " + getMapper() + "selectByExample(criteria);");
		method.addBodyLine("return PagerUtil.getPager(list, criteria);");
		return method;
	}

	/**
	 * count
	 * 
	 * @param introspectedTable
	 * @return
	 */
//	private Method countByExample() {
//		Method method = new Method();
//		method.setName("count");
//		method.setReturnType(FullyQualifiedJavaType.getIntInstance());
//		method.addParameter(new Parameter(modelCriteriaType, "criteria"));
//		method.setVisibility(JavaVisibility.PUBLIC);
//		StringBuilder sb = new StringBuilder();
//		sb.append("int count = this.").append(getMapper()).append("countByExample");
//		sb.append("(").append("criteria").append(");");
//		method.addBodyLine(sb.toString());
//		method.addBodyLine("return count;");
//		return method;
//	}

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
