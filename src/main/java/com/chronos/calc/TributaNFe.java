/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chronos.calc;

import com.chronos.calc.csosn.Csosn101;
import com.chronos.calc.csosn.Csosn201;
import com.chronos.calc.csosn.Csosn202;
import com.chronos.calc.csosn.Csosn203;
import com.chronos.calc.csosn.Csosn900;
import com.chronos.calc.cst.Cst00;
import com.chronos.calc.cst.Cst10;
import com.chronos.calc.cst.Cst20;
import com.chronos.calc.cst.Cst30;
import com.chronos.calc.cst.Cst51;
import com.chronos.calc.cst.Cst70;
import com.chronos.calc.cst.Cst90;
import com.chronos.calc.dto.Cofins;
import com.chronos.calc.dto.Icms;
import com.chronos.calc.dto.Imposto;
import com.chronos.calc.dto.Ipi;
import com.chronos.calc.dto.Issqn;
import com.chronos.calc.dto.Pis;
import com.chronos.calc.dto.Produto;
import com.chronos.calc.enuns.Crt;
import com.chronos.calc.enuns.Csosn;
import static com.chronos.calc.enuns.Csosn.Csosn203;
import static com.chronos.calc.enuns.Csosn.Csosn900;
import com.chronos.calc.enuns.Cst;
import com.chronos.calc.enuns.CstPisCofins;
import com.chronos.calc.enuns.TipoOperacao;
import com.chronos.calc.enuns.TipoPessoa;
import com.chronos.calc.resultados.IResultadoCalculoDifal;
import com.chronos.calc.resultados.IResultadoCalculoIbpt;
import com.chronos.calc.resultados.IResultadoCalculoIpi;
import com.chronos.calc.resultados.IResultadoCalculoIssqn;
import com.chronos.calc.resultados.IResultadoCalculoPis;
import com.chronos.calc.resultados.imp.DadosMensagemDifal;
import java.math.BigDecimal;

/**
 *
 * @author John Vanderson M L
 */
public class TributaNFe {

    private final Produto produto;
    private final CalcTributacao calcular;
    private TipoOperacao operacao;
    private TipoPessoa pessoa;

    public TributaNFe(Produto produto) {
        this.produto = produto;
        this.calcular = new CalcTributacao(produto);
    }

    public Imposto tributarNfe(Produto produto, Crt crt, TipoOperacao operacao, TipoPessoa pessoa) {
        Imposto imposto = new Imposto();
        this.pessoa = pessoa;
        this.operacao = operacao;

        if (produto.isServico()) {
            Issqn iss = calcularIssqn();
            imposto.setIssqn(iss);
        } else {

            Icms icms = crt.equals(Crt.SimplesNaciona) ? tributarIcmsSimplesNascinal(produto.getCsosn()) : tributarIcms(produto.getCst(), pessoa);
            icms = calcularDifal(icms);
            icms = calcularIbpt(icms);
            Ipi ipi = calcularIpi();
            Pis pis = calcularPis();
            Cofins cofins = calcularCofins();
            imposto.setCofins(cofins);
            imposto.setIcms(icms);
            imposto.setIpi(ipi);
            imposto.setPis(pis);
        }
        return imposto;
    }

