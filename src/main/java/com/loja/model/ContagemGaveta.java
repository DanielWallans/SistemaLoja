package com.loja.model;

import java.io.Serializable;

public class ContagemGaveta implements Serializable {
    private static final long serialVersionUID = 1L;

    // Cédulas
    private int qtd200;
    private int qtd100;
    private int qtd50;
    private int qtd20;
    private int qtd10;
    private int qtd5;
    private int qtd2;

    // Moedas
    private int qtd1Real;
    private int qtd50Cent;
    private int qtd25Cent;
    private int qtd10Cent;
    private int qtd5Cent;

    // Outros valores avulsos (ex: moedas/cédulas diversas ou comprovantes manuais)
    private double outrosValores;

    public ContagemGaveta() {
    }

    public double getTotalCalculado() {
        return (qtd200 * 200.0) +
               (qtd100 * 100.0) +
               (qtd50 * 50.0) +
               (qtd20 * 20.0) +
               (qtd10 * 10.0) +
               (qtd5 * 5.0) +
               (qtd2 * 2.0) +
               (qtd1Real * 1.0) +
               (qtd50Cent * 0.50) +
               (qtd25Cent * 0.25) +
               (qtd10Cent * 0.10) +
               (qtd5Cent * 0.05) +
               outrosValores;
    }

    public int getQtd200() { return qtd200; }
    public void setQtd200(int qtd200) { this.qtd200 = qtd200; }

    public int getQtd100() { return qtd100; }
    public void setQtd100(int qtd100) { this.qtd100 = qtd100; }

    public int getQtd50() { return qtd50; }
    public void setQtd50(int qtd50) { this.qtd50 = qtd50; }

    public int getQtd20() { return qtd20; }
    public void setQtd20(int qtd20) { this.qtd20 = qtd20; }

    public int getQtd10() { return qtd10; }
    public void setQtd10(int qtd10) { this.qtd10 = qtd10; }

    public int getQtd5() { return qtd5; }
    public void setQtd5(int qtd5) { this.qtd5 = qtd5; }

    public int getQtd2() { return qtd2; }
    public void setQtd2(int qtd2) { this.qtd2 = qtd2; }

    public int getQtd1Real() { return qtd1Real; }
    public void setQtd1Real(int qtd1Real) { this.qtd1Real = qtd1Real; }

    public int getQtd50Cent() { return qtd50Cent; }
    public void setQtd50Cent(int qtd50Cent) { this.qtd50Cent = qtd50Cent; }

    public int getQtd25Cent() { return qtd25Cent; }
    public void setQtd25Cent(int qtd25Cent) { this.qtd25Cent = qtd25Cent; }

    public int getQtd10Cent() { return qtd10Cent; }
    public void setQtd10Cent(int qtd10Cent) { this.qtd10Cent = qtd10Cent; }

    public int getQtd5Cent() { return qtd5Cent; }
    public void setQtd5Cent(int qtd5Cent) { this.qtd5Cent = qtd5Cent; }

    public double getOutrosValores() { return outrosValores; }
    public void setOutrosValores(double outrosValores) { this.outrosValores = outrosValores; }

    public String toResumoTexto() {
        StringBuilder sb = new StringBuilder();
        if (qtd200 > 0) sb.append(String.format("• %d x R$ 200,00 = R$ %.2f\n", qtd200, qtd200 * 200.0));
        if (qtd100 > 0) sb.append(String.format("• %d x R$ 100,00 = R$ %.2f\n", qtd100, qtd100 * 100.0));
        if (qtd50 > 0) sb.append(String.format("• %d x R$ 50,00 = R$ %.2f\n", qtd50, qtd50 * 50.0));
        if (qtd20 > 0) sb.append(String.format("• %d x R$ 20,00 = R$ %.2f\n", qtd20, qtd20 * 20.0));
        if (qtd10 > 0) sb.append(String.format("• %d x R$ 10,00 = R$ %.2f\n", qtd10, qtd10 * 10.0));
        if (qtd5 > 0) sb.append(String.format("• %d x R$ 5,00 = R$ %.2f\n", qtd5, qtd5 * 5.0));
        if (qtd2 > 0) sb.append(String.format("• %d x R$ 2,00 = R$ %.2f\n", qtd2, qtd2 * 2.0));
        if (qtd1Real > 0) sb.append(String.format("• %d x R$ 1,00 = R$ %.2f\n", qtd1Real, qtd1Real * 1.0));
        if (qtd50Cent > 0) sb.append(String.format("• %d x R$ 0,50 = R$ %.2f\n", qtd50Cent, qtd50Cent * 0.50));
        if (qtd25Cent > 0) sb.append(String.format("• %d x R$ 0,25 = R$ %.2f\n", qtd25Cent, qtd25Cent * 0.25));
        if (qtd10Cent > 0) sb.append(String.format("• %d x R$ 0,10 = R$ %.2f\n", qtd10Cent, qtd10Cent * 0.10));
        if (qtd5Cent > 0) sb.append(String.format("• %d x R$ 0,05 = R$ %.2f\n", qtd5Cent, qtd5Cent * 0.05));
        if (outrosValores > 0) sb.append(String.format("• Outros valores: R$ %.2f\n", outrosValores));
        return sb.toString();
    }
}
