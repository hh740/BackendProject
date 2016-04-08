# BackendProject
一个台应用的所有基本框架
包括一个web项目,一个thrift项目和一个parent项目
maven依赖结构如下:
---- parent
             |-- pom.xml (pom)
             |
             |-- mysql  
             |        |-- pom.xml (jar)
             |
             |-- thrift...(parent文件中其他子项目)
             |        |-- pom.xml (jar)
             |
             |-- web-service
             |        |-- pom.xml (war)
             |
             |-- thrift-service
                      |-- pom.xml (war)
parent项目下主要包含对各种类库的简单封装和公共模块的设计与实现
web-servcie主要完成业务逻辑的处理,引用parent下封装好的功能模块
thrift-service主要完成rpc服务的调用
