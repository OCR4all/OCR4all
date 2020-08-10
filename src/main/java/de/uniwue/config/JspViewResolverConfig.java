package de.uniwue.config;


import org.springframework.context.annotation.*;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.*;

@Configuration
public class JspViewResolverConfig {
    @Bean
    public ViewResolver jspViewResolver() {
        return new InternalResourceViewResolver("/WEB-INF/views/",".jsp");
    }
}