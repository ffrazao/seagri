package br.gov.df.seagri.modulo_organizacao.aplicacao;

import br.gov.df.seagri.modulo_organizacao.dominio.AlocacaoUnidade;
import br.gov.df.seagri.modulo_organizacao.dominio.Convite;
import br.gov.df.seagri.modulo_organizacao.dominio.VinculoUsuario;
import br.gov.df.seagri.modulo_organizacao.infraestrutura.AlocacaoUnidadeDAO;
import br.gov.df.seagri.modulo_organizacao.infraestrutura.ConviteDAO;
import br.gov.df.seagri.modulo_organizacao.infraestrutura.VinculoUsuarioDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConviteSrv {

    private final ConviteDAO conviteDAO;
    private final VinculoUsuarioDAO vinculoUsuarioDAO;
    private final AlocacaoUnidadeDAO alocacaoUnidadeDAO; // NOVO: Injetado para gerenciar a lotação

    public ConviteSrv(ConviteDAO conviteDAO, VinculoUsuarioDAO vinculoUsuarioDAO, AlocacaoUnidadeDAO alocacaoUnidadeDAO) {
        this.conviteDAO = conviteDAO;
        this.vinculoUsuarioDAO = vinculoUsuarioDAO;
        this.alocacaoUnidadeDAO = alocacaoUnidadeDAO;
    }

    @Transactional
    public void aceitarConvite(String codigo, String usuarioId) {
        // 1. Busca o convite pelo código
        Convite convite = conviteDAO.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Convite inválido ou não encontrado."));

        // 2. Valida expiração e se já foi usado
        if (!convite.isValido()) {
            throw new IllegalArgumentException("Este convite já foi utilizado ou está expirado.");
        }

        // 3. Gerencia o Vínculo Geral (Se não existir, cria. Se existir, reaproveita)
        VinculoUsuario vinculo;
        boolean jaVinculado = vinculoUsuarioDAO.existsByOrganizacaoIdAndUsuarioId(
                convite.getOrganizacao().getId(), usuarioId);
        
        if (!jaVinculado) {
            // Cria o vínculo oficial usando o papel definido dinamicamente no convite
            VinculoUsuario novoVinculo = new VinculoUsuario(
                    convite.getOrganizacao(), 
                    usuarioId, 
                    convite.getPapelEsperado(),
                    usuarioId 
            );
            vinculo = vinculoUsuarioDAO.save(novoVinculo);
        } else {
            // Como ele já tem vínculo geral com a SEAGRI, buscamos para associar à nova alocação de unidade
            vinculo = vinculoUsuarioDAO.findByUsuarioId(usuarioId).stream()
                .filter(v -> v.obterOrganizacaoId().equals(convite.getOrganizacao().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Erro ao recuperar vínculo existente."));
        }

        // 4. NOVO: Cria a lotação na Unidade Específica amarrada a este convite (ReBAC - RFC-0010)
        AlocacaoUnidade novaAlocacao = new AlocacaoUnidade(
                vinculo,
                convite.getUnidade(),
                convite.getPapelEsperado(),
                usuarioId
        );
        alocacaoUnidadeDAO.save(novaAlocacao);

        // 5. Marca o convite como usado
        convite.setUsado(true);
        conviteDAO.save(convite);
    }
}