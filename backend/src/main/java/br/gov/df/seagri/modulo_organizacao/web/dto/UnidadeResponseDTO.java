package br.gov.df.seagri.modulo_organizacao.web.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UnidadeResponseDTO {
    private UUID id;
    private UUID organizacaoId;
    private String nome;
    private String tipoGeometria;
    private LocalDateTime criadoEm;
    
    // Novos campos adicionados
    private Double centroGeoLat;
    private Double centroGeoLng;
    private Integer raioGeoMetros;
}