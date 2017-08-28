#### Overview
本项目基于mybatis-generator-core进行构建, 主要更新如下:

1. 添加了SpringMvcController, Service, MapperJunitTest三个代码生成插件
2. 集成Mybatis分页插件 [xsili-mybatis-plugin-page](https://github.com/morningtea/xsili-mybatis-plugin-page "Mybatis分页插件")  
3. 对原有功能进行了增强:  
a) Model添加字段注释(对应数据库列的注释)  
b) 为Mapper类添加@Repository注解(可配置)

#### Get started
插件支持hsqldb数据库内存模式, 只需要执行 MyBatisGeneratorTest即可生成样例代码.  
1. 测试脚本 src/test/resources/hsqldb-test.sql
2. 测试入口 src/test/java/generator/MyBatisGeneratorTest.java

#### How to start
1. 配置test/resources/generator.xml. 包括数据库连接, 需要生成的表, 及其他可选配置
2. 以junit的方式运行test/java/generator/MyBatisGeneratorTest.java
3. 把生成的文件copy到自己的工程


#### 其他说明
1. src/site 是mybatis-generator-core-1.3.6的官方文档
2. 支持MySQL, Oracle, PostgreSQL等数据库, 只要该数据库的JDBC驱动实现了DatabaseMetaData接口,
特别是"getColumns"和"getPrimaryKeys"方法

