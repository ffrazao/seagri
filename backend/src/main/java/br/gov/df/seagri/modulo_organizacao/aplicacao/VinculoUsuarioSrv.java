package br.gov.df.seagri.modulo_organizacao.aplicacao;

import br.gov.df.seagri.dominio_central.aplicacao.BaseCrudTenantSrv;
import br.gov.df.seagri.dominio_central.aplicacao.ValidadorTenant;
import br.gov.df.seagri.modulo_organizacao.dominio.Organizacao;
import br.gov.df.seagri.modulo_organizacao.dominio.VinculoUsuario;
import br.gov.df.seagri.modulo_organizacao.infraestrutura.VinculoUsuarioDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class VinculoUsuarioSrv extends BaseCrudTenantSrv<VinculoUsuario, UUID> {

    private final VinculoUsuarioDAO vinculoUsuarioDAO;
    private final OrganizacaoSrv organizacaoSrv;

    public VinculoUsuarioSrv(VinculoUsuarioDAO vinculoUsuarioDAO, ValidadorTenant validadorTenant, OrganizacaoSrv organizacaoSrv) {
        super(vinculoUsuarioDAO, validadorTenant);
        this.vinculoUsuarioDAO = vinculoUsuarioDAO;
        this.organizacaoSrv = organizacaoSrv;
    }

    @Transactional
    public VinculoUsuario criarVinculo(UUID organizacaoId, String usuarioId, String papel, String criadoPor) {
        vinculoUsuarioDAO.findByUsuarioIdAndOrganizacaoId(usuarioId, organizacaoId)
                .ifPresent(v -> { throw new IllegalStateException("O usuário já possui vínculo com esta organização."); });

        Organizacao organizacao = organizacaoSrv.buscarPorId(organizacaoId);
        VinculoUsuario novoVinculo = new VinculoUsuario(organizacao, usuarioId, papel, criadoPor);
        return super.salvar(organizacaoId, novoVinculo);
    }
}