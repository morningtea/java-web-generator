====
       Copyright 2006-2016 the original author or authors.

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.
====

===============================================================================

#### Overview
本项目基于mybatis-generator-core进行构建, 主要更新如下:

1. 添加了SpringMvcController, Service, MapperJunitTest三个代码生成插件
2. 对原有功能进行了增强:  
a) Model添加字段注释(对应数据库列的注释)  
b) 为Mapper类添加@Repository注解(可配置)


#### How to start
1. 配置test/resources/generator.xml. 包括数据库连接, 需要生成的表, 及其他可选配置
2. 以junit的方式运行test/java/generator/MyBatisGeneratorTest.java
3. 把生成的文件copy到自己的工程


#### 其他说明
1. src/site 是mybatis-generator-core-1.3.6的官方文档
2. 支持MySQL, Oracle, PostgreSQL等数据库, 只要该数据库的JDBC驱动实现了DatabaseMetaData接口,
特别是"getColumns"和"getPrimaryKeys"方法

