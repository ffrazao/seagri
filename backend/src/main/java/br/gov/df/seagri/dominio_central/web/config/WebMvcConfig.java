package br.gov.df.seagri.dominio_central.web.config;

import br.gov.df.seagri.dominio_central.web.config.CorsProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AutorizacaoTenantInterceptor autorizacaoTenantInterceptor;

    private final CorsProperties corsProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autorizacaoTenantInterceptor)
                .addPathPatterns("/api/v1/orgs/*/**")
                .excludePathPatterns("/api/v1/orgs");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Log estruturado mostrando os valores
        if (corsProperties.isDebug()) {
            log.debug("=== Configurando CORS ===");
            log.debug("allowedOrigins: {}", corsProperties.getAllowedOrigins());
            log.debug("allowedMethods: {}", corsProperties.getAllowedMethods());
            log.debug("allowedHeaders: {}", corsProperties.getAllowedHeaders());
            log.debug("allowCredentials: {}", corsProperties.isAllowCredentials());
        }

        registry.addMapping("/**") // Aplica a todas as rotas da API
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(corsProperties.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(corsProperties.getAllowedHeaders())
                .allowCredentials(corsProperties.isAllowCredentials());
    }

}