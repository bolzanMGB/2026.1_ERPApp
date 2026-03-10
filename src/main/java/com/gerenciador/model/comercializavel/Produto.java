package com.gerenciador.model.comercializavel;
import java.math.BigDecimal;
import java.util.List;

public class Produto implements Comercializavel {
    private String nome;
    private BigDecimal totalVendido = BigDecimal.ZERO;
    private Integer id;
    private List<MateriaPrima>  materiasPrimasNecessarias;

    public Produto(String nome, List<MateriaPrima> materiasPrimas ) {
        this.nome = validarNome(nome);
        this.materiasPrimasNecessarias = validarMateriasPrimas (materiasPrimas);
    }

    private static String validarNome (String valor){
        if (valor == null || valor.isBlank()){
            throw new IllegalArgumentException ("Nome é inválido");
        }
        return valor;
    }

    private static List<MateriaPrima> validarMateriasPrimas (List<MateriaPrima> materiasPrimas){
        if (materiasPrimas == null || materiasPrimas.isEmpty()) {
            throw new IllegalArgumentException("Lista de matérias-primas inválida");
        }

        for (MateriaPrima m : materiasPrimas) {
            if (m == null) {
                throw new IllegalArgumentException("Matéria-prima não pode ser null");
            }
        }

        return materiasPrimas;
    }

    public void setNome(String nome) {
        this.nome = validarNome(nome);
    }

    public void setTotalVendido(BigDecimal totalVendido) {
        this.totalVendido = totalVendido;
    }

    public void setMateriasPrimasNecessarias(List<MateriaPrima> materiasPrimasNecessarias) {
        this.materiasPrimasNecessarias = materiasPrimasNecessarias;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public String getTipo() {
        return "Produto";
    }

    @Override
    public BigDecimal getTotalVendido() {
        return this.totalVendido;
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    public List<MateriaPrima> getMateriasPrimasNecessarias() {
        return materiasPrimasNecessarias;
    }


    public void setId(Integer id) {
        this.id = id;
    }
}
