package com.gerenciador.model.comercializavel;

import java.math.BigDecimal;

public class Servico implements Comercializavel {
    private String nome;
    private static int quantidadeProdutos = 0;
    private  BigDecimal totalVendido;
    private Integer id;

    public Servico(String nome) {
        this.nome = validarNome (nome);
        this.totalVendido = BigDecimal.ZERO;
        quantidadeProdutos++;
        this.id = quantidadeProdutos;
    }

    private static String validarNome (String valor){
        if (valor == null || valor.isBlank()){
            throw new IllegalArgumentException ("Nome é inválido");
        }
        return valor;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public String getTipo() {
        return "Serviço";
    }

    public BigDecimal getTotalVendido() {
        return totalVendido;
    }

    @Override
    public Integer getId() {
        return this.id;
    }



    public void setTotalVendido(BigDecimal totalVendido) {
        this.totalVendido = totalVendido;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
