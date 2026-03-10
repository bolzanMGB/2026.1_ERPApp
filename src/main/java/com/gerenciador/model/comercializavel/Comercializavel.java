package com.gerenciador.model.comercializavel;

import java.math.BigDecimal;

public interface Comercializavel {
    String getNome();
    String getTipo();
    BigDecimal getTotalVendido();
    Integer getId();

}
