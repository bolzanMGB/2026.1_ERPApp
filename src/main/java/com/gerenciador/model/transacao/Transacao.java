package com.gerenciador.model.transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

public class Transacao {
    private LocalDate dataTransacao;
    private BigDecimal quantidade;
    private BigDecimal valorUnidade;
    private BigDecimal valorTotal;
    private Boolean estaPago;
    private LocalDate dataLimite;
    private LocalDate dataPagamento;
    private byte[] notaFiscal;
    private String observacao;
    private String nomeArquivoPdf;

    public Transacao (LocalDate dataTransacao,
                      BigDecimal quantidade,
                      BigDecimal valorUnidade,
                      Boolean estaPago,
                      LocalDate dataLimite,
                      LocalDate dataPagamento,
                      byte[] notaFiscal,
                      String observacao) {

        this.dataTransacao = validarData(dataTransacao, "Data da Transação");
        this.quantidade = validarDecimal(quantidade, "Quantidade");
        this.valorUnidade = validarDecimal(valorUnidade, "Valor por Unidade");
        this.valorTotal = this.valorUnidade.multiply(this.quantidade);
        this.estaPago = validarPago(estaPago);
        this.dataLimite = validarData(dataLimite, "Data Limite");
        this.observacao = observacao;

        if (this.estaPago) {
            this.dataPagamento = validarData(dataPagamento, "Data de Pagamento");
            this.notaFiscal = validarPDF(notaFiscal);
        } else {
            this.dataPagamento = null;
            this.notaFiscal = null;
        }
    }

    private static LocalDate validarData (LocalDate valor, String nomeAtributo){
        if (valor == null){
            throw new IllegalArgumentException(nomeAtributo + " é inválida");
        }
        return valor;
    }

    private static BigDecimal validarDecimal (BigDecimal valor, String nomeAtributo){
        if (valor == null || valor.compareTo (BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException(nomeAtributo + " é inválido");
        }
        return valor;
    }

    private static Boolean validarPago (Boolean valor){
        if (valor == null){
            throw new IllegalArgumentException("Condição de pagamento é inválida");
        }
        return valor;
    }

    private static byte[] validarPDF (byte[] arquivo){
        if (arquivo == null || arquivo.length == 0){
            throw new IllegalArgumentException("Arquivo da NF é inválido");
        }
        return arquivo;
    }

    public LocalDate getDataTransacao() {
        return dataTransacao;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getValorUnidade() {
        return valorUnidade;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public Boolean getEstaPago() {
        return estaPago;
    }

    public LocalDate getDataLimite() {
        return dataLimite;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public byte[] getNotaFiscal() {
        return notaFiscal;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = validarDecimal(quantidade, "Quantidade");
        recalcularTotal();
    }

    public void setValorUnidade(BigDecimal valorUnidade) {
        this.valorUnidade = validarDecimal(valorUnidade, "Valor por Unidade");
        recalcularTotal();
    }

    private void recalcularTotal() {
        if (this.valorUnidade != null && this.quantidade != null) {
            this.valorTotal = this.valorUnidade.multiply(this.quantidade);
        }
    }

    public void setEstaPago(Boolean estaPago) {
        this.estaPago = validarPago(estaPago);
    }

    public void setDataTransacao(LocalDate data) { this.dataTransacao = data; }
    public void setDataLimite(LocalDate data) { this.dataLimite = data; }
    public void setDataPagamento(LocalDate data) { this.dataPagamento = data; }
    public void setNotaFiscal(byte[] notaFiscal) { this.notaFiscal = notaFiscal; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public String getNomeArquivoPdf() {
        return nomeArquivoPdf;
    }

    public void setNomeArquivoPdf(String nomeArquivoPdf) {
        this.nomeArquivoPdf = nomeArquivoPdf;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
}
