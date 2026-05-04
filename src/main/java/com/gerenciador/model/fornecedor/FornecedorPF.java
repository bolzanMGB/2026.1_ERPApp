package com.gerenciador.model.fornecedor;

import com.gerenciador.model.pessoa.Endereco;
import com.gerenciador.model.pessoa.PessoaFisica;

public class FornecedorPF extends PessoaFisica implements Fornecedor {
    private Integer id;

    public FornecedorPF(String nome, String cpf, String telefone, String telefone2, String observacao, Endereco endereco) {
        super(nome, cpf, telefone, telefone2, observacao, endereco);
    }

    @Override public Integer getId() { return this.id; }
    @Override public void setId(Integer id) { this.id = id; }
    @Override public String getNomePrincipal() { return getNome(); }
    @Override public String getDocumento() { return getCpf(); }
}