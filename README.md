### Overview
本项目基于mybatis-generator-core进行构建, 主要更新如下:

1. 添加了SpringMvcControllerPlugin, ServicePlugin, MybatisMapperJunitPlugin三个代码生成插件
2. 集成Mybatis分页插件 [xsili-mybatis-plugin-page](https://github.com/morningtea/xsili-mybatis-plugin-page "Mybatis分页插件")
3. 新增自定义注释器XsiliCommentGenerator, 支持生成Model field,getter,setter的注释(读取数据库注释), 以及Mapper.xml"@mbg.generated"注释
4. 扩展MapperAnnotationPlugin, 添加对 annotationClass annotationName 配置支持. 可以配置为Mapper类添加@Repository注解

添加对JPA2的支持
1. 新增org.mybatis.generator.codegen.jpa2包
2. 新增Jpa2ModelAnnotationPlugin, Jpa2RepositoryJunitPlugin插件, 生成model注解和Repository测试类

说明:
1. SpringMvcControllerPlugin 支持生成传统风格和restful风格的API, 配置参数enableRestful即可
2. 支持对实体类字段枚举化. 在数据库列字段添加以下格式的注释即可, 支持中/英文逗号分隔. (暂时只支持Jpa2模式)
     * {enum: UNAUDITED(待审核), AUDIT_PASS(审核通过), AUDIT_NOT_PASS(审核未通过)}  
     
TODO 支持Oracle, PostGresql(添加驱动)


### Get started
#### 示例
插件支持hsqldb数据库内存模式, 只需要执行 MyBatisGeneratorTest/Jpa2GeneratorTest 即可生成样例代码.  
1. 测试脚本 src/test/resources/hsqldb-test.sql
2. 测试入口   
src/test/java/generator/MyBatisGeneratorTest.java  
src/test/java/generator/Jpa2GeneratorTest.java

#### 实际开发使用步骤
1. 配置test/resources/xsili-mybatis-generator.xml / xsili-jpa-generator.xml. 包括数据库连接, 需要生成的表, 及其他可选配置
2. 以junit的方式运行test/java/generator/MyBatisGeneratorTest.java / Jpa2GeneratorTest.java
3. 把生成的文件copy到自己的工程


### 兼容mybatis-generator-core:
1. 扩展MapperAnnotationPlugin, 添加对 annotationClass annotationName 配置支持
2. 扩展SerializablePlugin, 为ExampleClass添加serialVersionUID (添加覆盖方法  @Override#modelExampleClassGenerated)
3. 扩展org.mybatis.generator.api.dom.java.Method, 添加方法removeBodyLines()
4. 扩展IntrospectedTable/ObjectFactory, 添加TargetRuntime.JPA2
5. 新增XsiliJavaBeansUtil, 继承JavaBeansUtil
6. JavaMapperGenerator, XMLMapperGenerator, 屏蔽了以下方法已经对应的 Mapper sql.  
addSelectByExampleWithBLOBsMethod(interfaze);  
addInsertMethod(interfaze);  
addUpdateByExampleSelectiveMethod(interfaze);  
addUpdateByExampleWithBLOBsMethod(interfaze);  
addUpdateByExampleWithoutBLOBsMethod(interfaze);  
addUpdateByPrimaryKeyWithoutBLOBsMethod(interfaze);  
7. 修改Plugin#contextGenerateAdditionalJavaFiles方法签名, 添加List<TopLevelClass> modelClasses参数

除了第6点、第7点, 以上修改均是功能扩展, 可以兼容MBG的原有功能和配置项.

>其他
>1. src/site 是mybatis-generator-core-1.3.6的官方文档
>2. 支持MySQL, Oracle, PostgreSQL等数据库, 只要该数据库的JDBC驱动实现了DatabaseMetaData接口, 特别是"getColumns"和"getPrimaryKeys"方法
