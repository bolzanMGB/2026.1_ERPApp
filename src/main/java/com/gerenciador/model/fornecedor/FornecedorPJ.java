package com.gerenciador.model.fornecedor;

import com.gerenciador.model.pessoa.Endereco;
import com.gerenciador.model.pessoa.PessoaJuridica;

public class FornecedorPJ extends PessoaJuridica implements Fornecedor {
    private Integer id;

    public FornecedorPJ(String nome, String telefone, String telefone2, String observacao, Endereco endereco,
                        String cnpj, String nomeResponsavel, String inscricaoEstadual, String razaoSocial) {
        super(nome, telefone, telefone2, observacao, endereco, cnpj, nomeResponsavel, inscricaoEstadual, razaoSocial);
    }

    @Override public Integer getId() { return this.id; }
    @Override public void setId(Integer id) { this.id = id; }
    @Override public String getNomePrincipal() { return getNome(); }
    @Override public String getDocumento() { return getCnpj(); }
}