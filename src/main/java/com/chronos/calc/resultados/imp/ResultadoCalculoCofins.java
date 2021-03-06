/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chronos.calc.resultados.imp;

import com.chronos.calc.resultados.IResultadoCalculoCofins;
import java.math.BigDecimal;

/**
 *
 * @author John Vanderson M L
 */
public class ResultadoCalculoCofins implements IResultadoCalculoCofins{
    
    private final BigDecimal baseCalculo;
    private final BigDecimal valor;

    public ResultadoCalculoCofins(BigDecimal baseCalculo, BigDecimal valor) {
        this.baseCalculo = baseCalculo;
        this.valor = valor;
    }
    @Override
    public BigDecimal getBaseCalculo() {
        return baseCalculo;
    }

    @Override
    public BigDecimal getValor() {
        return valor;
    }
}
