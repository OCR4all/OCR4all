package de.uniwue.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
  }


  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {

    // Set async timeout to 1h (workaround for long lasting requests) -->
    configurer.setDefaultTimeout(3600000);
  }

}

