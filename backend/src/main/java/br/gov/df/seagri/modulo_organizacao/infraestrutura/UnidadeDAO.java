package br.gov.df.seagri.modulo_organizacao.infraestrutura;

import br.gov.df.seagri.dominio_central.infraestrutura.BaseDAO;
import br.gov.df.seagri.modulo_organizacao.dominio.Unidade;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UnidadeDAO extends BaseDAO<Unidade, UUID> {

    // Garante o isolamento Multi-Tenant na busca
    List<Unidade> findByOrganizacaoId(UUID organizacaoId);
}