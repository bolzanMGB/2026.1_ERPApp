package com.gerenciador.model.pessoa;

public class PessoaJuridica extends Pessoa {
    private String nomeResponsavel;
    private String cnpj;
    private String inscricaoEstadual;
    private String razaoSocial;

    public PessoaJuridica(String nome,
                          String telefone,
                          Endereco endereco,
                          String cnpj,
                          String nomeResponsavel,
                          String inscricaoEstadual,
                          String razaoSocial) {

        super (nome, telefone, endereco);
        this.nomeResponsavel = validarString (nomeResponsavel, "Nome Responsável");
        this.cnpj = validarCNPJ(cnpj);
        this.inscricaoEstadual = validarString (inscricaoEstadual, "Inscrição Estadual");
        this.razaoSocial = validarString (razaoSocial, "Razão Social");
    }

    private static String validarString (String valor, String nomeAtributo){
        if (valor == null || valor.isBlank()){
            throw new IllegalArgumentException(nomeAtributo + " é inválido");
        }
        return valor;
    }

    private static String validarCNPJ (String cnpj){
        if (cnpj == null || !cnpj.matches("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}")){
            throw new IllegalArgumentException("CNPJ inválido");
        }
        return cnpj;
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public void setInscricaoEstadual(String inscricaoEstadual) {
        this.inscricaoEstadual = inscricaoEstadual;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }
}
