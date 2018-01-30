package myzd.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;


/**
 * @author Diamond
 */
@Configuration
@EnableSwagger2
@Slf4j
@ControllerAdvice(basePackages = "myzd.api.controllers")
@Profile({"!prod"})
public class SwaggerConfiguration extends WebMvcConfigurerAdapter {

  private final static String SWAGGER_UI_HEADER = "Referer";

  private final static String SWAGGER_UI_URL_HOST = "swagger-ui.mingyizhudao.com";

  /**
   * 初始化文档信息
   *
   * @return 文档信息
   */
  private ApiInfo initApiInfo() {
    Contact contact = new Contact("后端团队", "", "");
    return new ApiInfo(
      "test",
      "test",
      "1.0",
      "",
      contact,
      "Apache 2.0",
      "http://www.apache.org/licenses/LICENSE-2.0",
      new ArrayList<>()
    );
  }

  @Bean
  public Docket api() {
    Docket docket = new Docket(DocumentationType.SWAGGER_2);
    return docket
      .apiInfo(initApiInfo())
      .select()
      .apis(RequestHandlerSelectors.basePackage("myzd.api.controllers"))
      .paths(PathSelectors.any())
      .build();
  }

  @Bean
  public OncePerRequestFilter swaggerCorsFilter() {
    return new OncePerRequestFilter() {
      @Override
      protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getHeader(SWAGGER_UI_HEADER) != null && request.getHeader(SWAGGER_UI_HEADER).contains(SWAGGER_UI_URL_HOST)) {
          response.addHeader(
            "Access-Control-Allow-Origin",
            "http://swagger-ui.mingyizhudao.com"
          );
          response.addHeader(
            "Access-Control-Allow-Headers",
            "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range,Authorization"
          );
          response.addHeader(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, PATCH, OPTIONS"
          );
        }
        filterChain.doFilter(request, response);
      }
    };
  }
}
