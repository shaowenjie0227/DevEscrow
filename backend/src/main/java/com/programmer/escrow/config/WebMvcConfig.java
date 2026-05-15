package com.programmer.escrow.config;

import com.programmer.escrow.admin.interceptor.AdminTokenInterceptor;
import com.programmer.escrow.auth.interceptor.DeveloperAccessInterceptor;
import com.programmer.escrow.config.properties.CorsProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminTokenInterceptor adminTokenInterceptor;
    private final DeveloperAccessInterceptor developerAccessInterceptor;
    private final CorsProperties corsProperties;

    @Value("${app.upload.base-dir:uploads}")
    private String uploadBaseDir;

    public WebMvcConfig(AdminTokenInterceptor adminTokenInterceptor,
                        DeveloperAccessInterceptor developerAccessInterceptor,
                        CorsProperties corsProperties) {
        this.adminTokenInterceptor = adminTokenInterceptor;
        this.developerAccessInterceptor = developerAccessInterceptor;
        this.corsProperties = corsProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminTokenInterceptor)
                .addPathPatterns("/api/admin/**");
        registry.addInterceptor(developerAccessInterceptor)
                .addPathPatterns("/api/developer/**")
                .excludePathPatterns("/api/developer/profile/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Refresh-Token")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadBaseDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location.endsWith("/") ? location : location + "/");
    }
}
