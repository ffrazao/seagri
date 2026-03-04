package br.gov.df.seagri.modulo_organizacao.aplicacao;

import br.gov.df.seagri.dominio_central.aplicacao.BaseCrudTenantSrv;
import br.gov.df.seagri.dominio_central.aplicacao.ValidadorTenant;
import br.gov.df.seagri.modulo_organizacao.dominio.Organizacao;
import br.gov.df.seagri.modulo_organizacao.dominio.Unidade;
import br.gov.df.seagri.modulo_organizacao.infraestrutura.UnidadeDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UnidadeSrv extends BaseCrudTenantSrv<Unidade, UUID> {

    private final UnidadeDAO unidadeDAO;
    private final OrganizacaoSrv organizacaoSrv;

    public UnidadeSrv(UnidadeDAO unidadeDAO, ValidadorTenant validadorTenant, OrganizacaoSrv organizacaoSrv) {
        super(unidadeDAO, validadorTenant);
        this.unidadeDAO = unidadeDAO;
        this.organizacaoSrv = organizacaoSrv;
    }

    @Transactional
    public Unidade criarUnidade(UUID organizacaoId, String nome, String tipoGeometria, String criadoPor) {
        Organizacao organizacao = organizacaoSrv.buscarPorId(organizacaoId);
        Unidade novaUnidade = new Unidade(organizacao, nome, tipoGeometria, criadoPor);
        return super.salvar(organizacaoId, novaUnidade);
    }
}