    /**
     * Simples Nacional - excesso de sublimite de receita bruta; 3 - Regime
     * Normal.
     *
     * @param cst
     * @return
     */
    private Icms tributarIcms(Cst cst, TipoPessoa tipoPessoa) {
        Icms calculo = new Icms();
        BigDecimal valorBcIcms = null;
        BigDecimal valorIcms = null;
        BigDecimal percentualIcms = null;
        BigDecimal percentualReducao = null;

        BigDecimal percentualMva = null;
        BigDecimal percentualReducaoST = null;
        BigDecimal valorBcIcmsSt = null;
        BigDecimal valorIcmsST = null;
        BigDecimal percentualIcmsST = null;

        BigDecimal valorIcmsOperacao;
        BigDecimal percentualDiferimento;
        BigDecimal valorIcmsDeferido;

        switch (cst) {
            case Cst00:
                Cst00 cst00 = new Cst00();
                switch (cst00.getModalidadeDeterminacaoBcIcms()) {
                    //Valor da Operação
                    case ValorOperacao:
                        //pessoa fisica inclui ipi na base de calculo do icms
                        //pessoa fisica sempre usa a aliquota normal da UF emissao
                        if (tipoPessoa == TipoPessoa.Fisica) {
                            BigDecimal valorIpi = calcular.calcularIpi().getValor();
                            produto.setValorIpi(valorIpi);
                            cst00.calcular(produto);
                            valorBcIcms = cst00.getValorBcIcms();
                            valorIcms = cst00.getValorIcms();
                            percentualIcms = cst00.getPercentualIcms();

                        } else {

                            cst00.calcular(produto);
                            valorBcIcms = cst00.getValorBcIcms();
                            valorIcms = cst00.getValorIcms();
                            percentualIcms = cst00.getPercentualIcms();
                        }

                        calculo.setValorIcms(valorIcms);
                        calculo.setValorBcIcms(valorBcIcms);
                        calculo.setPercentualIcms(percentualIcms);
                        break;
                }
                break;
            case Cst10:
                Cst10 cst10 = new Cst10();

                switch (cst10.getModalidadeDeterminacaoBcIcmsSt()) {
                    case MargemValorAgregado:

                        if (tipoPessoa == TipoPessoa.Fisica) {
                            BigDecimal valorIpi = calcular.calcularIpi().getValor();
                            produto.setValorIpi(valorIpi);
                            valorBcIcms = cst10.getValorBcIcms();
                            percentualIcms = cst10.getPercentualIcms();
                            valorIcms = cst10.getValorIcms();
                            percentualMva = cst10.getPercentualMva();
                            percentualReducaoST = cst10.getPercentualReducaoSt();
                            valorBcIcmsSt = cst10.getValorBcIcmsSt();
                            valorIcmsST = cst10.getValorIcmsSt();
                            percentualIcmsST = cst10.getPercentualIcmsSt();
                        } else {
                            valorBcIcms = cst10.getValorBcIcms();
                            percentualIcms = cst10.getPercentualIcms();
                            valorIcms = cst10.getValorIcms();
                            percentualMva = cst10.getPercentualMva();
                            percentualReducaoST = cst10.getPercentualReducaoSt();
                            valorBcIcmsSt = cst10.getValorBcIcmsSt();
                            valorIcmsST = cst10.getValorIcmsSt();
                            percentualIcmsST = cst10.getPercentualIcmsSt();
                        }
                        calculo.setValorBcIcms(valorBcIcms);
                        calculo.setPercentualIcms(percentualIcms);
                        calculo.setValorIcms(valorIcms);
                        calculo.setPercentualMva(percentualMva);
                        calculo.setPercentualReducaoST(percentualReducaoST);
                        calculo.setValorBaseCalcST(valorBcIcmsSt);
                        calculo.setPercentualIcmsST(percentualIcmsST);
                        calculo.setValorIcmsST(valorIcmsST);

                        break;
                }
                break;
            case Cst20:
                Cst20 cst20 = new Cst20();
                switch (cst20.getModalidadeDeterminacaoBcIcms()) {
                    case ValorOperacao:
                        if (tipoPessoa == TipoPessoa.Fisica) {
                            BigDecimal valorIpi = calcular.calcularIpi().getValor();
                            produto.setValorIpi(valorIpi);
                            cst20.calcular(produto);
                            valorBcIcms = cst20.getValorBcIcms();
                            valorIcms = cst20.getValorIcms();
                            percentualIcms = cst20.getPercentualIcms();
                            percentualReducao = cst20.getPercentualReducao();

                        } else {

                            cst20.calcular(produto);
                            valorBcIcms = cst20.getValorBcIcms();
                            valorIcms = cst20.getValorIcms();
                            percentualIcms = cst20.getPercentualIcms();
                            percentualReducao = cst20.getPercentualReducao();
                        }

                        calculo.setPercentualReducao(percentualReducao);
                        calculo.setValorBcIcms(valorBcIcms);
                        calculo.setPercentualIcms(percentualIcms);
                        calculo.setValorIcms(valorIcms);

                        break;
                }

                break;
            case Cst30:
                //30 Isenta ou não tributada e com cobrança do ICMS por substituição tributária
                Cst30 cst30 = new Cst30();

                switch (cst30.getModalidadeDeterminacaoBcIcmsSt()) {
                    case MargemValorAgregado:
                        if (tipoPessoa == TipoPessoa.Fisica) {
                            BigDecimal valorIpi = calcular.calcularIpi().getValor();
                            produto.setValorIpi(valorIpi);
                            cst30.calcular(produto);
                            percentualMva = cst30.getPercentualMva();
                            percentualReducaoST = cst30.getPercentualReducaoSt();
                            valorBcIcmsSt = cst30.getValorBcIcmsSt();
                            valorIcmsST = cst30.getValorIcmsSt();
                            percentualIcmsST = cst30.getPercentualIcmsSt();
                        } else {
                            cst30.calcular(produto);
                            percentualMva = cst30.getPercentualMva();
                            percentualReducaoST = cst30.getPercentualReducaoSt();
                            valorBcIcmsSt = cst30.getValorBcIcmsSt();
                            valorIcmsST = cst30.getValorIcmsSt();
                            percentualIcmsST = cst30.getPercentualIcmsSt();
                        }

                        calculo.setPercentualMva(percentualMva);
                        calculo.setPercentualReducaoST(percentualReducaoST);
                        calculo.setValorBaseCalcST(valorBcIcmsSt);
                        calculo.setPercentualIcmsST(percentualIcmsST);
                        calculo.setValorIcmsST(valorIcmsST);

                        break;
                }

                break;
            case Cst40:
                //40 Isenta do ICMS
                break;
            case Cst41:
                //41 Nao tributada no ICMS
                break;
            case Cst50:
                //50 Suspensao do ICMS
                break;
            case Cst51:
                Cst51 cst51 = new Cst51();
                switch (cst51.getModalidadeDeterminacaoBcIcms()) {
                    case ValorOperacao:
                        if (tipoPessoa == TipoPessoa.Fisica) {
                            BigDecimal valorIpi = calcular.calcularIpi().getValor();
                            produto.setValorIpi(valorIpi);
                            cst51.calcular(produto);

                            valorBcIcms = cst51.getValorBcIcms();
                            valorIcms = cst51.getValorIcms();
                            percentualIcms = cst51.getPercentualIcms();
                            valorIcmsOperacao = cst51.getValorIcmsOperacao();
                            percentualDiferimento = cst51.getPercentualDiferimento();
                            valorIcmsDeferido = cst51.getValorIcmsDiferido();
                            percentualReducao = cst51.getPercentualReducao();

                        } else {

                            cst51.calcular(produto);

                            valorBcIcms = cst51.getValorBcIcms();
                            valorIcms = cst51.getValorIcms();
                            percentualIcms = cst51.getPercentualIcms();
                            valorIcmsOperacao = cst51.getValorIcmsOperacao();
                            percentualDiferimento = cst51.getPercentualDiferimento();
                            valorIcmsDeferido = cst51.getValorIcmsDiferido();
                            percentualReducao = cst51.getPercentualReducao();
                        }

                        calculo.setPercentualReducao(percentualReducao);
                        calculo.setValorBcIcms(valorBcIcms);
                        calculo.setPercentualIcms(percentualIcms);
                        calculo.setValorIcmsOperacao(valorIcmsOperacao);
                        calculo.setPercentualDiferimento(percentualDiferimento);
                        calculo.setValorIcmsDeferido(valorIcmsDeferido);
                        calculo.setValorIcms(valorIcms);

                        break;
                }
                break;
            case Cst60:
                //60 ICMS cobrado anteriormente por substituição tributária
                break;
            case Cst70:
                Cst70 cst70 = new Cst70();
                if (tipoPessoa == TipoPessoa.Fisica) {
                    BigDecimal valorIpi = calcular.calcularIpi().getValor();
                    produto.setValorIpi(valorIpi);
                }
                cst70.calcular(produto);
                switch (cst70.getModalidadeDeterminacaoBcIcms()) {
                    case ValorOperacao:
                        valorBcIcms = cst70.getValorBcIcms();
                        percentualIcms = cst70.getPercentualIcms();
                        percentualReducao = cst70.getPercentualReducao();
                        valorIcms = cst70.getValorIcms();
                        break;
                }
                switch (cst70.getModalidadeDeterminacaoBcIcmsSt()) {
                    case MargemValorAgregado:
                        percentualMva = cst70.getPercentualMva();
                        percentualReducaoST = cst70.getPercentualReducaoSt();
                        valorBcIcmsSt = cst70.getValorBcIcmsSt();
                        valorIcmsST = cst70.getValorIcmsSt();
                        percentualIcmsST = cst70.getPercentualIcmsSt();
                        break;
                }

                calculo.setPercentualReducao(percentualReducao);
                calculo.setValorBcIcms(valorBcIcms);
                calculo.setPercentualIcms(percentualIcms);
                calculo.setValorIcms(valorIcms);

                calculo.setPercentualMva(percentualMva);
                calculo.setPercentualReducaoST(percentualReducaoST);
                calculo.setValorBaseCalcST(valorBcIcmsSt);
                calculo.setPercentualIcmsST(percentualIcmsST);
                calculo.setValorIcmsST(valorIcmsST);

                break;
            case Cst90:
                Cst90 cst90 = new Cst90();
                if (tipoPessoa == TipoPessoa.Fisica) {
                    BigDecimal valorIpi = calcular.calcularIpi().getValor();
                    produto.setValorIpi(valorIpi);
                }
                cst90.calcular(produto);
                switch (cst90.getModalidadeDeterminacaoBcIcms()) {
                    case ValorOperacao:
                        percentualIcms = cst90.getPercentualIcms();
                        valorIcms = cst90.getValorIcms();
                        valorBcIcms = cst90.getValorBcIcms();
                        break;
                }
                switch (cst90.getModalidadeDeterminacaoBcIcmsSt()) {
                    case MargemValorAgregado:

                        percentualMva = cst90.getPercentualMva();
                        percentualIcmsST = cst90.getPercentualIcmsSt();
                        percentualReducaoST = cst90.getPercentualReducaoSt();
                        valorIcmsST = cst90.getValorIcmsSt();
                        valorBcIcmsSt = cst90.getValorBcIcmsSt();
                        break;

                }
                calculo.setPercentualReducao(percentualReducao);
                calculo.setValorBcIcms(valorBcIcms);
                calculo.setPercentualIcms(percentualIcms);
                calculo.setValorIcms(valorIcms);

                calculo.setPercentualMva(percentualMva);
                calculo.setPercentualReducaoST(percentualReducaoST);
                calculo.setValorBaseCalcST(valorBcIcmsSt);
                calculo.setPercentualIcmsST(percentualIcmsST);
                calculo.setValorIcmsST(valorIcmsST);

                break;
        }

        return calculo;
    }

