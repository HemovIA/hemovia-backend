package com.hemovia.api.controller.config;

import com.hemovia.api.dto.request.DonationChatRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class DonationChatRequestConfiguration implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        JsonMapper mapper = JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .withCoercionConfig(String.class, config -> config
                        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail))
                .build();
        builder.addCustomConverter(new JacksonJsonHttpMessageConverter(mapper) {
            @Override
            public boolean canRead(ResolvableType type, MediaType mediaType) {
                return type.resolve() == DonationChatRequest.class && super.canRead(type, mediaType);
            }

            @Override
            public boolean canWrite(ResolvableType type, Class<?> valueClass, MediaType mediaType) {
                return false;
            }

            @Override
            public Object read(ResolvableType type, HttpInputMessage input, Map<String, Object> hints) throws IOException {
                try {
                    return super.read(type, input, hints);
                } catch (HttpMessageNotReadableException malformed) {
                    // O resolvedor do MVC pode registrar a exceção. A original inclui trechos do JSON.
                    throw new HttpMessageNotReadableException("JSON do chat inválido.", input);
                }
            }
        });
    }
}

