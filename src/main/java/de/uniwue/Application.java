package de.uniwue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.*;

@SpringBootApplication
@EnableWebMvc
@ServletComponentScan
@Configuration
public class Application extends SpringBootServletInitializer {
  public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

  @Override
  public void onStartup(ServletContext container) throws ServletException {
    container.setSessionTimeout(6 * 60);
    super.onStartup(container);
  }
}
