package br.gov.df.seagri.dominio_central.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@MappedSuperclass
@Getter
@Setter
public abstract class EntidadeBaseHierarquica<T extends EntidadeBaseHierarquica<T>> extends EntidadeBase implements Hierarquica<T> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pai_id")
    private T pai;

    // mappedBy aponta para a propriedade 'pai' declarada logo acima
    @OneToMany(mappedBy = "pai", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<T> filhos = new ArrayList<>();

    // --- Métodos Utilitários (Boas práticas do JPA) ---

    @SuppressWarnings("unchecked")
    public void adicionarFilho(T filho) {
        this.filhos.add(filho);
        filho.setPai((T) this);
    }

    public void removerFilho(T filho) {
        this.filhos.remove(filho);
        filho.setPai(null);
    }
}