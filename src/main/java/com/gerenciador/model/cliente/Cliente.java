package com.gerenciador.model.cliente;

import com.gerenciador.model.pessoa.Endereco;

public interface Cliente {
    Integer getId();
    void setId(Integer id);
    String getNomePrincipal();
    String getDocumento();
    String getTelefone();
    String getTelefone2();
    String getObservacao();
    String getCidade();
    Endereco getEndereco();
}