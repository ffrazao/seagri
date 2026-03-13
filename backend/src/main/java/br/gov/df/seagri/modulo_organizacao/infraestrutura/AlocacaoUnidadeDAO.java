package br.gov.df.seagri.modulo_organizacao.infraestrutura;

import br.gov.df.seagri.modulo_organizacao.dominio.AlocacaoUnidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AlocacaoUnidadeDAO extends JpaRepository<AlocacaoUnidade, UUID> {

    // A query agora recebe a data/hora exata calculada pelo Java
    @Query("SELECT a FROM AlocacaoUnidade a JOIN FETCH a.unidade WHERE a.vinculoUsuario.id = :vinculoId AND a.status = 'ATIVO' AND a.dataInicio <= :agora AND (a.dataFim IS NULL OR a.dataFim >= :agora)")
    List<AlocacaoUnidade> buscarAlocacoesVigentesPorVinculo(@Param("vinculoId") UUID vinculoId, @Param("agora") LocalDateTime agora);
    
}