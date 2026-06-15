package com.tyut.implatform.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi userApi() {
        String[] paths = {"/**"};
        String[] packagedToMatch = {"com.tyut"};
        return GroupedOpenApi.builder().group("IM-Platform")
            .pathsToMatch(paths)
            .packagesToScan(packagedToMatch).build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact();
        contact.setName("太理朋友圈");
        return new OpenAPI().info(new Info()
            .title("太理朋友圈接口文档")
            .description("太理朋友圈业务平台服务")
            .contact(contact)
            .version("3.0")
            .termsOfService("https://your-domain.com")
            .license(new License().name("MIT")
                .url("https://your-domain.com")));
    }

}
