package com.gerenciador.model.fornecedor;

import com.gerenciador.model.pessoa.Endereco;

public interface Fornecedor {
    Integer getId();
    String getNomePrincipal();
    String getDocumento();
    Endereco getEndereco();
    String getTelefone();
    void setId(Integer id);
}
