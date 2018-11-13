package com.mjm.redislettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Created by majunmin on 2018/11/13.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Test
    public void test1() {
        // Syntax: redis://[password@]host[:port][/databaseNumber]
        RedisClient redisClient = RedisClient.create("redis://47.105.144.63:6379");

        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.set("key", "Hello, Redis!");

        connection.close();
        redisClient.shutdown();

    }

    @Test
    public void testConnectionPool() {

        ExecutorService pool = Executors.newFixedThreadPool(10);
        LocalDateTime nPoolStartTime = LocalDateTime.now();
        IntStream.range(0, 10000).forEach((i) -> {
            redisTemplate.opsForList().leftPop("myqueue");
        });
        LocalDateTime nPoolEndTime = LocalDateTime.now();

        System.out.println("pooled pop 10000 data : " + Duration.between(nPoolStartTime, nPoolEndTime).toMillis()  + "ms"); //

        LocalDateTime poolStartTime = LocalDateTime.now();
        IntStream.range(0, 10000).forEach((i) -> {
            redisTemplate.opsForList().leftPop("myqueue");
        });
        LocalDateTime poolEndTime = LocalDateTime.now();
        System.out.println("pooled pop 10000 data : " + Duration.between(poolStartTime, poolEndTime).toMillis() + "ms"); //




    }


    /**
     * result
     *         npip      pip
     * 10000  54001ms    167ms
     *
     */
    @Test
    public void testLettucePip() {

        LocalDateTime nPipStartTime = LocalDateTime.now();
        /**
         * not pipline
         */
        List<Object> results1 = stringRedisTemplate.execute(
                connection -> {
                    StringRedisConnection stringRedisConn = (StringRedisConnection)connection;
                    for(int i=0; i< 10000; i++) {
                        stringRedisConn.rPush("myqueue", i+"");
                    }
                    return null;
                }, false, false);

        LocalDateTime nPipEndTime = LocalDateTime.now();
        System.out.println("not pip push 10000 data: " + Duration.between(nPipStartTime, nPipEndTime).toMillis() + "ms");

        /**
         * pipline
         *
         * RedisCallback
         *
         *
         * SessionCallback
         *     sessionCallback 使多个命令在同一个redisConnection里执行,实现Trancation
         */
        LocalDateTime pipStartTime = LocalDateTime.now();
        List<Object> results2 = stringRedisTemplate.executePipelined(
                (RedisCallback<Object>) connection -> {
                    StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
                    for (int i = 0; i < 10000; i++) {
                        stringRedisConn.rPush("myqueue", i+"");
                    }
                    return null;
                });

        LocalDateTime pipEndTime = LocalDateTime.now();
        System.out.println("pipline push 10000 data : " + Duration.between(pipStartTime, pipEndTime).toMillis());

    }


    /**
     * test lettunce threadSafe
     */
    @Test
    public void testThreadSafeLettuce() throws InterruptedException {
        // TODO 测试线程安全
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        IntStream.range(0, 1000).forEach(i ->
                executorService.execute(() -> stringRedisTemplate.opsForValue().increment("kk", 1))
        );
        Thread.currentThread().join();
//        stringRedisTemplate.opsForValue().set("k1", "v1");
//        final String k1 = stringRedisTemplate.opsForValue().get("k1");
//        log.info("[字符缓存结果] - [{}]", k1);
//        // TODO 以下只演示整合，具体Redis命令可以参考官方文档，Spring Data Redis 只是改了个名字而已，Redis支持的命令它都支持
//        String key = "battcn:user:1";
//        redisTemplate.opsForValue().set(key, new User(1L, "u1", "pa"));
//        // TODO 对应 String（字符串）
//        final User user = (User) redisTemplate.opsForValue().get(key);
//        log.info("[对象缓存结果] - [{}]", user);

    }
}
