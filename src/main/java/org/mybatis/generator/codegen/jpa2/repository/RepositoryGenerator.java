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
package org.mybatis.generator.codegen.jpa2.repository;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.SelectByExampleWithoutBLOBsMethodGenerator;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.xsili.GenHelper;
import org.mybatis.generator.xsili.plugins.util.PluginUtils;

/**
 * 
 * @author 叶鹏
 * @date 2017年12月13日
 */
public class RepositoryGenerator extends AbstractJavaClientGenerator {

    private FullyQualifiedJavaType annotationModifyingType = new FullyQualifiedJavaType("org.springframework.data.jpa.repository.Modifying");
    private FullyQualifiedJavaType annotationQueryType = new FullyQualifiedJavaType("org.springframework.data.jpa.repository.Query");
    
    public RepositoryGenerator() {
        super(false);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        progressCallback.startTask(getString("Progress.17", introspectedTable.getFullyQualifiedTable().toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        Interface interfaze = new Interface(type);
        interfaze.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(interfaze);

        String rootInterface = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        if (!stringHasValue(rootInterface)) {
            rootInterface = context.getJavaClientGeneratorConfiguration().getProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        }

        if (stringHasValue(rootInterface)) {
            FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(rootInterface);
            interfaze.addSuperInterface(fqjt);
            interfaze.addImportedType(fqjt);
        }

        FullyQualifiedJavaType modelType = introspectedTable.getRules().calculateAllFieldsClass();
        interfaze.addImportedType(modelType);

        FullyQualifiedJavaType jpaRepositoryType = new FullyQualifiedJavaType("org.springframework.data.jpa.repository.JpaRepository");
        jpaRepositoryType.addTypeArgument(modelType);
        jpaRepositoryType.addTypeArgument(PluginUtils.calculateKeyType(introspectedTable));
        interfaze.addSuperInterface(new FullyQualifiedJavaType(jpaRepositoryType.getShortName()));
        interfaze.addImportedType(jpaRepositoryType);

        FullyQualifiedJavaType querydslPEType = new FullyQualifiedJavaType("org.springframework.data.querydsl.QuerydslPredicateExecutor");
        querydslPEType.addTypeArgument(modelType);
        interfaze.addSuperInterface(new FullyQualifiedJavaType(querydslPEType.getShortName()));
        interfaze.addImportedType(querydslPEType);

        // 添加方法
        // 逻辑删除方法
//        addUpdateDeleted(interfaze, modelType);
        
        // TODO 添加 listWithoutBLOBs
        // addSelectByExampleWithoutBLOBsMethod(interfaze);

        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
        if (context.getPlugins().clientGenerated(interfaze, null, introspectedTable)) {
            answer.add(interfaze);
        }

        return answer;
    }
    
    @SuppressWarnings("unused")
    private void addUpdateDeleted(Interface interfaze, FullyQualifiedJavaType modelType) {
        IntrospectedColumn logicDeletedColumn = GenHelper.getLogicDeletedColumn(introspectedTable);
        if(logicDeletedColumn != null) {
            Method updateDeletedMethod = new Method();
            updateDeletedMethod.setName("updateDeleted");
            updateDeletedMethod.setReturnType(FullyQualifiedJavaType.getIntInstance());
            
            // 拼接hql
            StringBuilder hql = new StringBuilder();
            List<IntrospectedColumn> keyColumns = introspectedTable.getPrimaryKeyColumns();
            hql.append("update ").append(modelType.getShortName());
            hql.append(" set ").append(logicDeletedColumn.getJavaProperty() + " = ?").append(keyColumns.size() + 1);
            // 更新时间
            Parameter updatedDateParameter = null;
            String updatedTimeField = GenHelper.getUpdatedTimeField(introspectedTable);
            for (IntrospectedColumn updatedDateColumn : introspectedTable.getAllColumns()) {
                if (updatedTimeField.equals(updatedDateColumn.getJavaProperty())) {
                    hql.append(", ").append(updatedDateColumn.getJavaProperty() + " = ?").append(keyColumns.size() + 2);
                    
                    interfaze.addImportedType(updatedDateColumn.getFullyQualifiedJavaType());
                    updatedDateParameter = new Parameter(updatedDateColumn.getFullyQualifiedJavaType(), updatedDateColumn.getJavaProperty());
                }
            }
            hql.append(" where ");
            for (int i = 0; i < keyColumns.size(); i++) {
                IntrospectedColumn keyColumn = keyColumns.get(i);
                hql.append(keyColumn.getJavaProperty() + " = ?").append(i + 1);
                // 如果不是最后一个参数, 则拼接 and
                if (i + 1 < keyColumns.size()) {
                    hql.append(" and ");
                }
                updateDeletedMethod.addParameter(new Parameter(keyColumn.getFullyQualifiedJavaType(), keyColumn.getJavaProperty()));
            }
            updateDeletedMethod.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), logicDeletedColumn.getJavaProperty()));
            if(updatedDateParameter != null) {
                updateDeletedMethod.addParameter(updatedDateParameter);
            }
            
            updateDeletedMethod.addAnnotation("@Modifying");
            updateDeletedMethod.addAnnotation("@Query(\"" + hql.toString() + "\")");
            interfaze.addImportedType(annotationModifyingType);
            interfaze.addImportedType(annotationQueryType);
            
            interfaze.addMethod(updateDeletedMethod);
        }
    }

    protected void addSelectByExampleWithoutBLOBsMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateSelectByExampleWithoutBLOBs()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new SelectByExampleWithoutBLOBsMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void initializeAndExecuteGenerator(AbstractJavaMapperMethodGenerator methodGenerator,
                                                 Interface interfaze) {
        methodGenerator.setContext(context);
        methodGenerator.setIntrospectedTable(introspectedTable);
        methodGenerator.setProgressCallback(progressCallback);
        methodGenerator.setWarnings(warnings);
        methodGenerator.addInterfaceElements(interfaze);
    }

    @Override
    public AbstractXmlGenerator getMatchedXMLGenerator() {
        throw new UnsupportedOperationException();
    }
}
