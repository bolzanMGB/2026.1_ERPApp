package com.gerenciador.model.cliente;

import com.gerenciador.model.pessoa.Endereco;
import com.gerenciador.model.pessoa.PessoaJuridica;

public class ClientePJ extends PessoaJuridica implements Cliente {
    private Integer id;
    public ClientePJ (String nome,
                      String telefone,
                      Endereco endereco,
                      String cnpj,
                      String nomeResponsavel,
                      String inscricaoEstadual,
                      String razaoSocial){

        super (nome, telefone, endereco, cnpj, nomeResponsavel, inscricaoEstadual, razaoSocial);
    }


    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getNomePrincipal() {
        return getNome();
    }

    @Override
    public String getDocumento() {
        return getCnpj();
    }

    @Override
    public String getCidade() {
        return getEndereco().getCidade();
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }
}
