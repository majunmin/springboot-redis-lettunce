package com.mjm.redislettuce.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.Map;

@PropertySource(value = {"classpath:config/redis.properties" })
@Configuration
public class RedisClientConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setKeySerializer(new StringRedisSerializer());
        stringRedisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        //todo 定制化配置
        return stringRedisTemplate;
    }

    @Bean("redisPoolTempalte")
    public RedisTemplate redisPoolTempalte(RedisConnectionFactory redisPoolConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(redisSerializer);
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        //todo 定制化配置
        return redisTemplate;
    }

    @Bean("redisTempalte")
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //指定序列化输入类型， 类型必须是非 final
        //final修饰的类 String Integer  会抛出异常
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        //使用StringRedisSerializer来序列化和反序列化redis的key值
        redisTemplate.setKeySerializer(redisSerializer);
        //值采用json序列化
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //todo 定制化配置
        return redisTemplate;
    }

    /**
     *
     * @param redisConfiguration
     * @return
     */
    @Bean("redisConnectionFactory")
    public LettuceConnectionFactory lettuceConnectionFactory( RedisConfiguration redisConfiguration) {
        return new LettuceConnectionFactory(redisConfiguration);
    }

    /**
     * @param redisConfiguration   集群配置
     * @param lettuceClientConfiguration    连接池配置
     * @return
     */
    @Bean("redisPoolConnectionFactory")
    public LettuceConnectionFactory lettuceConnectionFactory(RedisConfiguration redisConfiguration, LettuceClientConfiguration lettuceClientConfiguration) {
        return new LettuceConnectionFactory(redisConfiguration, lettuceClientConfiguration);
    }

    /**
     * 配置哨兵集群信息 master和host:ip
     * @param sentinelProperties  集群Properties
     * @return redisSentinelConfiguration
     */
    @Bean
    public RedisSentinelConfiguration redisSentinelConfiguration(SentinelProperties sentinelProperties) {
        return new RedisSentinelConfiguration(sentinelProperties.getMaster(), sentinelProperties.getHosts());
    }


    /**
     * 配置redis 集群信息
     * @return
     */
    @Bean
    public RedisConfiguration redisConfiguration(){
        Map<String, Object> source = new HashMap<String, Object>();
        source.put("spring.redis.cluster.nodes", environment.getProperty("spring.redis.cluster.nodes"));
        source.put("spring.redis.cluster.timeout", environment.getProperty("spring.redis.cluster.timeout"));
        source.put("spring.redis.cluster.max-redirects", environment.getProperty("spring.redis.cluster.max-redirects"));

        return new RedisClusterConfiguration(new MapPropertySource("RedisClusterConfiguration", source));
    }



    /**
     * 配置LettuceClientConfiguration 包括线程池配置和安全项配置
     * @param genericObjectPoolConfig common-pool2线程池
     * @return lettuceClientConfiguration
     */
    @Bean
    public LettuceClientConfiguration lettuceClientConfiguration(GenericObjectPoolConfig genericObjectPoolConfig) {
        return LettucePoolingClientConfiguration.builder()
                .poolConfig(genericObjectPoolConfig)
                .build();
    }

    @Bean
    public GenericObjectPoolConfig genericObjectPoolConfig(CommonPool2Properties commonPool2Properties) {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(commonPool2Properties.getMaxIdle());
        poolConfig.setMinIdle(commonPool2Properties.getMinIdle());
        poolConfig.setMaxTotal(commonPool2Properties.getMaxTotal());
        //todo 其他配置
        return poolConfig;
    }
}