package com.gerenciador.model.pessoa;

public class PessoaFisica extends Pessoa {
    private String cpf;

    public PessoaFisica(String nome,
                        String cpf,
                        String telefone,
                        Endereco endereco){

        super (nome, telefone, endereco);
        this.cpf = validarCPF(cpf);
    }

    private static String validarCPF (String cpf){
        if (cpf == null || !cpf.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")){
            throw new IllegalArgumentException("CPF inválido");
        }
        return cpf;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    @Override
    public String toString() {
        return "PessoaFisica{" +
                "cpf='" + cpf + '\'' +
                ", " + super.toString() +
                '}';
    }
}
