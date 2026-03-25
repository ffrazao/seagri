package br.gov.df.seagri.modulo_organizacao.web.dto;

import br.gov.df.seagri.dominio_central.web.BaseMapper;
import br.gov.df.seagri.modulo_organizacao.dominio.Organizacao;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

// @Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Mapper(componentModel = "spring")
public interface OrganizacaoMapper extends BaseMapper<Organizacao, OrganizacaoRequestDTO, OrganizacaoResponseDTO> {
}