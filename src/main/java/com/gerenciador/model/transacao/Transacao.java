package com.gerenciador.model.transacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class Transacao {
    private LocalDate dataTransacao;
    private BigDecimal valorTotal;
    private Boolean estaPago;
    private LocalDate dataLimite;
    private LocalDate dataPagamento;

    private byte[] notaFiscal;
    private String nomeNotaFiscal;
    private byte[] comprovante;
    private String nomeComprovante;
    private String observacao;

    public Transacao(LocalDate dataTransacao,
                     Boolean estaPago,
                     LocalDate dataLimite,
                     LocalDate dataPagamento,
                     byte[] notaFiscal,
                     byte[] comprovante,
                     String observacao) {

        this.dataTransacao = validarData(dataTransacao, "Data da Transação");
        this.estaPago = validarPago(estaPago);
        this.dataLimite = validarData(dataLimite, "Data Limite");
        this.observacao = observacao;
        this.valorTotal = BigDecimal.ZERO; // Setado pela classe filha conforme a soma dos itens

        this.notaFiscal = notaFiscal;

        if (this.estaPago) {
            this.dataPagamento = validarData(dataPagamento, "Data de Pagamento");
            this.comprovante = validarArquivo(comprovante, "Arquivo do Comprovante");
        } else {
            this.dataPagamento = null;
            this.comprovante = null;
        }
    }

    private static LocalDate validarData(LocalDate valor, String nomeAtributo){
        if (valor == null){ throw new IllegalArgumentException(nomeAtributo + " é inválida"); }
        return valor;
    }

    protected static BigDecimal validarDecimal(BigDecimal valor, String nomeAtributo){
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0){ throw new IllegalArgumentException(nomeAtributo + " é inválido"); }
        return valor;
    }

    private static Boolean validarPago(Boolean valor){
        if (valor == null){ throw new IllegalArgumentException("Condição de pagamento é inválida"); }
        return valor;
    }

    private static byte[] validarArquivo(byte[] arquivo, String nomeAtributo){
        if (arquivo == null || arquivo.length == 0){ throw new IllegalArgumentException(nomeAtributo + " é obrigatório para itens pagos."); }
        return arquivo;
    }

    public LocalDate getDataTransacao() { return dataTransacao; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public Boolean getEstaPago() { return estaPago; }
    public LocalDate getDataLimite() { return dataLimite; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public String getObservacao() { return observacao; }

    public byte[] getNotaFiscal() { return notaFiscal; }
    public String getNomeNotaFiscal() { return nomeNotaFiscal; }
    public void setNomeNotaFiscal(String nome) { this.nomeNotaFiscal = nome; }
    public void setNotaFiscal(byte[] notaFiscal) { this.notaFiscal = notaFiscal; }

    public byte[] getComprovante() { return comprovante; }
    public String getNomeComprovante() { return nomeComprovante; }
    public void setNomeComprovante(String nome) { this.nomeComprovante = nome; }
    public void setComprovante(byte[] comprovante) { this.comprovante = comprovante; }

    public void setEstaPago(Boolean estaPago) { this.estaPago = validarPago(estaPago); }
    public void setDataTransacao(LocalDate data) { this.dataTransacao = data; }
    public void setDataLimite(LocalDate data) { this.dataLimite = data; }
    public void setDataPagamento(LocalDate data) { this.dataPagamento = data; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
}