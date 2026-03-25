package br.gov.df.seagri.modulo_presenca.infraestrutura;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

@Component
public class BiometriaClient {

    // Instanciamos o cliente HTTP do Spring
    private final RestTemplate restTemplate = new RestTemplate();
    
    // A URL interna do nosso container no Docker
    private final String BIOMETRIA_URL = "http://localhost:8000/api/v1/biometria/verify";

    public Map<String, Object> verificarFace(String fotoCadastroBase64, String fotoCapturadaBase64) {
        try {
            // Monta o payload exatamente como o nosso Pydantic do FastAPI espera
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("image_base64_1", fotoCadastroBase64);
            requestBody.put("image_base64_2", fotoCapturadaBase64);

            // Dispara a requisição POST para a IA em Python
            ResponseEntity<Map> response = restTemplate.postForEntity(BIOMETRIA_URL, requestBody, Map.class);
            return response.getBody();
            
        } catch (Exception e) {
            // Em caso de falha no motor Python (timeout, container offline, etc),
            // garantimos que o sistema não quebre, mas force um score 0.0 (suspeito)
            System.err.println("[BIOMETRIA] Falha ao comunicar com o motor Python: " + e.getMessage());
            
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("is_match", false);
            fallback.put("biometric_score", 0.0);
            return fallback;
        }
    }
}