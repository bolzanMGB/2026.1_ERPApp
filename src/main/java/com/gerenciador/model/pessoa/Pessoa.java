package com.gerenciador.model.pessoa;

public class Pessoa {
    private String nome;
    private String telefone;
    private String telefone2;
    private String observacao;
    private Endereco endereco;

    public Pessoa(String nome, String telefone, String telefone2, String observacao, Endereco endereco) {
        this.nome = validarNome(nome);
        this.telefone = validarTelefone(telefone);
        this.telefone2 = validarTelefoneOpcional(telefone2);
        this.observacao = observacao;
        this.endereco = validarEndereco(endereco);
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome ou Nome Fantasia inválido");
        }
        return nome;
    }

    private static String validarTelefone(String valor) {
        if (valor == null || !valor.matches("\\(\\d{2}\\)\\d{4,5}-\\d{4}")) {
            throw new IllegalArgumentException("Telefone é inválido");
        }
        return valor;
    }

    private static String validarTelefoneOpcional(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "";
        }
        if (!valor.matches("\\(\\d{2}\\)\\d{4,5}-\\d{4}")) {
            throw new IllegalArgumentException("Telefone 2 é inválido");
        }
        return valor;
    }

    private static Endereco validarEndereco(Endereco endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException("Endereço é inválido");
        }
        return endereco;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getTelefone2() { return telefone2; }
    public void setTelefone2(String telefone2) { this.telefone2 = telefone2; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    @Override
    public String toString() {
        return "Pessoa{" +
                "nome='" + nome + '\'' +
                ", telefone='" + telefone + '\'' +
                ", telefone2='" + telefone2 + '\'' +
                ", observacao='" + observacao + '\'' +
                ", endereco=" + endereco +
                '}';
    }
}