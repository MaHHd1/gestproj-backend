package com.gestproj.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GestProj Backend API")
                        .version("1.0.0")
                        .description("Project management backend API with task, member, and project management features")
                        .contact(new Contact()
                                .name("GestProj Team")
                                .url("https://github.com/gestproj")));
    }
}
