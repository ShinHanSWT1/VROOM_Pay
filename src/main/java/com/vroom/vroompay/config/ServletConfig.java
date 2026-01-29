package com.vroom.vroompay.config;

import com.vroom.vroompay.interceptor.ApiKeyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebMvc // <annotation-driven /> 역할
@ComponentScan(basePackages = "com.vroom.vroompay")
public class ServletConfig implements WebMvcConfigurer {

    // API 키 인터셉터 주입
    @Autowired
    private ApiKeyInterceptor apiKeyInterceptor;

    // 인터셉터 등록 - /pay/** 요청에 API 키 검증 적용
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/pay/**");
    }

    // JSP 위치 설정 (VROOM의 ViewResolver 역할)
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setPrefix("/WEB-INF/views/");
        bean.setSuffix(".jsp");
        registry.viewResolver(bean);
    }

    // 정적 리소스 설정 (css, js, img 등)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }
    // UTF-8 전역 설정 (extendMessageConverters 사용 - 기존 Jackson 컨버터 유지)
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
    }
}