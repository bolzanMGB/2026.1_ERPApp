package com.gerenciador.model.transacao;

import com.gerenciador.model.comercializavel.MateriaPrima;
import com.gerenciador.model.fornecedor.Fornecedor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Compra extends Transacao {

    public static class ItemCompra {
        private MateriaPrima materiaPrima;
        private BigDecimal quantidade;
        private BigDecimal valorUnidade;
        private BigDecimal valorTotal;

        public ItemCompra(MateriaPrima materiaPrima, BigDecimal quantidade, BigDecimal valorUnidade) {
            this.materiaPrima = materiaPrima;
            this.quantidade = Transacao.validarDecimal(quantidade, "Quantidade do item");
            this.valorUnidade = Transacao.validarDecimal(valorUnidade, "Valor Unitário do item");
            this.valorTotal = this.quantidade.multiply(this.valorUnidade);
        }
        public MateriaPrima getMateriaPrima() { return materiaPrima; }
        public BigDecimal getQuantidade() { return quantidade; }
        public BigDecimal getValorUnidade() { return valorUnidade; }
        public BigDecimal getValorTotal() { return valorTotal; }
    }

    private Fornecedor fornecedor;
    private List<ItemCompra> itens;
    private Integer id;

    public Compra(LocalDate dataTransacao,
                  Fornecedor fornecedor,
                  List<ItemCompra> itens,
                  Boolean estaPago,
                  LocalDate dataLimite,
                  String observacao){

        this(dataTransacao, fornecedor, itens, estaPago, dataLimite, null, null, null, observacao);
    }

    public Compra(LocalDate dataTransacao,
                  Fornecedor fornecedor,
                  List<ItemCompra> itens,
                  Boolean estaPago,
                  LocalDate dataLimite,
                  LocalDate dataPagamento,
                  byte[] notaFiscal,
                  byte[] comprovante,
                  String observacao){

        super(dataTransacao, estaPago, dataLimite, dataPagamento, notaFiscal, comprovante, observacao);

        this.fornecedor = validarFornecedor(fornecedor);
        this.itens = itens;

        BigDecimal total = BigDecimal.ZERO;
        for (ItemCompra i : itens) {
            total = total.add(i.getValorTotal());
            i.getMateriaPrima().processarCompra(i.getMateriaPrima(), i.getQuantidade());
        }
        this.setValorTotal(total);
    }

    public void atualizarDados(LocalDate dataTransacao,
                               Fornecedor fornecedor,
                               List<ItemCompra> novosItens,
                               Boolean estaPago,
                               LocalDate dataLimite,
                               LocalDate dataPagamento,
                               byte[] notaFiscal,
                               byte[] comprovante,
                               String observacao) {

        // Estorno
        for (ItemCompra i : this.itens) {
            i.getMateriaPrima().processarCompra(i.getMateriaPrima(), i.getQuantidade().negate());
        }

        setDataTransacao(dataTransacao);
        setEstaPago(estaPago);
        setDataLimite(dataLimite);
        setDataPagamento(dataPagamento);
        setNotaFiscal(notaFiscal);
        setComprovante(comprovante);
        setObservacao(observacao);

        this.fornecedor = validarFornecedor(fornecedor);
        this.itens = novosItens;

        BigDecimal total = BigDecimal.ZERO;
        for (ItemCompra i : this.itens) {
            total = total.add(i.getValorTotal());
            i.getMateriaPrima().processarCompra(i.getMateriaPrima(), i.getQuantidade());
        }
        this.setValorTotal(total);
    }

    private static Fornecedor validarFornecedor(Fornecedor fornecedor){
        if (fornecedor == null) throw new IllegalArgumentException("Fornecedor é inválido");
        return fornecedor;
    }

    public void setId(Integer id) { this.id = id; }
    public Integer getId() { return id; }
    public Fornecedor getFornecedor() { return fornecedor; }
    public List<ItemCompra> getItens() { return itens; }
}