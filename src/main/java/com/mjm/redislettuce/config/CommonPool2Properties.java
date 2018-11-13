package com.mjm.redislettuce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class CommonPool2Properties {
    @Value("${lettuce.pool.maxTotal}")
    private Integer maxTotal;

    @Value("${lettuce.pool.maxIdle}")
    private Integer maxIdle;

    @Value("${lettuce.pool.minIdle}")
    private Integer minIdle;

    //TODO 其他属性
}