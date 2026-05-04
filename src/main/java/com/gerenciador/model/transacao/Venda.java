package com.gerenciador.model.transacao;

import com.gerenciador.model.cliente.Cliente;
import com.gerenciador.model.comercializavel.Comercializavel;
import com.gerenciador.model.comercializavel.MateriaPrima;
import com.gerenciador.model.comercializavel.Produto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Venda extends Transacao {

    public static class ItemVenda {
        private Comercializavel item;
        private BigDecimal quantidade;
        private BigDecimal valorUnidade;
        private BigDecimal valorTotal;
        private ComposicaoVenda composicao;

        public ItemVenda(Comercializavel item, BigDecimal quantidade, BigDecimal valorUnidade, ComposicaoVenda composicao) {
            if (item == null) throw new IllegalArgumentException("Item inválido");
            this.item = item;
            this.quantidade = Transacao.validarDecimal(quantidade, "Quantidade do item");
            this.valorUnidade = Transacao.validarDecimal(valorUnidade, "Valor Unitário do item");
            this.valorTotal = this.quantidade.multiply(this.valorUnidade);
            this.composicao = composicao;
        }

        public Comercializavel getItem() { return item; }
        public BigDecimal getQuantidade() { return quantidade; }
        public BigDecimal getValorUnidade() { return valorUnidade; }
        public BigDecimal getValorTotal() { return valorTotal; }
        public ComposicaoVenda getComposicao() { return composicao; }
    }

    private Integer numeroVenda;
    private Cliente cliente;
    private List<ItemVenda> itens;
    private Integer id;

    private byte[] arquivoVenda;
    private String nomeArquivoVenda;

    public Venda(Integer numeroVenda, LocalDate dataTransacao, Cliente cliente, List<ItemVenda> itens,
                 Boolean estaPago, LocalDate dataLimite, String observacao) {
        this(numeroVenda, dataTransacao, cliente, itens, estaPago, dataLimite, null, null, null, null, observacao);
    }

    public Venda(Integer numeroVenda, LocalDate dataTransacao, Cliente cliente, List<ItemVenda> itens,
                 Boolean estaPago, LocalDate dataLimite, LocalDate dataPagamento,
                 byte[] notaFiscal, byte[] comprovante, byte[] arquivoVenda, String observacao) {

        super(dataTransacao, estaPago, dataLimite, dataPagamento, notaFiscal, comprovante, observacao);

        if (numeroVenda == null) throw new IllegalArgumentException("O Número da Venda é obrigatório.");
        this.numeroVenda = numeroVenda;
        this.cliente = validarCliente(cliente);
        this.itens = itens;
        this.arquivoVenda = arquivoVenda;

        BigDecimal total = BigDecimal.ZERO;
        for (ItemVenda iv : itens) {
            total = total.add(iv.getValorTotal());
            processarConsumoEstoque(iv);
        }
        this.setValorTotal(total);
    }

    public void atualizarDados(Integer numeroVenda, LocalDate dtVenda, Cliente cliente, List<ItemVenda> novosItens, boolean pago, LocalDate dtLimite, LocalDate dtPgto, byte[] notaFiscal, byte[] comprovante, byte[] arquivoVenda, String obs) {
        estornarImpactoEstoque();

        if (numeroVenda == null) throw new IllegalArgumentException("O Número da Venda é obrigatório.");
        this.numeroVenda = numeroVenda;

        setDataTransacao(dtVenda);
        setEstaPago(pago);
        setDataLimite(dtLimite);
        setDataPagamento(dtPgto);
        setNotaFiscal(notaFiscal);
        setComprovante(comprovante);
        setArquivoVenda(arquivoVenda);
        setObservacao(obs);

        this.cliente = validarCliente(cliente);
        this.itens = novosItens;

        BigDecimal total = BigDecimal.ZERO;
        for (ItemVenda iv : this.itens) {
            total = total.add(iv.getValorTotal());
            aplicarImpactoEstoque(iv);
        }
        this.setValorTotal(total);
    }

    private void processarConsumoEstoque(ItemVenda iv) {
        if (iv.getItem() instanceof MateriaPrima mp) {
            // A matéria-prima já gerencia sua própria baixa e total vendido internamente
            mp.baixarEstoqueVenda(iv.getQuantidade());
        } else {
            if (iv.getItem() instanceof Produto produto) {
                if (iv.getComposicao() == null) throw new IllegalArgumentException("Venda de produto exige uma composição.");
                validarTiposComposicao(produto, iv.getComposicao());
                iv.getComposicao().validarEstoqueDisponivel();
                iv.getComposicao().baixarEstoque();
            }

            // Incrementa o volume vendido apenas para Produtos e Serviços
            BigDecimal totalAtual = iv.getItem().getTotalVendido();
            iv.getItem().setTotalVendido(totalAtual.add(iv.getQuantidade()));
        }
    }

    private void validarTiposComposicao(Produto p, ComposicaoVenda c) {
        var materiasExigidas = p.getMateriasPrimasNecessarias();
        var materiasInformadas = c.ingredientes().keySet();
        if (materiasExigidas.size() != materiasInformadas.size() || !materiasInformadas.containsAll(materiasExigidas)) {
            throw new IllegalArgumentException("A composição não condiz com as matérias-primas do produto: " + p.getNome());
        }
    }

    private static Cliente validarCliente(Cliente c) {
        if (c == null) throw new IllegalArgumentException("Cliente inválido");
        return c;
    }


    private void aplicarImpactoEstoque(ItemVenda iv) {
        if (iv.getItem() instanceof MateriaPrima mp) {
            mp.baixarEstoqueVenda(iv.getQuantidade());
        } else {
            // Aplica o novo volume vendido apenas para Produtos e Serviços
            BigDecimal totalAtual = iv.getItem().getTotalVendido();
            iv.getItem().setTotalVendido(totalAtual.add(iv.getQuantidade()));

            if (iv.getItem() instanceof Produto && iv.getComposicao() != null) {
                iv.getComposicao().validarEstoqueDisponivel();
                iv.getComposicao().baixarEstoque();
            }
        }
    }

    private void estornarImpactoEstoque() {
        for (ItemVenda iv : this.itens) {
            if (iv.getItem() instanceof MateriaPrima mp) {
                // Estorno exclusivo para Matéria-Prima (executado apenas uma vez)
                mp.setEstoqueAtual(mp.getEstoqueAtual().add(iv.getQuantidade()));
                mp.setTotalVendido(mp.getTotalVendido().subtract(iv.getQuantidade()));
            } else {
                // Estorna o volume vendido para Produtos e Serviços
                BigDecimal totalAtual = iv.getItem().getTotalVendido();
                iv.getItem().setTotalVendido(totalAtual.subtract(iv.getQuantidade()));

                // Devolve os ingredientes ao estoque se for um Produto
                if (iv.getItem() instanceof Produto && iv.getComposicao() != null) {
                    iv.getComposicao().ingredientes().forEach((mpIngrediente, qtdNecessaria) -> {
                        mpIngrediente.setEstoqueAtual(mpIngrediente.getEstoqueAtual().add(qtdNecessaria));
                        mpIngrediente.setTotalVendido(mpIngrediente.getTotalVendido().subtract(qtdNecessaria));
                    });
                }
            }
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getNumeroVenda() { return numeroVenda; }
    public void setNumeroVenda(Integer numeroVenda) { this.numeroVenda = numeroVenda; }
    public Cliente getCliente() { return cliente; }
    public List<ItemVenda> getItens() { return itens; }

    public byte[] getArquivoVenda() { return arquivoVenda; }
    public void setArquivoVenda(byte[] arquivoVenda) { this.arquivoVenda = arquivoVenda; }
    public String getNomeArquivoVenda() { return nomeArquivoVenda; }
    public void setNomeArquivoVenda(String nomeArquivoVenda) { this.nomeArquivoVenda = nomeArquivoVenda; }
}