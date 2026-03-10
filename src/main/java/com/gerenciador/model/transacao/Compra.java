package com.gerenciador.model.transacao;
import com.gerenciador.model.comercializavel.MateriaPrima;
import com.gerenciador.model.fornecedor.Fornecedor;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Compra extends Transacao {
    private Fornecedor fornecedor;
    private MateriaPrima produto;
    private Integer id;

    public Compra (LocalDate dataTransacao,
                   Fornecedor fornecedor,
                   MateriaPrima produto,
                   BigDecimal quantidade,
                   BigDecimal valorUnidade,
                   Boolean estaPago,
                   LocalDate dataLimite,
                   String observacao){

        this (dataTransacao, fornecedor, produto, quantidade, valorUnidade,
                estaPago, dataLimite, null, null, observacao);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Compra (LocalDate dataTransacao,
                   Fornecedor fornecedor,
                   MateriaPrima produto,
                   BigDecimal quantidade,
                   BigDecimal valorUnidade,
                   Boolean estaPago,
                   LocalDate dataLimite,
                   LocalDate dataPagamento,
                   byte[] notaFiscal,
                   String observacao){

        super (dataTransacao,
                quantidade,
                valorUnidade,
                estaPago,
                dataLimite,
                dataPagamento,
                notaFiscal,
                observacao);

        this.fornecedor = validarFornecedor(fornecedor);
        this.produto = validarProduto (produto);
        produto.processarCompra (produto, quantidade);
        this.setValorTotal(quantidade.multiply(valorUnidade));
    }

    private static Fornecedor validarFornecedor(Fornecedor fornecedor){
        if (fornecedor == null){
            throw new IllegalArgumentException("Fornecedor é inválido");
        }
        return fornecedor;
    }

    private static MateriaPrima validarProduto(MateriaPrima item){
        if (item == null){
            throw new IllegalArgumentException("MateriaPrima é inválido");
        }
        return item;
    }
    public void atualizarDados(LocalDate dataTransacao,
                               Fornecedor fornecedor,
                               MateriaPrima produto,
                               BigDecimal quantidade,
                               BigDecimal valorUnidade,
                               Boolean estaPago,
                               LocalDate dataLimite,
                               LocalDate dataPagamento,
                               byte[] notaFiscal,
                               String observacao) {


        this.produto.processarCompra(this.produto, this.getQuantidade().negate());


        setDataTransacao(dataTransacao);
        setQuantidade(quantidade);
        setValorUnidade(valorUnidade);
        setEstaPago(estaPago);
        setDataLimite(dataLimite);
        setDataPagamento(dataPagamento);
        setNotaFiscal(notaFiscal);
        setObservacao(observacao);


        this.fornecedor = validarFornecedor(fornecedor);
        this.produto = validarProduto(produto);
        this.setValorTotal(quantidade.multiply(valorUnidade));


        this.produto.processarCompra(this.produto, quantidade);
    }
    public Integer getId() {
        return id;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public MateriaPrima getProduto() {
        return produto;
    }

}
