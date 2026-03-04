package br.gov.df.seagri.modulo_organizacao.infraestrutura;

import br.gov.df.seagri.dominio_central.infraestrutura.BaseDAO;
import br.gov.df.seagri.modulo_organizacao.dominio.VinculoUsuario;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VinculoUsuarioDAO extends BaseDAO<VinculoUsuario, UUID> {

    // Valida se o usuário pertence ao Tenant (Usado no PDP / Autorização)
    Optional<VinculoUsuario> findByUsuarioIdAndOrganizacaoId(String usuarioId, UUID organizacaoId);

    // Lista todos os usuários de um determinado órgão
    List<VinculoUsuario> findByOrganizacaoId(UUID organizacaoId);
    
    // Lista todas as organizações nas quais um usuário possui vínculo
    List<VinculoUsuario> findByUsuarioId(String usuarioId);
}