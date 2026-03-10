package com.gerenciador.model.pessoa;

public class Endereco {
    private final String cidade;
    private final String bairro;
    private final String rua;
    private final String numeroCasa;

    public Endereco (String cidade, String bairro, String rua, String numeroCasa){
        this.cidade = validarStringF(cidade, "Cidade");
        this.bairro = validarStringM(bairro, "Bairro");
        this.rua = validarStringF(rua, "Rua");
        this.numeroCasa = validarStringM(numeroCasa, "Número");
    }

    private static String validarStringM (String valor, String nomeAtributo){
        if (valor == null || valor.isBlank()){
            throw new IllegalArgumentException (nomeAtributo + " inválido");
        }
        return valor;
    }

    private static String validarStringF (String valor, String nomeAtributo){
        if (valor == null || valor.isBlank()){
            throw new IllegalArgumentException (nomeAtributo + " inválida");
        }
        return valor;
    }

    public String getCidade() {
        return cidade;
    }

    public String getBairro() {
        return bairro;
    }

    public String getRua() {
        return rua;
    }

    public String getnumeroCasa() {
        return numeroCasa;
    }


    @Override
    public String toString() {
        return "Endereco{" +
                "cidade='" + cidade + '\'' +
                ", bairro='" + bairro + '\'' +
                ", rua='" + rua + '\'' +
                ", numeroCasa='" + numeroCasa + '\'' +
                '}';
    }
}
