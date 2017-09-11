#### Overview
>>本项目基于mybatis-generator-core进行构建, 主要更新如下:

1. 添加了SpringMvcController, Service, MapperJunitTest三个代码生成插件
2. 集成Mybatis分页插件 [xsili-mybatis-plugin-page](https://github.com/morningtea/xsili-mybatis-plugin-page "Mybatis分页插件")
4. 新增自定义注释器XsiliCommentGenerator, 支持生成Model field,getter,setter的注释(读取数据库注释), 以及Mapper.xml"@mbg.generated"注释
3. 扩展MapperAnnotationPlugin, 添加对 annotationClass annotationName 配置支持. 可以配置为Mapper类添加@Repository注解

#### Get started
##### 示例
>>插件支持hsqldb数据库内存模式, 只需要执行 MyBatisGeneratorTest即可生成样例代码.  
1. 测试脚本 src/test/resources/hsqldb-test.sql
2. 测试入口 src/test/java/generator/MyBatisGeneratorTest.java

##### 实际开发使用步骤
1. 配置test/resources/generator.xml. 包括数据库连接, 需要生成的表, 及其他可选配置
2. 以junit的方式运行test/java/generator/MyBatisGeneratorTest.java
3. 把生成的文件copy到自己的工程

#### 兼容mybatis-generator-core:
1. 扩展MapperAnnotationPlugin, 添加对 annotationClass annotationName 配置支持
2. 扩展SerializablePlugin, 为ExampleClass添加serialVersionUID (添加覆盖方法  @Override#modelExampleClassGenerated)
3. 扩展org.mybatis.generator.api.dom.java.Method, 添加方法removeBodyLines()
4. JavaMapperGenerator, XMLMapperGenerator, 屏蔽了以下方法已经对应的 Mapper sql.  
addSelectByExampleWithBLOBsMethod(interfaze);  
addInsertMethod(interfaze);  
addUpdateByExampleSelectiveMethod(interfaze);  
addUpdateByExampleWithBLOBsMethod(interfaze);  
addUpdateByExampleWithoutBLOBsMethod(interfaze);  
addUpdateByPrimaryKeyWithoutBLOBsMethod(interfaze);  

>>除了第4点, 以上修改均是功能扩展, 可以兼容MBG的原有功能和配置项.

>>其他
1. src/site 是mybatis-generator-core-1.3.6的官方文档
2. 支持MySQL, Oracle, PostgreSQL等数据库, 只要该数据库的JDBC驱动实现了DatabaseMetaData接口, 特别是"getColumns"和"getPrimaryKeys"方法
