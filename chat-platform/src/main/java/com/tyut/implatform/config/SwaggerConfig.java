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
        contact.setName("TYUT CHAT");
        return new OpenAPI().info(new Info()
            .title("TYUT CHAT接口文档")
            .description("TYUT CHAT业务平台服务")
            .contact(contact)
            .version("3.0")
            .termsOfService("https://your-domain.com")
            .license(new License().name("MIT")
                .url("https://your-domain.com")));
    }

}
