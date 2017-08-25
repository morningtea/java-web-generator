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
import org.mybatis.generator.internal.util.StringUtility;

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
    private FullyQualifiedJavaType serviceType;
    private FullyQualifiedJavaType daoType;
    private FullyQualifiedJavaType interfaceType;
    private FullyQualifiedJavaType pojoType;
    private FullyQualifiedJavaType pojoCriteriaType;
    private FullyQualifiedJavaType pojoSubCriteriaType;
    private FullyQualifiedJavaType listType;
    private FullyQualifiedJavaType autowired;
    private FullyQualifiedJavaType service;
    private FullyQualifiedJavaType returnType;
    private FullyQualifiedJavaType pagerType;
    private String servicePack;
    private String serviceImplPack;
    private String project;
    private String pojoUrl;

    /**
     * 是否添加注解
     */
    private boolean enableAnnotation = true;
    private boolean enableInsert = false;
    private boolean enableInsertSelective = false;
    private boolean enableDeleteByPrimaryKey = false;
    private boolean enableDeleteByExample = false;
    private boolean enableUpdateByExample = false;
    private boolean enableUpdateByExampleSelective = false;
    private boolean enableUpdateByPrimaryKey = false;
    private boolean enableUpdateByPrimaryKeySelective = false;

    public MybatisServicePlugin() {
        super();
        // 默认是slf4j
        slf4jLogger = new FullyQualifiedJavaType("org.slf4j.Logger");
        slf4jLoggerFactory = new FullyQualifiedJavaType("org.slf4j.LoggerFactory");
    }

    @Override
    public boolean validate(List<String> warnings) {
        if (StringUtility.stringHasValue(properties.getProperty("enableAnnotation")))
            enableAnnotation = StringUtility.isTrue(properties.getProperty("enableAnnotation"));

        String enableInsert = properties.getProperty("enableInsert");
        String enableUpdateByExampleSelective = properties.getProperty("enableUpdateByExampleSelective");
        String enableInsertSelective = properties.getProperty("enableInsertSelective");
        String enableUpdateByPrimaryKey = properties.getProperty("enableUpdateByPrimaryKey");
        String enableDeleteByPrimaryKey = properties.getProperty("enableDeleteByPrimaryKey");
        String enableDeleteByExample = properties.getProperty("enableDeleteByExample");
        String enableUpdateByPrimaryKeySelective = properties.getProperty("enableUpdateByPrimaryKeySelective");
        String enableUpdateByExample = properties.getProperty("enableUpdateByExample");

        if (StringUtility.stringHasValue(enableInsert))
            this.enableInsert = StringUtility.isTrue(enableInsert);
        if (StringUtility.stringHasValue(enableUpdateByExampleSelective))
            this.enableUpdateByExampleSelective = StringUtility.isTrue(enableUpdateByExampleSelective);
        if (StringUtility.stringHasValue(enableInsertSelective))
            this.enableInsertSelective = StringUtility.isTrue(enableInsertSelective);
        if (StringUtility.stringHasValue(enableUpdateByPrimaryKey))
            this.enableUpdateByPrimaryKey = StringUtility.isTrue(enableUpdateByPrimaryKey);
        if (StringUtility.stringHasValue(enableDeleteByPrimaryKey))
            this.enableDeleteByPrimaryKey = StringUtility.isTrue(enableDeleteByPrimaryKey);
        if (StringUtility.stringHasValue(enableDeleteByExample))
            this.enableDeleteByExample = StringUtility.isTrue(enableDeleteByExample);
        if (StringUtility.stringHasValue(enableUpdateByPrimaryKeySelective))
            this.enableUpdateByPrimaryKeySelective = StringUtility.isTrue(enableUpdateByPrimaryKeySelective);
        if (StringUtility.stringHasValue(enableUpdateByExample))
            this.enableUpdateByExample = StringUtility.isTrue(enableUpdateByExample);

        servicePack = properties.getProperty("targetPackage");
        serviceImplPack = properties.getProperty("implementationPackage");
        project = properties.getProperty("targetProject");
        pojoUrl = context.getJavaModelGeneratorConfiguration().getTargetPackage();

        if (enableAnnotation) {
            autowired = new FullyQualifiedJavaType("javax.annotation.Resource");
            service = new FullyQualifiedJavaType("org.springframework.stereotype.Service");
        }

        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
        String table = introspectedTable.getBaseRecordType();
        String tableName = table.replaceAll(this.pojoUrl + ".", "");
        interfaceType = new FullyQualifiedJavaType(servicePack + "." + tableName + "Service");

        // mybatis
        daoType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());

        // logger.info(toLowerCase(daoType.getShortName()));
        serviceType = new FullyQualifiedJavaType(serviceImplPack + "." + tableName + "ServiceImpl");

        // pojoType = new FullyQualifiedJavaType(pojoUrl + "." + tableName);
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            pojoType = new FullyQualifiedJavaType(introspectedTable.getRecordWithBLOBsType());
        } else {
            pojoType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        }

        pojoCriteriaType = new FullyQualifiedJavaType(pojoUrl + "." + tableName + "Criteria");
        pojoSubCriteriaType = new FullyQualifiedJavaType(pojoUrl + "." + tableName + "Criteria.Criteria");
        listType = new FullyQualifiedJavaType("java.util.List");
        pagerType = new FullyQualifiedJavaType("com.fpx.mybatis.plugin.model.Pager");
        Interface interface1 = new Interface(interfaceType);
        TopLevelClass topLevelClass = new TopLevelClass(serviceType);

        // 导入必要的类
        addImport(interface1, topLevelClass);

        // 接口
        addService(topLevelClass, interface1, introspectedTable, tableName, files);

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
    private void addService(TopLevelClass topLevelClass,
                            Interface interface1,
                            IntrospectedTable introspectedTable,
                            String tableName,
                            List<GeneratedJavaFile> files) {

        interface1.setVisibility(JavaVisibility.PUBLIC);

        // 添加缺省方法
        Method method = addEntity(topLevelClass, introspectedTable, tableName);
        method.removeBodyLines();
        interface1.addMethod(method);

        method = deleteEntity(introspectedTable, tableName);
        method.removeBodyLines();
        interface1.addMethod(method);

        method = updateEntity(topLevelClass, introspectedTable, tableName);
        method.removeBodyLines();
        interface1.addMethod(method);

        method = getEntity(introspectedTable, tableName);
        method.removeBodyLines();
        interface1.addMethod(method);

        method = listEntitys(topLevelClass, introspectedTable, tableName);
        method.removeBodyLines();
        interface1.addMethod(method);

        if (enableDeleteByPrimaryKey) {
            method = getOtherInteger("removeByPrimaryKey", "deleteByPrimaryKey", introspectedTable, tableName, 2);
            method.removeBodyLines();
            interface1.addMethod(method);
        }
        if (enableUpdateByPrimaryKeySelective) {
            method = getOtherInteger("saveByPrimaryKeySelective", "updateByPrimaryKeySelective", introspectedTable, tableName, 1);
            method.removeBodyLines();
            interface1.addMethod(method);
        }
        if (enableUpdateByPrimaryKey) {
            method = getOtherInteger("saveByPrimaryKey", "updateByPrimaryKey", introspectedTable, tableName, 1);
            method.removeBodyLines();
            interface1.addMethod(method);
        }
        if (enableDeleteByExample) {
            method = getOtherInteger("removeByCriteria", "deleteByCriteria", introspectedTable, tableName, 3);
            method.removeBodyLines();
            interface1.addMethod(method);
        }
        if (enableUpdateByExampleSelective) {
            method = getOtherInteger("saveByCriteriaSelective", "updateByCriteriaSelective", introspectedTable, tableName, 4);
            method.removeBodyLines();
            interface1.addMethod(method);
        }
        if (enableUpdateByExample) {
            method = getOtherInteger("saveByCriteria", "updateByCriteria", introspectedTable, tableName, 4);
            method.removeBodyLines();
            interface1.addMethod(method);
        }
        if (enableInsert) {
            method = getOtherInsertboolean("create", "insert", introspectedTable, tableName);
            method.removeBodyLines();
            interface1.addMethod(method);
        }
        if (enableInsertSelective) {
            method = getOtherInsertboolean("createSelective", "insertSelective", introspectedTable, tableName);
            method.removeBodyLines();
            interface1.addMethod(method);
        }

        GeneratedJavaFile file = new GeneratedJavaFile(interface1, project, context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
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
    private void addServiceImpl(TopLevelClass topLevelClass,
                                IntrospectedTable introspectedTable,
                                String tableName,
                                List<GeneratedJavaFile> files) {
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // 设置实现的接口
        topLevelClass.addSuperInterface(interfaceType);
        // 添加注解
        if (enableAnnotation) {
            topLevelClass.addAnnotation("@Service(\"" + tableName.substring(0, 1).toLowerCase() + tableName.substring(1)
                                        + "Service\")");
            topLevelClass.addImportedType(service);
        }
        // 添加引用dao
        addField(topLevelClass, tableName);
        // 添加基础方法
        topLevelClass.addMethod(addEntity(topLevelClass, introspectedTable, tableName));
        topLevelClass.addMethod(deleteEntity(introspectedTable, tableName));
        topLevelClass.addMethod(updateEntity(topLevelClass, introspectedTable, tableName));
        topLevelClass.addMethod(getEntity(introspectedTable, tableName));
        topLevelClass.addMethod(listEntitys(topLevelClass, introspectedTable, tableName));

        /**
         * type 的意义 pojo 1 ;key 2 ;example 3 ;pojo+example 4
         */
        if (enableDeleteByPrimaryKey) {
            topLevelClass.addMethod(getOtherInteger("removeByPrimaryKey", "deleteByPrimaryKey", introspectedTable, tableName, 2));
        }
        if (enableUpdateByPrimaryKeySelective) {
            topLevelClass.addMethod(getOtherInteger("saveByPrimaryKeySelective", "updateByPrimaryKeySelective", introspectedTable, tableName, 1));
        }
        if (enableUpdateByPrimaryKey) {
            topLevelClass.addMethod(getOtherInteger("saveByPrimaryKey", "updateByPrimaryKey", introspectedTable, tableName, 1));
        }
        if (enableDeleteByExample) {
            topLevelClass.addMethod(getOtherInteger("removeByCriteria", "deleteByCriteria", introspectedTable, tableName, 3));
        }
        if (enableUpdateByExampleSelective) {
            topLevelClass.addMethod(getOtherInteger("saveByCriteriaSelective", "updateByCriteriaSelective", introspectedTable, tableName, 4));
        }
        if (enableUpdateByExample) {
            topLevelClass.addMethod(getOtherInteger("saveByCriteria", "updateByCriteria", introspectedTable, tableName, 4));
        }
        if (enableInsert) {
            topLevelClass.addMethod(getOtherInsertboolean("create", "insert", introspectedTable, tableName));
        }
        if (enableInsertSelective) {
            topLevelClass.addMethod(getOtherInsertboolean("createSelective", "insertSelective", introspectedTable, tableName));
        }

        // 生成文件
        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project, context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
        files.add(file);
    }

    /**
     * 导入需要的类
     */
    private void addImport(Interface interfaces, TopLevelClass topLevelClass) {
        interfaces.addImportedType(pojoType);
        interfaces.addImportedType(pagerType);

        topLevelClass.addImportedType(daoType);
        topLevelClass.addImportedType(interfaceType);
        topLevelClass.addImportedType(pojoType);
        topLevelClass.addImportedType(pojoCriteriaType);
        topLevelClass.addImportedType(pojoSubCriteriaType);
        topLevelClass.addImportedType(listType);
        topLevelClass.addImportedType(slf4jLogger);
        topLevelClass.addImportedType(slf4jLoggerFactory);
        topLevelClass.addImportedType(pagerType);
        if (enableAnnotation) {
            topLevelClass.addImportedType(service);
            topLevelClass.addImportedType(autowired);
        }
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

        method.addBodyLine(pojoCriteriaType.getShortName() + " criteria = new " + pojoCriteriaType.getShortName()
                           + "();");
        method.addBodyLine("criteria.setPage(page);");
        method.addBodyLine("criteria.setLimit(limit);");
        method.addBodyLine(pojoSubCriteriaType.getShortName() + " cri = criteria.createCriteria();");

        method.addBodyLine("List<" + tableName + "> list = " + getDaoShort() + "selectByConditionList(criteria);");
        method.addBodyLine("return PagerUtil.getPager(list, criteria);");
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
        sb.append(getDaoShort());
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
        sb.append("if(this.");
        sb.append(getDaoShort());
        sb.append("updateByPrimaryKeySelective");
        sb.append("(record)==1)");
        
        method.addBodyLine(sb.toString());
        method.addBodyLine("\treturn record;");
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
        method.setName("delete");
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        String params = addParams(introspectedTable, method, 2);
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("return this.");
        sb.append(getDaoShort());
        sb.append("deleteByPrimaryKey");
        sb.append("(");
        sb.append(params);
        sb.append(")==1;");
        method.addBodyLine(sb.toString());
        return method;
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
        sb.append("if(this.");
        sb.append(getDaoShort());
        sb.append("insertSelective");
        sb.append("(");
        sb.append("record");
        sb.append(")==1)");
        method.addBodyLine(sb.toString());
        method.addBodyLine("\treturn record; ");
        method.addBodyLine("return null;");
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
        sb.append(getDaoShort());
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
     * 添加字段
     * 
     * @param topLevelClass
     */
    private void addField(TopLevelClass topLevelClass, String tableName) {
        // 添加 dao
        Field field = new Field();
        field.setName(toLowerCase(daoType.getShortName())); // 设置变量名
        topLevelClass.addImportedType(daoType);
        field.setType(daoType); // 类型
        field.setVisibility(JavaVisibility.PRIVATE);
        if (enableAnnotation) {
            field.addAnnotation("@Resource");
        }
        topLevelClass.addField(field);
    }

    /**
     * 添加方法
     */
    private Method getOtherInteger(String methodName,
                                   String daoName,
                                   IntrospectedTable introspectedTable,
                                   String tableName,
                                   int type) {
        Method method = new Method();
        method.setName(methodName);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        String params = addParams(introspectedTable, method, type);
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("return this.");
        sb.append(getDaoShort());
        if (introspectedTable.hasBLOBColumns()
            && (!"saveByPrimaryKeySelective".equals(methodName) && !"removeByPrimaryKey".equals(methodName)
                && !"removeByCriteria".equals(methodName) && !"saveByCriteriaSelective".equals(methodName))) {
            sb.append(daoName + "WithoutBLOBs");
        } else {
            sb.append(daoName);
        }
        sb.append("(");
        sb.append(params);
        sb.append(");");
        method.addBodyLine(sb.toString());
        return method;
    }

    /**
     * 添加方法
     */
    private Method getOtherInsertboolean(String methodName,
                                         String daoName,
                                         IntrospectedTable introspectedTable,
                                         String tableName) {
        Method method = new Method();
        method.setName(methodName);
        method.setReturnType(returnType);
        method.addParameter(new Parameter(pojoType, "record"));
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        if (returnType == null) {
            sb.append("this.");
        } else {
            sb.append("return this.");
        }
        sb.append(getDaoShort());
        sb.append(daoName);
        sb.append("(");
        sb.append("record");
        sb.append(");");
        method.addBodyLine(sb.toString());
        return method;
    }

    /**
     * type 的意义 pojo 1 key 2 example 3 pojo+example 4
     */
    private String addParams(IntrospectedTable introspectedTable, Method method, int type1) {
        switch (type1) {
            case 1:
                method.addParameter(new Parameter(pojoType, "record"));
                return "record";
            case 2:
                if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                    FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
                    method.addParameter(new Parameter(type, "key"));
                } else {
                    for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                        FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                        method.addParameter(new Parameter(type, introspectedColumn.getJavaProperty()));
                    }
                }
                StringBuffer sb = new StringBuffer();
                for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                    sb.append(introspectedColumn.getJavaProperty());
                    sb.append(",");
                }
                sb.setLength(sb.length() - 1);
                return sb.toString();
            case 3:
                method.addParameter(new Parameter(pojoCriteriaType, "condition"));
                return "condition";
            case 4:

                method.addParameter(0, new Parameter(pojoType, "record"));
                method.addParameter(1, new Parameter(pojoCriteriaType, "condition"));
                // if(method.getName().equals("updateByExampleSelective")||method.getName().equals("updateByExample")){
                // return "record, example.getCondition()";
                // }
                return "record, condition";
            default:
                break;
        }
        return null;
    }

    // private void addComment(JavaElement field, String comment) {
    // StringBuilder sb = new StringBuilder();
    // field.addJavaDocLine("/**");
    // sb.append(" * ");
    // comment = comment.replaceAll(OutputUtilities.lineSeparator, "<br>"+OutputUtilities.lineSeparator+"\t * ");
    // sb.append(comment);
    // field.addJavaDocLine(sb.toString());
    // field.addJavaDocLine(" */");
    // }

    /**
     * BaseUsers to baseUsers
     * 
     * @param tableName
     * @return
     */
    private String toLowerCase(String tableName) {
        StringBuilder sb = new StringBuilder(tableName);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * baseUsers to BaseUsers
     * 
     * @param tableName
     * @return
     */
    private String toUpperCase(String tableName) {
        StringBuilder sb = new StringBuilder(tableName);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    private String getDaoShort() {
        return toLowerCase(daoType.getShortName()) + ".";
    }

    public boolean clientInsertMethodGenerated(Method method,
                                               Interface interfaze,
                                               IntrospectedTable introspectedTable) {
        returnType = method.getReturnType();
        return true;
    }
}
