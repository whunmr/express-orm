expresss-orm
============

Object Relational Mapping Framework
惯例扯一下ORM这东西的缘由。ORM的产生也是有历史背景的，主要是从数据库中心结构到内存计算的过渡中产生的。早期的MIS系统协同性要求不高，1个人用一用就可以了，所以应用是直接写在数据库上的。比如，dBase啊，FoxBase啊之类的，调用个功能都是在db console上完成的。数据库即是数据的存储中心也是计算的中心。后来企业开始大规模信息化，协同性的要求增加，需要多人使用系统，出现了C/S结构。这时候就出现一个非常核心的问题，数据库到底是数据功能还是需要包含计算功能？数据库厂商的答案是都做，所以各种存储过程各种SQL扩展都出来了。理由也充分，1.快 2.SQL本来是给non-dev准备的，所以扩展的sql也不难，你们找半职业程序员这事也能搞定。但是，自大的数据库厂商忽略了，C/S时代前端后端其实差不多重要。而当时的前端是对象语言的世界。于是冲突就来了，业务逻辑放在哪？是靠近数据获取性能，还是靠近ui获取更好的维护性？以过往历史而言，最后选择放在对象里的人占据了大多素。

但是并不是选完了就完了，还有很多后续问题，两套类型系统，schema，class表现力不同，要怎么处理？最后各类ORM框架就出来了。各种模式都有，SQLMapper啊，ORM啊，ActiveRecord啊。最后这个因RoR而名声大噪，其实Delphi里就有了，相当古老的pattern呢。

那么我们今天来挑战的就是ORM/Active Record框架。

功能要求

1. the implementation must support table per class mapping
2. the implementation must support primitive type mapping(int, long, String etc.) and enum mapping
3. the implementation must support one to many relationship mapping, and the object representation of this relationship could be List, Set or Array
4. the implementation must not have N+1 SQL problem
5. the implementation must support one of the following RDBMSes: H2, MySQL or PostgreSQL

集成要求
1. improve the demo app from last round, to support post data to database
2. improve the demo app from last round, to support rendering object with 1-N relationship
3. you must use your own MVC and DI container(or Guice if you failed the DI challenge)


how to using instrument lib
---------------------------
0. `gradle clean idea`
1. in instrument folder, execute `gradle jar`
2. setup configuration so we can using the instrument agent jar, when running junit test.
In IntelliJ IDEA,  Run-->Edit Configurations…-->Defaults-->JUnit:
set "VM Options:" to **-javaagent:./instrument/build/libs/instrument.jar**
