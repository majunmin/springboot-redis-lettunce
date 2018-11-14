# springboot-redis (lettuce)

JedisCluster 不支持pipLine 操作

    java.lang.UnsupportedOperationException: Pipeline is currently not supported for JedisClusterConnection.
    
spring boot 2.0开始，配置spring-boot-starter-data-redis将不依赖Jedis，而是依赖Lettuce，在Lettuce中，redis cluster使用pipeline不会有问题。
[spring-data-redis中JedisCluster不支持pipelined问题解决](https://www.jianshu.com/p/7d7175e4616a)


