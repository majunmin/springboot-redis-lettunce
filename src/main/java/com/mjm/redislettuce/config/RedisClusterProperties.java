package com.mjm.redislettuce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by majunmin on 2018/11/13.
 */
@Getter
@Setter
@Component
public class RedisClusterProperties {

    @Value("redis.cluster.nodes")
    private String nodes;
    @Value("redis.cluster.timeout")
    private String timeout;
    @Value("redis.cluster.max-redirects")
    private String maxRedirects;
}
