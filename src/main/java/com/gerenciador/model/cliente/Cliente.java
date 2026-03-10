package com.gerenciador.model.cliente;

import com.gerenciador.model.pessoa.Endereco;

public interface Cliente {
    Integer getId();   String getNomePrincipal();
    String getDocumento();
    String getCidade();
    String getTelefone();
    Endereco getEndereco();
    void setId(Integer id);
}
