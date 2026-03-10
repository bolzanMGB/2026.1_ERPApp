package com.gerenciador.model.pessoa;

public class Pessoa {
    private String nome;
    private String telefone;
    private Endereco endereco;

    public Pessoa (String nome,
                   String telefone,
                   Endereco endereco){


        this.telefone = validarTelefone (telefone);
        this.nome = validarNome (nome);
        this.endereco = validarEndereco (endereco);
    }

    private static String validarNome (String nome){
        if (nome == null || nome.isBlank()){
            throw new IllegalArgumentException("Nome ou Nome Fantasia inválido");
        }
        return nome;
    }

    private static String validarTelefone (String valor){
        if (valor == null || !valor.matches("\\(\\d{2}\\)\\d{5}-\\d{4}") ){
            throw new IllegalArgumentException("Telefone é inválido");
        }
        return valor;
    }

    private static Endereco validarEndereco (Endereco endereco){
        if (endereco == null){
            throw new IllegalArgumentException("Endereço é inválido");
        }
        return endereco;
    }


    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    @Override
    public String toString() {
        return "Pessoa{" +
                "nome='" + nome + '\'' +
                ", telefone='" + telefone + '\'' +
                ", endereco=" + endereco +
                '}';
    }
}