    /**
     * 1 = Simples Nacional
     *
     * @param cosn
     * @return
     */
    private Icms tributarIcmsSimplesNascinal(Csosn cosn) {
        Icms calculo = new Icms();
        BigDecimal percentualCredito;
        BigDecimal valorCredito;
        switch (cosn) {
            //101 Tributada pelo Simples Nacional com permissão de crédito
            case Csosn101:

                Csosn101 csosn = new Csosn101();
                csosn.calcular(produto);
                valorCredito = csosn.getValorCredito();
                percentualCredito = csosn.getPercentualCredito();
                calculo.setValorCredito(valorCredito);
                calculo.setPercentualCredito(percentualCredito);

                break;

            case Csosn102:
                //102 Tributada pelo Simples Nacional sem permissão de crédito
                //nao tem calculo
                break;
            case Csosn103:
                //103 Isenção do ICMS no Simples Nacional para faixa de receita bruta
                //nao tem calculo
                break;
            //201 Tributada pelo Simples Nacional com permissão de crédito e com cobrança do ICMS por substituição tributária    
            case Csosn201:

                Csosn201 csosn201 = new Csosn201();

                csosn201.calcular(produto);

                percentualCredito = csosn201.getPercentualCredito();
                valorCredito = csosn201.getValorCredito();

                calculo.setValorCredito(valorCredito);
                calculo.setPercentualCredito(percentualCredito);

                switch (csosn201.getModalidadeDeterminacaoBcIcmsSt()) {
                    case ListaNegativa:
                        //lista Negativa(valor)
                        break;
                    case ListaPositiva:
                        //Lista Positiva(valor)
                        break;
                    case ListaNeutra:
                        //Lista Neutra(valor)
                        break;
                    case MargemValorAgregado:
                        //Margem valor Agregado(%)
                        BigDecimal percentualMva = csosn201.getPercentualMva();
                        BigDecimal percentualIcmsST = csosn201.getPercentualIcmsSt();
                        BigDecimal percentualReducaoST = csosn201.getPercentualReducaoSt();
                        BigDecimal valorIcmsST = csosn201.getValorIcmsSt();
                        BigDecimal valorBaseCalcST = csosn201.getValorBcIcmsSt();

                        calculo.setPercentualMva(percentualMva);
                        calculo.setPercentualIcmsST(percentualIcmsST);
                        calculo.setPercentualReducaoST(percentualReducaoST);
                        calculo.setValorIcmsST(valorIcmsST);
                        calculo.setValorBaseCalcST(valorBaseCalcST);

                        break;
                    case Pauta:

                        break;
                    case PrecoTabeladoOuMaximoSugerido:
                        //Preço Tabelado ou Máximo Sugerido
                        break;
                }
                break;
            //202 Tributada pelo Simples Nacional sem permissão de crédito e com cobrança do ICMS por substituição tributária    
            case Csosn202:
                Csosn202 csosn202 = new Csosn202();
                csosn202.calcular(produto);

                switch (csosn202.getModalidadeDeterminacaoBcIcmsSt()) {
                    case ListaNegativa:
                        //lista Negativa(valor)
                        break;
                    case ListaPositiva:
                        //Lista Positiva(valor)
                        break;
                    case ListaNeutra:
                        //Lista Neutra(valor)
                        break;
                    case MargemValorAgregado:
                        //Margem valor Agregado(%)
                        BigDecimal percentualMva = csosn202.getPercentualMva();
                        BigDecimal percentualIcmsST = csosn202.getPercentualIcmsSt();
                        BigDecimal percentualReducaoST = csosn202.getPercentualReducaoSt();
                        BigDecimal valorIcmsST = csosn202.getValorIcmsSt();
                        BigDecimal valorBaseCalcST = csosn202.getValorBcIcmsSt();

                        calculo.setPercentualMva(percentualMva);
                        calculo.setPercentualIcmsST(percentualIcmsST);
                        calculo.setPercentualReducaoST(percentualReducaoST);
                        calculo.setValorIcmsST(valorIcmsST);
                        calculo.setValorBaseCalcST(valorBaseCalcST);

                        break;
                    case Pauta:

                        break;
                    case PrecoTabeladoOuMaximoSugerido:
                        //Preço Tabelado ou Máximo Sugerido
                        break;
                }

                break;
            //203 Tributada pelo Simples Nacional sem permissão de crédito e com cobrança do ICMS por substituição tributária    
            case Csosn203:
                Csosn203 csosn203 = new Csosn203();
                csosn203.calcular(produto);

                switch (csosn203.getModalidadeDeterminacaoBcIcmsSt()) {
                    case ListaNegativa:
                        //lista Negativa(valor)
                        break;
                    case ListaPositiva:
                        //Lista Positiva(valor)
                        break;
                    case ListaNeutra:
                        //Lista Neutra(valor)
                        break;
                    case MargemValorAgregado:
                        //Margem valor Agregado(%)
                        BigDecimal percentualMva = csosn203.getPercentualMva();
                        BigDecimal percentualIcmsST = csosn203.getPercentualIcmsSt();
                        BigDecimal percentualReducaoST = csosn203.getPercentualReducaoSt();
                        BigDecimal valorIcmsST = csosn203.getValorIcmsSt();
                        BigDecimal valorBaseCalcST = csosn203.getValorBcIcmsSt();

                        calculo.setPercentualMva(percentualMva);
                        calculo.setPercentualIcmsST(percentualIcmsST);
                        calculo.setPercentualReducaoST(percentualReducaoST);
                        calculo.setValorIcmsST(valorIcmsST);
                        calculo.setValorBaseCalcST(valorBaseCalcST);

                        break;
                    case Pauta:

                        break;
                    case PrecoTabeladoOuMaximoSugerido:
                        //Preço Tabelado ou Máximo Sugerido
                        break;
                }

                break;

            case Csosn300:
                //300 Imune - Classificam-se neste código as operações praticadas por optantes pelo Simples Nacional contempladas com imunidade do ICMS
                //nao tem calculo
                break;

            case Csosn400:
                //400 Não tributada pelo Simples Nacional - Classificam-se neste código as operações praticadas por optantes pelo Simples Nacional não sujeitas à tributação pelo ICMS dentro do Simples Nacional
                //nao tem calculo
                break;
            case Csosn500:
                //500 ICMS cobrado anteriormente por substituição tributária (substituído) ou por antecipação - Classificam-se neste código as operações sujeitas exclusivamente ao regime de substituição tributária na condição de substituído tributário ou no caso de antecipações
                //nao tem calculo

                break;

            case Csosn900:
                Csosn900 csosn900 = new Csosn900();
                csosn900.calcular(produto);

                percentualCredito = csosn900.getPercentualCredito();
                BigDecimal percentualMva = csosn900.getPercentualMva();
                BigDecimal percentualIcmsST = csosn900.getPercentualIcmsSt();
                BigDecimal percentualReducaoST = csosn900.getPercentualReducaoSt();
                BigDecimal valorIcmsST = csosn900.getValorIcmsSt();
                BigDecimal valorBaseCalcST = csosn900.getValorBcIcmsSt();
                valorCredito = csosn900.getValorCredito();
                BigDecimal valorIcms = csosn900.getValorIcms();
                BigDecimal valorBcIcms = csosn900.getValorBcIcms();

                calculo.setPercentualCredito(percentualCredito);
                calculo.setValorCredito(valorCredito);
                calculo.setValorIcms(valorIcms);
                calculo.setValorBcIcms(valorBcIcms);
                calculo.setPercentualMva(percentualMva);
                calculo.setPercentualIcmsST(percentualIcmsST);
                calculo.setPercentualReducaoST(percentualReducaoST);
                calculo.setValorIcmsST(valorIcmsST);
                calculo.setValorBaseCalcST(valorBaseCalcST);

                break;

        }

        return calculo;
    }

