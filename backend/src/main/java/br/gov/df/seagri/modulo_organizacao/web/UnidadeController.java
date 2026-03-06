package br.gov.df.seagri.modulo_organizacao.web;

import br.gov.df.seagri.dominio_central.web.AbstractCrudTenantController;
import br.gov.df.seagri.modulo_organizacao.aplicacao.UnidadeSrv;

import br.gov.df.seagri.modulo_organizacao.dominio.Unidade;
import br.gov.df.seagri.modulo_organizacao.web.dto.UnidadeMapper;
import br.gov.df.seagri.modulo_organizacao.web.dto.UnidadeRequestDTO;
import br.gov.df.seagri.modulo_organizacao.web.dto.UnidadeResponseDTO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{organizacaoId}/unidades")
public class UnidadeController extends AbstractCrudTenantController<Unidade, UnidadeRequestDTO, UnidadeResponseDTO, UUID> {

    public UnidadeController(UnidadeSrv unidadeSrv, UnidadeMapper unidadeMapper) {
        super(unidadeSrv, unidadeMapper);
    }

    @Override
    protected void vincularContexto(Unidade unidade, UUID organizacaoId) {
        // Removemos a criação da Organizacao falsa. 
        // Apenas injetamos a auditoria se for um novo registro.
        if (unidade.getId() == null) {
            unidade.setCriadoPor(obterUsuarioAutenticado());
            unidade.setCriadoEm(LocalDateTime.now(ZoneOffset.UTC));
        }
    }
}