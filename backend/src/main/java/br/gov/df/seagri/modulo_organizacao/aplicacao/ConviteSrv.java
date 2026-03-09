package br.gov.df.seagri.modulo_organizacao.aplicacao;

import br.gov.df.seagri.modulo_organizacao.dominio.Convite;
import br.gov.df.seagri.modulo_organizacao.dominio.VinculoUsuario;
import br.gov.df.seagri.modulo_organizacao.infraestrutura.ConviteDAO;
import br.gov.df.seagri.modulo_organizacao.infraestrutura.VinculoUsuarioDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConviteSrv {

    private final ConviteDAO conviteDAO;
    private final VinculoUsuarioDAO vinculoUsuarioDAO;

    public ConviteSrv(ConviteDAO conviteDAO, VinculoUsuarioDAO vinculoUsuarioDAO) {
        this.conviteDAO = conviteDAO;
        this.vinculoUsuarioDAO = vinculoUsuarioDAO;
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

        // 3. Verifica se o usuário já possui vínculo com esta organização
        boolean jaVinculado = vinculoUsuarioDAO.existsByOrganizacaoIdAndUsuarioId(
                convite.getOrganizacao().getId(), usuarioId);
        
        if (jaVinculado) {
            throw new IllegalArgumentException("Você já faz parte desta organização.");
        }

        // 4. Cria o vínculo oficial (Corrigido para 4 parâmetros: Organizacao, usuarioId, papel, criadoPor)
        VinculoUsuario novoVinculo = new VinculoUsuario(
                convite.getOrganizacao(), 
                usuarioId, 
                "PARTICIPANTE",
                usuarioId // O próprio usuário que aceitou o convite assina a autoria da vinculação
        );
        vinculoUsuarioDAO.save(novoVinculo);

        // 5. Marca o convite como usado
        convite.setUsado(true);
        conviteDAO.save(convite);
    }
}