    private Icms calcularDifal(Icms icms) {
        String cstCson = (produto.getCst() != null) ? produto.getCst().getCodigo().toString() : produto.getCsosn().getCodigo().toString();
        if (pessoa == TipoPessoa.Fisica && operacao == TipoOperacao.OperacaoInterestadual && cstGeraDifal(cstCson)) {
            if (produto.getPercentualDifalInterna().signum() != 0 && produto.getPercentualDifalInterestadual().signum() != 0) {
               

                IResultadoCalculoDifal result = calcular.calculaDifalFcp();
                BigDecimal baseCalculoDifal = result.getBaseCalculo();
                BigDecimal fcp = result.getFcp();
                BigDecimal difal = result.getDifal();
                BigDecimal valorIcmsOrigem = result.getValorIcmsOrigem();
                BigDecimal valorIcmsDestino = result.getValorIcmsDestino();
                
                
                String obs = result.getObservacao(new DadosMensagemDifal(fcp, valorIcmsDestino, valorIcmsOrigem));
                
                
                icms.setValorBcDifal(baseCalculoDifal);
                icms.setFcp(fcp);
                icms.setDifal(difal);
                icms.setValorIcmsOrigem(valorIcmsOrigem);
                icms.setValorIcmsDestino(valorIcmsDestino);
                icms.setObsDifal(obs);
                
                
            }
        }

        return icms;
    }

