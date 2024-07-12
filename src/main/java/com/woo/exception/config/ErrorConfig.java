package com.woo.exception.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.woo.exception.dto.ErrorInfo;
import lombok.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
public class ErrorConfig {

    private Map<String, ErrorInfo> errors = new HashMap<>();

    public static ErrorConfig build() throws Exception {
        return new ErrorConfig().setResource("/error/exception.yml");
    }

    public ErrorConfig setResource(String path) throws Exception {
        Resource resource = new ClassPathResource(path);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        this.errors = mapper.readValue(resource.getInputStream(), mapper.getTypeFactory().constructMapType(HashMap.class, String.class, ErrorInfo.class));

        return this;
    }
}
