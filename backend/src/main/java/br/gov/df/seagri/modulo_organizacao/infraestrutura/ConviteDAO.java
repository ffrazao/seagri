package br.gov.df.seagri.modulo_organizacao.infraestrutura;

import br.gov.df.seagri.modulo_organizacao.dominio.Convite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConviteDAO extends JpaRepository<Convite, UUID> {
    
    // O Spring Data JPA cria a query automaticamente baseada no nome do método
    Optional<Convite> findByCodigo(String codigo);
    
}