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
package org.mybatis.generator.plugins;

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * 支持配置 annotationClass annotationName
 * 
 * @author 叶鹏
 * @date 2017年8月29日
 */
public class MapperAnnotationPlugin extends PluginAdapter {

	private static final String DEFAULT_ANNOTATION_CLASS = "org.apache.ibatis.annotations.Mapper";
	private static final String DEFAULT_ANNOTATION_NAME = "@Mapper";

	private String annotationClass;
	private String annotationName;

	@Override
	public void setProperties(Properties properties) {
		super.setProperties(properties);
		String annotationClass = properties.getProperty("annotationClass");
		String annotationName = properties.getProperty("annotationName");
		if (StringUtils.isBlank(annotationClass) && StringUtils.isBlank(annotationName)) {
			this.annotationClass = DEFAULT_ANNOTATION_CLASS;
			this.annotationName = DEFAULT_ANNOTATION_NAME;
		} else if (StringUtils.isNotBlank(annotationClass) && StringUtils.isNotBlank(annotationName)) {
			this.annotationClass = annotationClass;
			this.annotationName = annotationName;
		} else {
			throw new RuntimeException("annotationClass annotationName 需配对设置");
		}
	}

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        interfaze.addImportedType(new FullyQualifiedJavaType(annotationClass)); //$NON-NLS-1$
        interfaze.addAnnotation(annotationName); //$NON-NLS-1$
        return true;
    }
}
