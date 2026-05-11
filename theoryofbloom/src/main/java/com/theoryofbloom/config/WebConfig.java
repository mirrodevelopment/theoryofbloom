package com.theoryofbloom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        Path returnsDir = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "static", "images", "returns");
        String returnsPath = returnsDir.toFile().getAbsolutePath().replace("\\", "/");
        
        registry.addResourceHandler("/images/returns/**")
                .addResourceLocations("file:///" + returnsPath + "/");
    }
}
