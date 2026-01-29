package com.vroom.vroompay.config;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import javax.servlet.Filter;


// 서버 스위치
public class WebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    // RootConfig 연결 (DB, MyBatis, 서비스 등)
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { RootConfig.class };
    }

    // ServletConfig 연결 (컨트롤러, 뷰 리졸버 등)
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] { ServletConfig.class };
    }

    //  모든 요청("/")을 스프링이 가로채서 처리함
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    //  한글 깨짐 방지 필터 (UTF-8 강제 적용)
    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return new Filter[] { characterEncodingFilter };
    }
}