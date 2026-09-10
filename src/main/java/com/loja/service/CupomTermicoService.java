package com.loja.service;

import com.loja.model.CaixaSessao;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;

public class CupomTermicoService {

    public static String gerarTextoCupomFechamento(CaixaSessao sessao, String nomeLoja, boolean is80mm) {
        int colunas = is80mm ? 48 : 32;
        String div = "-".repeat(colunas);
        String divDupla = "=".repeat(colunas);

        StringBuilder sb = new StringBuilder();
        sb.append(divDupla).append("\n");
        sb.append(centralizar(nomeLoja != null && !nomeLoja.isEmpty() ? nomeLoja.toUpperCase() : "SYSTEM PRO", colunas)).append("\n");
        sb.append(centralizar("FECHAMENTO DE CAIXA / TURNO", colunas)).append("\n");
        sb.append(divDupla).append("\n");

        sb.append(String.format("TURNO / SESSÃO:    #%d\n", sessao.getId()));
        sb.append(String.format("STATUS:            %s\n", sessao.getStatus()));
        sb.append(String.format("ABERTURA:          %s\n", sessao.getDataAberturaFormatada()));
        sb.append(String.format("FECHAMENTO:        %s\n", sessao.getDataFechamentoFormatada()));
        sb.append(String.format("OP. ABERTURA:      %s\n", truncar(sessao.getOperadorAbertura(), colunas - 19)));
        sb.append(String.format("OP. FECHAMENTO:    %s\n", truncar(sessao.getOperadorFechamento() != null ? sessao.getOperadorFechamento() : sessao.getOperadorAbertura(), colunas - 19)));
        sb.append(div).append("\n");

        sb.append("FATURAMENTO / ENTRADAS DO TURNO\n");
        sb.append(formatarLinha("Dinheiro em Vendas/OS:", String.format("R$ %.2f", sessao.getTotalDinheiro()), colunas)).append("\n");
        sb.append(formatarLinha("PIX Recebido:", String.format("R$ %.2f", sessao.getTotalPix()), colunas)).append("\n");
        sb.append(formatarLinha("Débito (Bruto):", String.format("R$ %.2f", sessao.getTotalDebitoBruto()), colunas)).append("\n");
        sb.append(formatarLinha("Débito (Líquido):", String.format("R$ %.2f", sessao.getTotalDebitoLiquido()), colunas)).append("\n");
        sb.append(formatarLinha("Crédito (Bruto):", String.format("R$ %.2f", sessao.getTotalCreditoBruto()), colunas)).append("\n");
        sb.append(formatarLinha("Crédito (Líquido):", String.format("R$ %.2f", sessao.getTotalCreditoLiquido()), colunas)).append("\n");
        sb.append(div).append("\n");

        sb.append(formatarLinha("TOTAL VENDAS BRUTO:", String.format("R$ %.2f", sessao.getTotalVendasBruto()), colunas)).append("\n");
        sb.append(formatarLinha("TOTAL VENDAS LÍQUIDO:", String.format("R$ %.2f", sessao.getTotalVendasLiquido()), colunas)).append("\n");
        sb.append(formatarLinha("QTD DE OPERAÇÕES:", String.valueOf(sessao.getQtdVendas()), colunas)).append("\n");
        sb.append(div).append("\n");

        sb.append("CONFERÊNCIA DA GAVETA (DINHEIRO)\n");
        sb.append(formatarLinha("Fundo Troco Inicial:", String.format("R$ %.2f", sessao.getSaldoInicial()), colunas)).append("\n");
        sb.append(formatarLinha("(+) Suprimentos:", String.format("R$ %.2f", sessao.getTotalSuprimentos()), colunas)).append("\n");
        sb.append(formatarLinha("(-) Sangrias Anteriores:", String.format("R$ %.2f", sessao.getTotalSangrias()), colunas)).append("\n");
        sb.append(formatarLinha("(=) Saldo Esperado Sistema:", String.format("R$ %.2f", sessao.getSaldoFinalSistema()), colunas)).append("\n");
        sb.append(formatarLinha("(=) Contagem Física Real:", String.format("R$ %.2f", sessao.getSaldoFinalInformado()), colunas)).append("\n");
        sb.append(formatarLinha("DIFERENÇA APURADA:", sessao.getStatusDiferencaFormatado(), colunas)).append("\n");

        if (sessao.getJustificativaDiferenca() != null && !sessao.getJustificativaDiferenca().trim().isEmpty()) {
            sb.append("\nJUSTIFICATIVA DA DIVERGÊNCIA:\n");
            sb.append("  ").append(sessao.getJustificativaDiferenca().trim()).append("\n");
        }

        sb.append(div).append("\n");
        sb.append("DESTINAÇÃO DO DINHEIRO FÍSICO\n");
        if (sessao.getSangriaMalote() > 0) {
            sb.append(formatarLinha("Sangria / Malote Cofre:", String.format("R$ %.2f", sessao.getSangriaMalote()), colunas)).append("\n");
            sb.append(formatarLinha("Troco Deixado na Gaveta:", String.format("R$ %.2f", sessao.getFundoTrocoDeixado()), colunas)).append("\n");
        } else {
            sb.append(formatarLinha("Saldo em Gaveta:", String.format("R$ %.2f", sessao.getFundoTrocoDeixado()), colunas)).append("\n");
        }

        if (sessao.getContagemDetalhadaTexto() != null && !sessao.getContagemDetalhadaTexto().trim().isEmpty()) {
            sb.append(div).append("\n");
            sb.append("DETALHAMENTO DA CONTAGEM:\n");
            sb.append(sessao.getContagemDetalhadaTexto());
        }

        sb.append(divDupla).append("\n\n");
        sb.append("________________________________________\n");
        sb.append(centralizar("Assinatura do Operador de Caixa", colunas)).append("\n\n");
        sb.append("________________________________________\n");
        sb.append(centralizar("Assinatura do Gerente / Supervisor", colunas)).append("\n");
        sb.append(divDupla).append("\n");

        return sb.toString();
    }

    public static boolean imprimirCupom(String textoCupom, Component parent) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Fechamento de Caixa");

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(pf.getImageableX(), pf.getImageableY());
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 8));

                FontMetrics fm = g2d.getFontMetrics();
                int lineHeight = fm.getHeight();
                int y = lineHeight;

                String[] linhas = textoCupom.split("\n");
                for (String linha : linhas) {
                    g2d.drawString(linha, 10, y);
                    y += lineHeight;
                }

                return PAGE_EXISTS;
            }
        });

        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(parent, "Cupom enviado para a impressora com sucesso!", "Impressão", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(parent, "Erro ao imprimir cupom: " + e.getMessage(), "Erro de Impressão", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    private static String centralizar(String texto, int largura) {
        if (texto == null) return "";
        if (texto.length() >= largura) return texto.substring(0, largura);
        int espacos = (largura - texto.length()) / 2;
        return " ".repeat(espacos) + texto;
    }

    private static String formatarLinha(String esquerda, String direita, int largura) {
        if (esquerda == null) esquerda = "";
        if (direita == null) direita = "";
        int espacoDisponivel = largura - esquerda.length() - direita.length();
        if (espacoDisponivel < 1) {
            int corte = largura - direita.length() - 2;
            if (corte > 0) esquerda = esquerda.substring(0, corte) + ".";
            espacoDisponivel = 1;
        }
        return esquerda + " ".repeat(espacoDisponivel) + direita;
    }

    private static String truncar(String texto, int max) {
        if (texto == null) return "-";
        if (max <= 3) return texto;
        return texto.length() > max ? texto.substring(0, max - 3) + "..." : texto;
    }
}
