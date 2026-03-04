package br.gov.df.seagri.modulo_presenca.infraestrutura;

import br.gov.df.seagri.dominio_central.infraestrutura.BaseDAO;
import br.gov.df.seagri.modulo_presenca.dominio.RegistroPresenca;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RegistroPresencaDAO extends BaseDAO<RegistroPresenca, UUID> {
}