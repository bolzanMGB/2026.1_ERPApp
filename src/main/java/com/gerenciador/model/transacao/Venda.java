package com.gerenciador.model.transacao;

import com.gerenciador.model.cliente.Cliente;
import com.gerenciador.model.comercializavel.Comercializavel;
import com.gerenciador.model.comercializavel.MateriaPrima;
import com.gerenciador.model.comercializavel.Produto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Venda extends Transacao {
    private Cliente cliente;
    private Comercializavel item;
    private ComposicaoVenda composicao;
    private Integer id;
    public Cliente getCliente() {
        return cliente;
    }

    public Comercializavel getItem() {
        return item;
    }

    public ComposicaoVenda getComposicao() {
        return composicao;
    }
    public Venda(LocalDate dataTransacao, Cliente cliente, Produto item,
                 BigDecimal quantidade, BigDecimal valorUnidade, ComposicaoVenda composicao,
                 Boolean estaPago, LocalDate dataLimite, String observacao) {
        this(dataTransacao, cliente, item, quantidade, valorUnidade, composicao,
                estaPago, dataLimite, null, null, observacao);
    }

    public Venda(LocalDate dataTransacao, Cliente cliente, Produto item,
                 BigDecimal quantidade, BigDecimal valorUnidade, ComposicaoVenda composicao,
                 Boolean estaPago, LocalDate dataLimite, LocalDate dataPagamento,
                 byte[] notaFiscal, String observacao) {
        this(dataTransacao, cliente, (Comercializavel) item, quantidade, valorUnidade, composicao,
                estaPago, dataLimite, dataPagamento, notaFiscal, observacao);
    }

    public Venda(LocalDate dataTransacao, Cliente cliente, Comercializavel item,
                 BigDecimal quantidade, BigDecimal valorUnidade,
                 Boolean estaPago, LocalDate dataLimite, String observacao) {
        this(dataTransacao, cliente, item, quantidade, valorUnidade, null,
                estaPago, dataLimite, null, null, observacao);
    }

    public Venda(LocalDate dataTransacao, Cliente cliente, Comercializavel item,
                 BigDecimal quantidade, BigDecimal valorUnidade,
                 Boolean estaPago, LocalDate dataLimite, LocalDate dataPagamento,
                 byte[] notaFiscal, String observacao) {
        this(dataTransacao, cliente, item, quantidade, valorUnidade, null,
                estaPago, dataLimite, dataPagamento, notaFiscal, observacao);
    }

    private Venda(LocalDate dataTransacao, Cliente cliente, Comercializavel item,
                  BigDecimal quantidade, BigDecimal valorUnidade, ComposicaoVenda composicao,
                  Boolean estaPago, LocalDate dataLimite, LocalDate dataPagamento,
                  byte[] notaFiscal, String observacao) {

        super(dataTransacao, quantidade, valorUnidade, estaPago, dataLimite,
                dataPagamento, notaFiscal, observacao);

        this.cliente = validarCliente(cliente);
        this.item = validarItem(item);
        this.composicao = composicao;

        processarConsumoEstoque(quantidade, composicao);
    }

    private void processarConsumoEstoque(BigDecimal quantidadeVenda, ComposicaoVenda composicao) {
        if (item instanceof MateriaPrima mp) {

            mp.baixarEstoqueVenda(quantidadeVenda);
        }
        else if (item instanceof Produto produto) {
            if (composicao == null) {
                throw new IllegalArgumentException("Venda de produto exige uma composição de matérias-primas.");
            }

            validarTiposComposicao(produto, composicao);

            composicao.validarEstoqueDisponivel();

            composicao.baixarEstoque();
        }
    }

    private void validarTiposComposicao(Produto p, ComposicaoVenda c) {
        var materiasExigidas = p.getMateriasPrimasNecessarias();
        var materiasInformadas = c.ingredientes().keySet();

        if (materiasExigidas.size() != materiasInformadas.size() ||
                !materiasInformadas.containsAll(materiasExigidas)) {

            throw new IllegalArgumentException(
                    "A composição informada não condiz com as matérias-primas do produto: " + p.getNome() +
                            ". Esperado: " + materiasExigidas.stream().map(MateriaPrima::getNome).toList()
            );
        }
    }

    private static Cliente validarCliente(Cliente c) {
        if (c == null) throw new IllegalArgumentException("Cliente inválido");
        return c;
    }
    public Integer getId() {
        return id;
    }
    private static Comercializavel validarItem(Comercializavel i) {
        if (i == null) throw new IllegalArgumentException("Item inválido");
        return i;
    }

    public void atualizarDados(LocalDate dtVenda, Cliente cliente, Comercializavel item, BigDecimal qtd, BigDecimal valUnid, ComposicaoVenda composicao, boolean pago, LocalDate dtLimite, LocalDate dtPgto, byte[] pdf, String obs) {

        estornarImpactoEstoque();

        setDataTransacao(dtVenda);
        setQuantidade(qtd);
        setValorUnidade(valUnid);
        setEstaPago(pago);
        setDataLimite(dtLimite);
        setDataPagamento(dtPgto);
        setNotaFiscal(pdf);
        setObservacao(obs);

        this.cliente = cliente;
        this.item = item;
        this.composicao = composicao;

        aplicarImpactoEstoque();
    }

    private void estornarImpactoEstoque() {
        if (this.item instanceof MateriaPrima mp) {
            mp.setEstoqueAtual(mp.getEstoqueAtual().add(getQuantidade()));
            mp.setTotalVendido(mp.getTotalVendido().subtract(getQuantidade()));
        } else if (this.item instanceof Produto && this.composicao != null) {
            this.composicao.ingredientes().forEach((mp, qtdNecessaria) -> {
                mp.setEstoqueAtual(mp.getEstoqueAtual().add(qtdNecessaria));
                mp.setTotalVendido(mp.getTotalVendido().subtract(qtdNecessaria));
            });
        }
    }

    private void aplicarImpactoEstoque() {
        if (this.item instanceof MateriaPrima mp) {
            mp.baixarEstoqueVenda(getQuantidade());
        } else if (this.item instanceof Produto && this.composicao != null) {
            this.composicao.validarEstoqueDisponivel();
            this.composicao.baixarEstoque();
        }
    }

    public void setId(Integer id) {
        this.id = id;
    }


}