    private Ipi calcularIpi() {
        Ipi ipi = new Ipi();
        String cst = produto.getCstIpi().getCodigo();
        BigDecimal valor = BigDecimal.ZERO;
        BigDecimal baseCalculo = BigDecimal.ZERO;

        if (cst.equals("00")
                || cst.equals("49")
                || cst.equals("50")
                || cst.equals("99")) {
            IResultadoCalculoIpi result = calcular.calcularIpi();
            valor = result.getValor();
            baseCalculo = result.getBaseCalculo();
        }

        ipi.setValorBcIpi(baseCalculo);
        ipi.setValorIpi(valor);

        return ipi;
    }

    private Pis calcularPis() {
        Pis pis = new Pis();
        CstPisCofins cst = produto.getCstPisCofins();

        if (cst == CstPisCofins.Cst01 || cst == CstPisCofins.Cst02) {
            IResultadoCalculoPis result = calcular.calcularPis();
            BigDecimal valor = result.getValor();
            BigDecimal baseCalculo = result.getBaseCalculo();
            pis.setBaseCalculo(baseCalculo);
            pis.setValor(valor);
        }

        return pis;
    }

    private Cofins calcularCofins() {
        Cofins cofins = new Cofins();
        CstPisCofins cst = produto.getCstPisCofins();
        if (cst == CstPisCofins.Cst01 || cst == CstPisCofins.Cst02) {
            IResultadoCalculoPis result = calcular.calcularPis();
            BigDecimal valor = result.getValor();
            BigDecimal baseCalculo = result.getBaseCalculo();
            cofins.setBaseCalculo(baseCalculo);
            cofins.setValor(valor);
        }

        return cofins;
    }

