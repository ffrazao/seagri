package br.gov.df.seagri.dominio_central.dominio;

import java.util.Collection;

public interface Hierarquica<T> {
    T getPai();
    void setPai(T pai);
    Collection<T> getFilhos();
}