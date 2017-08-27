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

	private FullyQualifiedJavaType interfaceType;
	private FullyQualifiedJavaType serviceType;
	private FullyQualifiedJavaType mapperType;
	private FullyQualifiedJavaType pojoType;
	private FullyQualifiedJavaType pojoCriteriaType;
	private FullyQualifiedJavaType pojoSubCriteriaType;

	private FullyQualifiedJavaType listType;
	private FullyQualifiedJavaType pagerType;

	private FullyQualifiedJavaType annotationResource;
	private FullyQualifiedJavaType annotationService;

	private String servicePackage;
	private String serviceImplPackage;
	private String project;
	private String pojoPackage;

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
		pojoPackage = context.getJavaModelGeneratorConfiguration().getTargetPackage();

		annotationResource = new FullyQualifiedJavaType("javax.annotation.Resource");
		annotationService = new FullyQualifiedJavaType("org.springframework.stereotype.Service");

		return true;
	}

	@Override
	public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
		List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
		String table = introspectedTable.getBaseRecordType();
		String tableName = table.replaceAll(this.pojoPackage + ".", "");

		mapperType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
		interfaceType = new FullyQualifiedJavaType(servicePackage + "." + tableName + "Service");
		serviceType = new FullyQualifiedJavaType(serviceImplPackage + "." + tableName + "ServiceImpl");

		if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
			pojoType = new FullyQualifiedJavaType(introspectedTable.getRecordWithBLOBsType());
		} else {
			pojoType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		}

		pojoCriteriaType = new FullyQualifiedJavaType(pojoPackage + "." + tableName + "Criteria");
		pojoSubCriteriaType = new FullyQualifiedJavaType(pojoPackage + "." + tableName + "Criteria.Criteria");
		listType = new FullyQualifiedJavaType("java.util.List");
		pagerType = new FullyQualifiedJavaType("com.fpx.mybatis.plugin.model.Pager");
		Interface serviceInterface = new Interface(interfaceType);
		TopLevelClass topLevelClass = new TopLevelClass(serviceType);

		// 导入必要的类
		addImport(topLevelClass, serviceInterface);

		// 接口
		addService(topLevelClass, serviceInterface, introspectedTable, tableName, files);

		// 实现类
		addServiceImpl(topLevelClass, introspectedTable, tableName, files);

		// 日志
		addLogger(topLevelClass);

		return files;
	}

	/**
	 * 添加接口
	 * 
	 * @param tableName
	 * @param files
	 */
	private void addService(TopLevelClass topLevelClass, Interface serviceInterface,
			IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
		serviceInterface.setVisibility(JavaVisibility.PUBLIC);

		Method addMethod = addEntity(topLevelClass, introspectedTable, tableName);
		addMethod.removeBodyLines();
		serviceInterface.addMethod(addMethod);

		Method updateMethod = updateEntity(topLevelClass, introspectedTable, tableName);
		updateMethod.removeBodyLines();
		serviceInterface.addMethod(updateMethod);

		Method deleteMethod = deleteEntity(introspectedTable, tableName);
		deleteMethod.removeBodyLines();
		serviceInterface.addMethod(deleteMethod);

		Method getMethod = getEntity(introspectedTable, tableName);
		getMethod.removeBodyLines();
		serviceInterface.addMethod(getMethod);

		Method listMethod = listEntitys(topLevelClass, introspectedTable, tableName);
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
	 * @param tableName
	 * @param files
	 */
	private void addServiceImpl(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String tableName,
			List<GeneratedJavaFile> files) {
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		// 设置实现的接口
		topLevelClass.addSuperInterface(interfaceType);
		// 添加注解
		topLevelClass.addAnnotation(
				"@Service(\"" + tableName.substring(0, 1).toLowerCase() + tableName.substring(1) + "Service\")");
		topLevelClass.addImportedType(annotationService);

		// 添加 Mapper引用
		addMapperField(topLevelClass, tableName);

		// 添加基础方法

		Method addMethod = addEntity(topLevelClass, introspectedTable, tableName);
		addMethod.addAnnotation("@Override");
		topLevelClass.addMethod(addMethod);

		Method updateMethod = updateEntity(topLevelClass, introspectedTable, tableName);
		updateMethod.addAnnotation("@Override");
		topLevelClass.addMethod(updateMethod);

		Method deleteMethod = deleteEntity(introspectedTable, tableName);
		deleteMethod.addAnnotation("@Override");
		topLevelClass.addMethod(deleteMethod);

		Method getMethod = getEntity(introspectedTable, tableName);
		getMethod.addAnnotation("@Override");
		topLevelClass.addMethod(getMethod);

		Method listMethod = listEntitys(topLevelClass, introspectedTable, tableName);
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
		serviceInterface.addImportedType(pojoType);
		serviceInterface.addImportedType(pagerType);

		topLevelClass.addImportedType(slf4jLogger);
		topLevelClass.addImportedType(slf4jLoggerFactory);

		topLevelClass.addImportedType(interfaceType);
		topLevelClass.addImportedType(mapperType);
		topLevelClass.addImportedType(pojoType);
		topLevelClass.addImportedType(pojoCriteriaType);
		topLevelClass.addImportedType(pojoSubCriteriaType);

		topLevelClass.addImportedType(listType);
		topLevelClass.addImportedType(pagerType);

		topLevelClass.addImportedType(annotationService);
		topLevelClass.addImportedType(annotationResource);
	}

	/**
	 * 导入logger
	 */
	private void addLogger(TopLevelClass topLevelClass) {
		Field field = new Field();
		field.setFinal(true);
		field.setInitializationString("LoggerFactory.getLogger(" + topLevelClass.getType().getShortName() + ".class)"); // 设置值
		field.setName("logger"); // 设置变量名
		field.setStatic(true);
		field.setType(new FullyQualifiedJavaType("Logger")); // 类型
		field.setVisibility(JavaVisibility.PRIVATE);
		topLevelClass.addField(field);
	}

	/**
	 * add
	 * 
	 * @param topLevelClass
	 * @param introspectedTable
	 * @param tableName
	 * @return
	 */
	private Method addEntity(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String tableName) {
		Method method = new Method();
		method.setName("add");
		method.setReturnType(pojoType);
		method.addParameter(new Parameter(pojoType, "record"));
		method.setVisibility(JavaVisibility.PUBLIC);
		List<IntrospectedColumn> introspectedColumnsList = introspectedTable.getAllColumns();
		for (IntrospectedColumn introspectedColumn : introspectedColumnsList) {
			// 非自增主键, 默认使用UUID
			if (introspectedColumn.isIdentity() && !introspectedColumn.isAutoIncrement()) {
				method.addBodyLine("record.setId(IDGenerator.getUUID());");
			}

			if ("createTime".equals(introspectedColumn.getJavaProperty())) {
				topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
				method.addBodyLine("record.setCreateTime(new Date());");
			}
			if ("updateTime".equals(introspectedColumn.getJavaProperty())) {
				topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
				method.addBodyLine("record.setUpdateTime(new Date());");
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("if(this.").append(getMapper()).append("insertSelective(record) == 1) {");
		method.addBodyLine(sb.toString());
		method.addBodyLine("return record; ");
		method.addBodyLine("}");
		method.addBodyLine("return null;");
		return method;
	}

	/**
	 * update
	 * 
	 * @param topLevelClass
	 * @param introspectedTable
	 * @param tableName
	 * @return
	 */
	private Method updateEntity(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String tableName) {
		Method method = new Method();
		method.setName("update");
		method.setReturnType(pojoType);
		method.addParameter(new Parameter(pojoType, "record"));
		method.setVisibility(JavaVisibility.PUBLIC);

		List<IntrospectedColumn> introspectedColumnsList = introspectedTable.getAllColumns();
		for (IntrospectedColumn introspectedColumn : introspectedColumnsList) {
			// mysql date introspectedColumn.isJDBCDateColumn()
			// mysql time introspectedColumn.isJDBCTimeColumn()
			// mysql dateTime ??

			if ("updateTime".equals(introspectedColumn.getJavaProperty())) {
				topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
				method.addBodyLine("record.setUpdateTime(new Date());");
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("if(this.").append(getMapper()).append("updateByPrimaryKeySelective(record) == 1) {");
		method.addBodyLine(sb.toString());
		method.addBodyLine("return record;");
		method.addBodyLine("}");
		method.addBodyLine("return null;");

		return method;
	}

	/**
	 * delete
	 * 
	 * @param introspectedTable
	 * @param tableName
	 * @return
	 */
	private Method deleteEntity(IntrospectedTable introspectedTable, String tableName) {
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName("delete");
		method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());

		List<Parameter> parameterList = getPrimaryKeyParameter(introspectedTable);
		for (Parameter parameter : parameterList) {
			method.addParameter(parameter);
		}

		StringBuffer paramsBuf = new StringBuffer();
		for (Parameter parameter : parameterList) {
			paramsBuf.append(parameter.getName());
			paramsBuf.append(",");
		}
		paramsBuf.setLength(paramsBuf.length() - 1);
		String params = paramsBuf.toString();

		StringBuilder sb = new StringBuilder();
		sb.append("return this.").append(getMapper()).append("deleteByPrimaryKey(").append(params).append(") == 1;");
		method.addBodyLine(sb.toString());
		return method;
	}

	/**
	 * get
	 * 
	 * @param introspectedTable
	 * @param tableName
	 * @return
	 */
	private Method getEntity(IntrospectedTable introspectedTable, String tableName) {
		Method method = new Method();
		method.setName("get");
		method.setReturnType(pojoType);
		if (introspectedTable.getRules().generatePrimaryKeyClass()) {
			FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
			method.addParameter(new Parameter(type, "key"));
		} else {
			for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
				FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
				method.addParameter(new Parameter(type, introspectedColumn.getJavaProperty()));
			}
		}
		method.setVisibility(JavaVisibility.PUBLIC);
		StringBuilder sb = new StringBuilder();
		sb.append("return this.");
		sb.append(getMapper());
		sb.append("selectByPrimaryKey");
		sb.append("(");
		for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
			sb.append(introspectedColumn.getJavaProperty());
			sb.append(",");
		}
		sb.setLength(sb.length() - 1);
		sb.append(");");
		method.addBodyLine(sb.toString());
		return method;
	}

	/**
	 * list
	 * 
	 * @param topLevelClass
	 * @param introspectedTable
	 * @param tableName
	 * @return
	 */
	private Method listEntitys(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String tableName) {
		topLevelClass.addImportedType(new FullyQualifiedJavaType("com.fpx.mybatis.plugin.util.PagerUtil"));
		Method method = new Method();
		method.setName("list");
		method.setReturnType(new FullyQualifiedJavaType("Pager<" + tableName + ">"));
		method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "page"));
		method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "limit"));
		method.setVisibility(JavaVisibility.PUBLIC);

		method.addBodyLine(
				pojoCriteriaType.getShortName() + " criteria = new " + pojoCriteriaType.getShortName() + "();");
		method.addBodyLine("criteria.setPage(page);");
		method.addBodyLine("criteria.setLimit(limit);");
		method.addBodyLine(pojoSubCriteriaType.getShortName() + " cri = criteria.createCriteria();");

		method.addBodyLine("List<" + tableName + "> list = " + getMapper() + "selectByConditionList(criteria);");
		method.addBodyLine("return PagerUtil.getPager(list, criteria);");
		return method;
	}

	/**
	 * count
	 * 
	 * @param introspectedTable
	 * @param tableName
	 * @return
	 */
	private Method countByExample(IntrospectedTable introspectedTable, String tableName) {
		Method method = new Method();
		method.setName("countByCriteria");
		method.setReturnType(FullyQualifiedJavaType.getIntInstance());
		method.addParameter(new Parameter(pojoCriteriaType, "condition"));
		method.setVisibility(JavaVisibility.PUBLIC);
		StringBuilder sb = new StringBuilder();
		sb.append("int count = this.");
		sb.append(getMapper());
		sb.append("countByCriteria");
		sb.append("(");
		sb.append("condition");
		sb.append(");");
		method.addBodyLine(sb.toString());
		method.addBodyLine("logger.debug(\"count: {}\", count);");
		method.addBodyLine("return count;");
		return method;
	}

	/**
	 * 添加 Mapper依赖字段
	 */
	private void addMapperField(TopLevelClass topLevelClass, String tableName) {
		topLevelClass.addImportedType(mapperType);

		Field field = new Field();
		field.setType(mapperType);
		field.setName(lowerCaseFirstLetter(mapperType.getShortName()));
		field.setVisibility(JavaVisibility.PRIVATE);
		field.addAnnotation("@Resource");
		topLevelClass.addField(field);
	}

	/**
	 * 获取主键参数
	 * 
	 * @param introspectedTable
	 * @return
	 */
	private List<Parameter> getPrimaryKeyParameter(IntrospectedTable introspectedTable) {
		List<Parameter> list = new ArrayList<>();
		if (introspectedTable.getRules().generatePrimaryKeyClass()) {
			FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
			list.add(new Parameter(type, "key"));
		} else {
			for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
				FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
				list.add(new Parameter(type, introspectedColumn.getJavaProperty()));
			}
		}
		return list;
	}

	private String lowerCaseFirstLetter(String str) {
		StringBuilder sb = new StringBuilder(str);
		sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
		return sb.toString();
	}

	private String getMapper() {
		return lowerCaseFirstLetter(mapperType.getShortName()) + ".";
	}

}
