package com.hemovia.api.service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RoutingProperties.class)
public class RoutingConfiguration {
}
