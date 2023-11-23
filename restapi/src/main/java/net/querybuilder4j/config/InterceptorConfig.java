package net.querybuilder4j.config;

import net.querybuilder4j.interceptor.MdcInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Configuration
public class InterceptorConfig {

    @Bean
    public MappedInterceptor buildMdcInterceptor() {
        return new MappedInterceptor(null, new MdcInterceptor());
    }

}
