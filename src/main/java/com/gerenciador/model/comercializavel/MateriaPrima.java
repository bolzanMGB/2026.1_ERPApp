package com.gerenciador.model.comercializavel;

import java.math.BigDecimal;

public class MateriaPrima implements Comercializavel {
    private String nome;

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    private String unidade;
    private BigDecimal estoqueAtual = BigDecimal.ZERO;
    private BigDecimal totalVendido = BigDecimal.ZERO;
    private BigDecimal totalComprado = BigDecimal.ZERO;

    private Integer id;
    private static int quantidadeProdutos = 0;

    public MateriaPrima(String nome, String unidade) {
        this.nome = validarString(nome, "Nome");
        this.unidade = validarString (unidade, "Unidade");
    }

    private static String validarString (String valor, String nomeAtributo){
        if (valor == null || valor.isBlank()){
            throw new IllegalArgumentException (nomeAtributo + " é inválido");
        }
        return valor;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    private static MateriaPrima validarProduto (MateriaPrima produto){
        if (produto == null){
            throw new IllegalArgumentException("MateriaPrima é inválido");
        }
        return produto;
    }

    public String getUnidade() {
        return unidade;
    }

    public BigDecimal getEstoqueAtual() {
        return estoqueAtual;
    }

    public BigDecimal getTotalComprado() {
        return totalComprado;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public String getTipo() {
        return "MateriaPrima";
    }

    @Override
    public BigDecimal getTotalVendido() {
        return this.totalVendido;
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    public void setEstoqueAtual(BigDecimal estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    public void setTotalVendido(BigDecimal totalVendido) {
        this.totalVendido = totalVendido;
    }

    public void setTotalComprado(BigDecimal totalComprado) {
        this.totalComprado = totalComprado;
    }

    public static int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    public static void setQuantidadeProdutos(int quantidadeProdutos) {
        MateriaPrima.quantidadeProdutos = quantidadeProdutos;
    }

    public void baixarEstoqueVenda(BigDecimal quantidade) {
        if (estoqueAtual.compareTo(quantidade) < 0){
            throw new IllegalStateException("Estoque insuficiente para essa venda. Disponível: " + estoqueAtual);
        }
        this.estoqueAtual = this.estoqueAtual.subtract(quantidade);
        this.totalVendido = this.totalVendido.add(quantidade);
    }

    public void processarCompra (MateriaPrima produto, BigDecimal quantidade){
        produto.setEstoqueAtual(produto.getEstoqueAtual().add(quantidade));
        produto.setTotalComprado(produto.getTotalComprado().add(quantidade));

    }

}
