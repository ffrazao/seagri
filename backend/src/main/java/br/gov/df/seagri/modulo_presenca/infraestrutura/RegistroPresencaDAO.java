package br.gov.df.seagri.modulo_presenca.infraestrutura;

import br.gov.df.seagri.dominio_central.infraestrutura.BaseDAO;
import br.gov.df.seagri.modulo_presenca.dominio.RegistroPresenca;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RegistroPresencaDAO extends BaseDAO<RegistroPresenca, UUID> {

    // A anotação @Query instrui o Hibernate a fazer o SELECT ordenando do mais recente para o mais antigo [1]
    @Query("SELECT r FROM RegistroPresenca r WHERE r.usuarioId = :usuarioId ORDER BY r.capturadoEm DESC")
    List<RegistroPresenca> buscarPorUsuario(@Param("usuarioId") String usuarioId);

}