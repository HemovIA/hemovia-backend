package com.hemovia.api.infrastructure.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*
Liga a leitura e a validação das propriedades "hemovia" ao contexto do Spring.
*/
@Configuration
@EnableConfigurationProperties(HemoviaProperties.class)
public class HemoviaPropertiesConfiguration {
}
