package com.gerenciador.model.transacao;

import com.gerenciador.model.comercializavel.MateriaPrima;
import java.math.BigDecimal;
import java.util.Map;

public record ComposicaoVenda(Map<MateriaPrima, BigDecimal> ingredientes) {

    public void validarEstoqueDisponivel() {
        ingredientes.forEach((materia, qtdNecessaria) -> {
            if (materia.getEstoqueAtual().compareTo(qtdNecessaria) < 0) {
                throw new IllegalStateException("Estoque insuficiente de " + materia.getNome() +
                        ". Disponível: " + materia.getEstoqueAtual() + ", Necessário: " + qtdNecessaria);
            }
        });
    }

    public void baixarEstoque() {
        ingredientes.forEach((materia, qtd) -> {
            materia.setEstoqueAtual(materia.getEstoqueAtual().subtract(qtd));
            materia.setTotalVendido(materia.getTotalVendido().add(qtd));
        });
    }


    public void estornarEstoque() {
        ingredientes.forEach((materia, qtd) -> {
            materia.setEstoqueAtual(materia.getEstoqueAtual().add(qtd));
            materia.setTotalVendido(materia.getTotalVendido().subtract(qtd));
        });
    }


}