    private Issqn calcularIssqn() {
        Issqn iss = new Issqn();

        IResultadoCalculoIssqn result = calcular.calcularIssqn();
        BigDecimal valor = result.getValor();
        BigDecimal baseCalculo = result.getBaseCalculo();
        iss.setValor(valor);
        iss.setBaseCalculo(baseCalculo);
        return iss;
    }

    private Icms calcularIbpt(Icms icms) {
      
        IResultadoCalculoIbpt result = calcular.calculaIbpt(produto);
        BigDecimal tributacaoEstadual = result.getTributacaoEstadual();
        BigDecimal tributacaoFederal = result.getTributacaoFederal();
        BigDecimal tributacaoFederalImp = result.getTributacaoFederalImportados();
        BigDecimal tributacaoMunicipal = result.getTributacaoMunicipal();

        icms.setTributacaoEstadual(tributacaoEstadual);
        icms.setTributacaoFederal(tributacaoFederal);
        icms.setTributacaoFederalImp(tributacaoFederalImp);
        icms.setTributacaoMunicipal(tributacaoMunicipal);

        return icms;
    }

    private boolean cstGeraDifal(String cst) {
        return cst.equals("00") || cst.equals("20") || cst.equals("40") || cst.equals("41") || cst.equals("60") || cst.equals("102") || cst.equals("103") || cst.equals("400") || cst.equals("500");
    }
}
