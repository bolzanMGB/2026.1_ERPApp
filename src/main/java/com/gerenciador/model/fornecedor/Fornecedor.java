package com.gerenciador.model.fornecedor;

import com.gerenciador.model.pessoa.Endereco;

public interface Fornecedor {
    Integer getId();
    void setId(Integer id);
    String getNomePrincipal();
    String getDocumento();
    String getTelefone();
    String getTelefone2();
    String getObservacao();
    Endereco getEndereco();
}