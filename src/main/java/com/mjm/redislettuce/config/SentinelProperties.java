package com.mjm.redislettuce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Component
public class SentinelProperties {
    @Value("${lettuce.sentinel.master}")
    private String master;
    @Value("${lettuce.sentinel.nodes}")
    private String nodes;

    private Set<String> hosts;

    @PostConstruct
    public void hosts() {
        hosts = new HashSet<>();
        hosts.addAll(Arrays.asList(nodes.split(",")));
    }
}