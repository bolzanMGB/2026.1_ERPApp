package com.gerenciador.model.cliente;

import com.gerenciador.model.pessoa.Endereco;
import com.gerenciador.model.pessoa.PessoaFisica;

public class ClientePF extends PessoaFisica implements Cliente {
    private Integer id;

    public ClientePF(String nome, String cpf, String telefone, String telefone2, String observacao, Endereco endereco) {
        super(nome, cpf, telefone, telefone2, observacao, endereco);
    }

    @Override public Integer getId() { return id; }
    @Override public void setId(Integer id) { this.id = id; }
    @Override public String getNomePrincipal() { return getNome(); }
    @Override public String getDocumento() { return getCpf(); }
    @Override public String getCidade() { return getEndereco().getCidade(); }
}