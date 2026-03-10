package com.gerenciador.model.cliente;

import com.gerenciador.model.pessoa.Endereco;
import com.gerenciador.model.pessoa.PessoaFisica;

public class ClientePF extends PessoaFisica implements Cliente {
    private Integer id;
    public ClientePF(String nome,
                        String cpf,
                        String telefone,
                        Endereco endereco){

        super (nome, cpf, telefone, endereco);
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
        return getCpf();
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
