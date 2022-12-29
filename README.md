## KRONOS：一个解决宽表的方案

kronos是一个解决微服务体系宽表问题的框架。针对宽表的实现，有一些开源实现方案，如[Netflix 推出数据同步和增强平台 Delta](https://blog.51cto.com/u_15471709/4868156)、[flink cdc](https://github.com/ververica/flink-cdc-connectors)。但上述框架多多少少都存在一些场景不适合或者侵入性较强的问题。



Delta需要业务编程嵌入式代码，侵入性较强，flink cdc无法解决es中嵌套结构数据同步问题



kronos的算法源自本人在上一家公司主导的一个宽表自研框架，经过公司内部的大量线上经验，之前工程实现的弊端导致某些问题已无法解决，比如性能、N：1问题、读写库延迟、数据事件优先级问题、项目可维护性与成本变高等

### 架构设计图

![kronos.drawio](https://tva1.sinaimg.cn/large/008vxvgGly1h9kr9y4n7mj30u60htjsh.jpg)

* 数据源模拟flink cdc的框架实现结合*Debezium*实现
* 算子数据流采用Disruptor实现

### How to usage

* 想了解框架实现与效果，可以运行集成测试命令

​		`mvn test -Dtest=MySqlSourceExampleTest -DfailIfNoTests=false`

* 运行业务配置命令：`java -jar core-1.0-SNAPSHOT-jar-with-dependencies.jar self-config.yml` 
