package com.vroom.vroompay.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:db.properties") // 아까 만든 properties 파일 로딩
@MapperScan("com.vroom.vroompay.mapper")   // 매퍼 인터페이스 위치 (곧 만들 예정)
@ComponentScan(basePackages = {"com.vroom.vroompay.service"})
public class RootConfig {

    // db.properties에 적은 이름 그대로 가져오기
    @Value("${db.driver}") private String driverClassName;
    @Value("${db.url}")      private String url;
    @Value("${db.username}") private String username;
    @Value("${db.password}") private String password;

    // HikariCP 데이터소스 설정 (DB 연결)
    @Bean
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        // 연결 풀 옵션 (필요하면 조정 가능)
        hikariConfig.setPoolName("vroompay-pool");
        hikariConfig.setMaximumPoolSize(10);

        return new HikariDataSource(hikariConfig);
    }

    // MyBatis 설정 (SQL 실행 공장)
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());

        // 매퍼 XML 파일 위치 지정 (resources/mappers 폴더)
        sessionFactory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:mappers/*.xml")
        );

        // 카멜케이스 자동 변환 (user_id -> userId)
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        sessionFactory.setConfiguration(configuration);

        return sessionFactory.getObject();
    }

    // 트랜잭션 매니저 (데이터 무결성 보장)
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }


    // @Value("${...}")를 해석해주는 필수 Bean
    @Bean
    public static org.springframework.context.support.PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new org.springframework.context.support.PropertySourcesPlaceholderConfigurer();
    }

}