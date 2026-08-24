package com.loja.service;

import com.loja.model.Cliente;
import com.loja.model.Equipamento;
import com.loja.model.OrdemServico;
import com.loja.model.PecaItem;
import com.loja.model.ServicoItem;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrcamentoPDFService {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static File gerarOrcamentoPDF(File arquivoDestino, OrdemServico os, Cliente cliente, Equipamento equip,
                                         List<ServicoItem> servicos, List<PecaItem> pecas) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(arquivoDestino));
        document.open();

        Font fontTituloEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(41, 128, 185));
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
        Font fontSecao = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
        Font fontTexto = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font fontTextoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(39, 174, 96));

        // 1. Cabeçalho da Empresa / Orçamento
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60, 40});

        PdfPCell cellEmpresa = new PdfPCell();
        cellEmpresa.setBorder(Rectangle.NO_BORDER);
        cellEmpresa.addElement(new Paragraph("ASSISTÊNCIA TÉCNICA ESPECIALIZADA", fontTituloEmpresa));
        cellEmpresa.addElement(new Paragraph("Manutenção de Computadores, Notebooks, Smartphones e Impressoras", fontSubtitulo));
        headerTable.addCell(cellEmpresa);

        PdfPCell cellOSInfo = new PdfPCell();
        cellOSInfo.setBorder(Rectangle.NO_BORDER);
        cellOSInfo.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph pOS = new Paragraph("ORÇAMENTO Nº #" + os.getId(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.RED));
        pOS.setAlignment(Element.ALIGN_RIGHT);
        Paragraph pData = new Paragraph("Emissão: " + os.getDataEntrada().format(formatter), fontSubtitulo);
        pData.setAlignment(Element.ALIGN_RIGHT);
        Paragraph pStatus = new Paragraph("Status: " + os.getStatus(), fontTextoBold);
        pStatus.setAlignment(Element.ALIGN_RIGHT);
        cellOSInfo.addElement(pOS);
        cellOSInfo.addElement(pData);
        cellOSInfo.addElement(pStatus);
        headerTable.addCell(cellOSInfo);

        document.add(headerTable);
        document.add(new Paragraph(" "));

        // 2. Dados do Cliente e Equipamento
        PdfPTable dadosTable = new PdfPTable(2);
        dadosTable.setWidthPercentage(100);
        dadosTable.setWidths(new float[]{50, 50});

        // Header Secao Cliente
        PdfPCell h1 = new PdfPCell(new Phrase(" DADOS DO CLIENTE", fontSecao));
        h1.setBackgroundColor(new Color(41, 128, 185));
        h1.setPadding(4);
        dadosTable.addCell(h1);

        // Header Secao Equipamento
        PdfPCell h2 = new PdfPCell(new Phrase(" DADOS DO EQUIPAMENTO", fontSecao));
        h2.setBackgroundColor(new Color(41, 128, 185));
        h2.setPadding(4);
        dadosTable.addCell(h2);

        // Conteúdo Cliente
        PdfPCell cCliente = new PdfPCell();
        cCliente.setPadding(6);
        if (cliente != null) {
            cCliente.addElement(new Paragraph("Nome: " + cliente.getNome(), fontTextoBold));
            cCliente.addElement(new Paragraph("Telefone/WhatsApp: " + (cliente.getTelefone() != null ? cliente.getTelefone() : "-"), fontTexto));
            cCliente.addElement(new Paragraph("CPF/CNPJ: " + (cliente.getCpfCnpj() != null ? cliente.getCpfCnpj() : "-"), fontTexto));
            cCliente.addElement(new Paragraph("Endereço: " + (cliente.getEndereco() != null ? cliente.getEndereco() : "-"), fontTexto));
        } else {
            cCliente.addElement(new Paragraph("Cliente Avulso", fontTexto));
        }
        dadosTable.addCell(cCliente);

        // Conteúdo Equipamento
        PdfPCell cEquip = new PdfPCell();
        cEquip.setPadding(6);
        if (equip != null) {
            cEquip.addElement(new Paragraph("Aparelho: " + equip.getTipo() + " " + equip.getMarca() + " " + equip.getModelo(), fontTextoBold));
            cEquip.addElement(new Paragraph("Nº de Série: " + equip.getNumeroSerie() + " | Cor: " + equip.getCor(), fontTexto));
            cEquip.addElement(new Paragraph("Avarias de Entrada: " + equip.getAvarias(), fontTexto));
            cEquip.addElement(new Paragraph("Acessórios: " + equip.getAcessorios(), fontTexto));
        } else {
            cEquip.addElement(new Paragraph("Equipamento não localizado", fontTexto));
        }
        dadosTable.addCell(cEquip);

        document.add(dadosTable);
        document.add(new Paragraph(" "));

        // 3. Defeito e Diagnóstico Técnico
        PdfPTable defTable = new PdfPTable(1);
        defTable.setWidthPercentage(100);
        PdfPCell hDef = new PdfPCell(new Phrase(" RELATO DO PROBLEMA E DIAGNÓSTICO TÉCNICO", fontSecao));
        hDef.setBackgroundColor(new Color(52, 73, 94));
        hDef.setPadding(4);
        defTable.addCell(hDef);

        PdfPCell cDef = new PdfPCell();
        cDef.setPadding(6);
        cDef.addElement(new Paragraph("Defeito Relatado: " + os.getProblemaRelatado(), fontTextoBold));
        cDef.addElement(new Paragraph("Diagnóstico Técnico: " + (os.getDiagnosticoTecnico() != null && !os.getDiagnosticoTecnico().isEmpty() ? os.getDiagnosticoTecnico() : "Em análise / Conforme orçamento abaixo"), fontTexto));
        defTable.addCell(cDef);
        document.add(defTable);
        document.add(new Paragraph(" "));

        // 4. Tabela de Serviços
        if (servicos != null && !servicos.isEmpty()) {
            PdfPTable servTable = new PdfPTable(2);
            servTable.setWidthPercentage(100);
            servTable.setWidths(new float[]{80, 20});

            PdfPCell hS = new PdfPCell(new Phrase(" SERVIÇOS / MÃO DE OBRA", fontSecao));
            hS.setBackgroundColor(new Color(41, 128, 185));
            hS.setColspan(2);
            hS.setPadding(4);
            servTable.addCell(hS);

            for (ServicoItem s : servicos) {
                PdfPCell cDesc = new PdfPCell(new Phrase(s.getDescricao(), fontTexto));
                cDesc.setPadding(5);
                PdfPCell cVal = new PdfPCell(new Phrase("R$ " + String.format("%.2f", s.getValor()), fontTextoBold));
                cVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cVal.setPadding(5);
                servTable.addCell(cDesc);
                servTable.addCell(cVal);
            }
            document.add(servTable);
            document.add(new Paragraph(" "));
        }

        // 5. Tabela de Peças
        if (pecas != null && !pecas.isEmpty()) {
            PdfPTable pecaTable = new PdfPTable(4);
            pecaTable.setWidthPercentage(100);
            pecaTable.setWidths(new float[]{55, 15, 15, 15});

            PdfPCell hP = new PdfPCell(new Phrase(" PEÇAS E COMPONENTES APLICADOS", fontSecao));
            hP.setBackgroundColor(new Color(41, 128, 185));
            hP.setColspan(4);
            hP.setPadding(4);
            pecaTable.addCell(hP);

            pecaTable.addCell(new Phrase("Descrição do Componente", fontTextoBold));
            pecaTable.addCell(new Phrase("Qtd.", fontTextoBold));
            pecaTable.addCell(new Phrase("Valor Un.", fontTextoBold));
            pecaTable.addCell(new Phrase("Total", fontTextoBold));

            for (PecaItem p : pecas) {
                PdfPCell cNome = new PdfPCell(new Phrase(p.getNome(), fontTexto));
                PdfPCell cQtd = new PdfPCell(new Phrase(String.valueOf(p.getQuantidade()), fontTexto));
                cQtd.setHorizontalAlignment(Element.ALIGN_CENTER);
                PdfPCell cUn = new PdfPCell(new Phrase("R$ " + String.format("%.2f", p.getValorUnitario()), fontTexto));
                cUn.setHorizontalAlignment(Element.ALIGN_RIGHT);
                PdfPCell cTot = new PdfPCell(new Phrase("R$ " + String.format("%.2f", p.getSubtotal()), fontTextoBold));
                cTot.setHorizontalAlignment(Element.ALIGN_RIGHT);

                cNome.setPadding(5);
                cQtd.setPadding(5);
                cUn.setPadding(5);
                cTot.setPadding(5);

                pecaTable.addCell(cNome);
                pecaTable.addCell(cQtd);
                pecaTable.addCell(cUn);
                pecaTable.addCell(cTot);
            }
            document.add(pecaTable);
            document.add(new Paragraph(" "));
        }

        // 6. Resumo e Valor Total
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setWidths(new float[]{60, 40});

        PdfPCell cTermos = new PdfPCell();
        cTermos.setBorder(Rectangle.NO_BORDER);
        cTermos.addElement(new Paragraph("• Validade da proposta: 10 dias a contar da data de emissão.", fontSubtitulo));
        cTermos.addElement(new Paragraph("• Garantia: 90 dias sobre peças e serviços executados.", fontSubtitulo));
        totalTable.addCell(cTermos);

        PdfPCell cTotalBox = new PdfPCell();
        cTotalBox.setBackgroundColor(new Color(235, 247, 240));
        cTotalBox.setBorderColor(new Color(39, 174, 96));
        cTotalBox.setBorderWidth(2);
        cTotalBox.setPadding(10);
        Paragraph pTotTit = new Paragraph("TOTAL DO ORÇAMENTO", fontTextoBold);
        pTotTit.setAlignment(Element.ALIGN_RIGHT);
        Paragraph pTotVal = new Paragraph("R$ " + String.format("%.2f", os.getValorTotal()), fontTotal);
        pTotVal.setAlignment(Element.ALIGN_RIGHT);
        cTotalBox.addElement(pTotTit);
        cTotalBox.addElement(pTotVal);
        totalTable.addCell(cTotalBox);

        document.add(totalTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // 7. Campo de Assinatura
        Paragraph pAssinatura = new Paragraph("_____________________________________________\nAssinatura de Aprovação do Cliente", fontTexto);
        pAssinatura.setAlignment(Element.ALIGN_CENTER);
        document.add(pAssinatura);

        document.close();
        return arquivoDestino;
    }
}
