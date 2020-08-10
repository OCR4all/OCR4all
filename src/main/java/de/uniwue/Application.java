package de.uniwue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.*;
import org.springframework.web.context.support.*;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.*;

@SpringBootApplication
@EnableWebMvc
@ServletComponentScan
public class Application implements WebApplicationInitializer {
  public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

  @Override
  public void onStartup(ServletContext container) {
    ConfigurableWebApplicationContext context = new AnnotationConfigWebApplicationContext();
    context.setConfigLocation("de.uniwue.config");

    container.addListener(new ContextLoaderListener(context));

    ServletRegistration.Dynamic dispatcher = container
            .addServlet("dispatcher", new DispatcherServlet(context));

    container.setSessionTimeout(6 * 60);

    dispatcher.setLoadOnStartup(1);
    dispatcher.addMapping("/");
  }
}
