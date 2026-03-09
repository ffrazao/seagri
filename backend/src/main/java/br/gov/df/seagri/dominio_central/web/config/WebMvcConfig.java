package br.gov.df.seagri.dominio_central.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry; // Adicione este import
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
        registry.addInterceptor(autorizacaoTenantInterceptor)
                .addPathPatterns("/api/v1/orgs/*/**")
                .excludePathPatterns("/api/v1/orgs"); 
    }

    // ADICIONE ESTE BLOCO PARA RESOLVER O CORS
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todas as rotas da API
                .allowedOrigins("http://localhost:5173") // Autoriza a porta do React (Vite)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}