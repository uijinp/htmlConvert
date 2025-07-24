package com.htmlConvert.buj.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // http://localhost:8080/images/generated/파일이름.png 요청을
        // 프로젝트 루트의 'generated-images' 폴더에 있는 파일과 연결합니다.
        registry.addResourceHandler("/images/generated/**")
                .addResourceLocations("file:generated-images/");
    }
}