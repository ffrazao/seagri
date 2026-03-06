package br.gov.df.seagri.modulo_organizacao.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnidadeRequestDTO {

    @NotBlank(message = "O nome da unidade é obrigatório")
    private String nome;

    @NotBlank(message = "O tipo de geometria é obrigatório (ex: RADIUS, POLYGON)")
    private String tipoGeometria;

    // Campos opcionais dependendo do tipo da geometria
    private Double geoCenterLat;
    private Double geoCenterLng;
    private Integer geoRadiusMeters;
}