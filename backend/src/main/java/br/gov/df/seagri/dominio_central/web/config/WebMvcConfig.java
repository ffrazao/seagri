package br.gov.df.seagri.dominio_central.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AutorizacaoTenantInterceptor autorizacaoTenantInterceptor;

    public WebMvcConfig(AutorizacaoTenantInterceptor autorizacaoTenantInterceptor) {
        this.autorizacaoTenantInterceptor = autorizacaoTenantInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Aplica o filtro exclusivamente para as rotas filhas de uma organização
        registry.addInterceptor(autorizacaoTenantInterceptor)
                .addPathPatterns("/api/v1/orgs/*/**")
                // Excluímos explicitamente rotas globais ou soltas para evitar bloqueios acidentais
                .excludePathPatterns("/api/v1/orgs"); 
    }
}