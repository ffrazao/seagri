package br.gov.df.seagri.modulo_presenca.web.dto;

import br.gov.df.seagri.dominio_central.web.BaseMapper;
import br.gov.df.seagri.modulo_presenca.dominio.RegistroPresenca;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper automatizado via MapStruct para a entidade RegistroPresenca.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegistroPresencaMapper extends BaseMapper<RegistroPresenca, RegistroPresencaRequestDTO, RegistroPresencaResponseDTO> {
    
    // O MapStruct vai gerar automaticamente o código do método "paraDto" 
    // lendo os @Getters do RegistroPresenca e setando no RegistroPresencaResponseDTO.
    
    // O método "paraEntidade(RegistroPresencaRequestDTO)" que vem de herança do BaseMapper
    // não será efetivamente utilizado para a criação neste caso específico, 
    // porque usamos o construtor cego e imutável exigido pela RFC-008 diretamente no Serviço.
}