package com.loja.service;

import com.loja.model.Cliente;
import com.loja.model.Equipamento;
import com.loja.model.HistoricoOS;
import com.loja.model.OrdemServico;
import com.loja.model.PecaItem;
import com.loja.model.ServicoItem;
import com.loja.repository.CaixaDAO;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável pela geração de Orçamentos e Ordens de Serviço em PDF.
 * Padrão visual corporativo, monocromático e executivo.
 */
public class OrcamentoPDFService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Paleta de Cores Corporativa (Preto, Tons de Cinza e Branco)
    private static final Color COLOR_TEXT_PRIMARY = new Color(20, 24, 27);
    private static final Color COLOR_TEXT_SECONDARY = new Color(80, 85, 90);
    private static final Color COLOR_BORDER = new Color(180, 185, 190);
    private static final Color COLOR_BORDER_LIGHT = new Color(220, 224, 228);
    private static final Color COLOR_BG_HEADER = new Color(238, 240, 242);
    private static final Color COLOR_BG_ZEBRA = new Color(250, 250, 251);
    private static final Color COLOR_BG_TOTAL = new Color(242, 244, 246);

    // Tipografia Executiva
    private static final Font FONT_EMPRESA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, COLOR_TEXT_PRIMARY);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, COLOR_TEXT_SECONDARY);
    private static final Font FONT_TITULO_OS = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COLOR_TEXT_PRIMARY);
    private static final Font FONT_HEADER_SECAO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, COLOR_TEXT_PRIMARY);
    private static final Font FONT_TABELA_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_TEXT_PRIMARY);
    private static final Font FONT_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT_PRIMARY);
    private static final Font FONT_TEXTO_MUTED = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, COLOR_TEXT_SECONDARY);
    private static final Font FONT_TEXTO_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_TEXT_PRIMARY);
    private static final Font FONT_TOTAL_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_TEXT_PRIMARY);
    private static final Font FONT_TOTAL_VALOR = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COLOR_TEXT_PRIMARY);
    private static final Font FONT_RODAPE = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXT_SECONDARY);

    public static File gerarOrcamentoPDF(File arquivoDestino, OrdemServico os, Cliente cliente, Equipamento equip,
                                         List<ServicoItem> servicos, List<PecaItem> pecas) throws Exception {
        return gerarOrcamentoPDF(arquivoDestino, os, cliente, equip, servicos, pecas, null);
    }

    public static File gerarOrcamentoPDF(File arquivoDestino, OrdemServico os, Cliente cliente, Equipamento equip,
                                         List<ServicoItem> servicos, List<PecaItem> pecas, List<HistoricoOS> historico) throws Exception {
        Document document = new Document(PageSize.A4, 32, 32, 32, 32);
        PdfWriter.getInstance(document, new FileOutputStream(arquivoDestino));
        document.open();

        // 1. Cabeçalho Principal (Empresa e Identificação do Documento)
        adicionarCabecalho(document, os);

        // Linha divisória sutil
        adicionarDivisor(document);

        // 2. Dados do Cliente e Dados do Equipamento
        adicionarDadosClienteEquipamento(document, cliente, equip);

        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));

        // 3. Relato do Problema e Diagnóstico Técnico
        adicionarDiagnostico(document, os);

        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));

        // 4. Tabela de Serviços / Mão de Obra
        adicionarTabelaServicos(document, servicos);

        // 5. Tabela de Peças / Componentes
        adicionarTabelaPecas(document, pecas);

        // 6. Linha do Tempo / Histórico de Mudanças de Status (se houver)
        if (historico != null && !historico.isEmpty()) {
            adicionarTimelinePDF(document, historico);
        }

        // 7. Resumo Financeiro, Condições e Valor Total
        adicionarResumoFinanceiroETermos(document, os, servicos, pecas);

        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 8)));

        // 8. Área de Assinaturas
        adicionarAssinaturas(document);

        // 9. Rodapé de Autenticidade
        adicionarRodape(document);

        document.close();
        return arquivoDestino;
    }

    private static void adicionarCabecalho(Document document, OrdemServico os) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{62, 38});
        headerTable.setSpacingAfter(6);

        CaixaDAO caixaDAO = new CaixaDAO();
        String nomeEmpresa = caixaDAO.obterConfig("nome_loja", "ASSISTÊNCIA TÉCNICA ESPECIALIZADA").toUpperCase();
        String cnpj = caixaDAO.obterConfig("cnpj_loja", "");
        String telefone = caixaDAO.obterConfig("telefone_loja", "");
        String email = caixaDAO.obterConfig("email_loja", "");
        String endereco = caixaDAO.obterConfig("endereco_loja", "");

        // Lado Esquerdo: Identificação da Empresa
        PdfPCell cellEmpresa = new PdfPCell();
        cellEmpresa.setBorder(Rectangle.NO_BORDER);
        cellEmpresa.setPadding(0);
        
        Paragraph pNome = new Paragraph(nomeEmpresa, FONT_EMPRESA);
        cellEmpresa.addElement(pNome);

        StringBuilder sbInfo = new StringBuilder();
        if (!cnpj.isEmpty()) {
            sbInfo.append("CNPJ/CPF: ").append(cnpj);
        }
        if (!telefone.isEmpty()) {
            if (sbInfo.length() > 0) sbInfo.append(" • ");
            sbInfo.append("Tel: ").append(telefone);
        }
        if (!email.isEmpty()) {
            if (sbInfo.length() > 0) sbInfo.append(" • ");
            sbInfo.append("E-mail: ").append(email);
        }
        if (sbInfo.length() > 0) {
            Paragraph pInfo = new Paragraph(sbInfo.toString(), FONT_SUBTITULO);
            cellEmpresa.addElement(pInfo);
        }

        if (!endereco.isEmpty()) {
            Paragraph pEnd = new Paragraph(endereco, FONT_SUBTITULO);
            cellEmpresa.addElement(pEnd);
        }

        Paragraph pContato = new Paragraph("Documento Oficial de Orçamento e Ordem de Serviço", FONT_SUBTITULO);
        cellEmpresa.addElement(pContato);
        headerTable.addCell(cellEmpresa);

        // Lado Direito: Caixa de Controle da OS
        PdfPCell cellOSInfo = new PdfPCell();
        cellOSInfo.setBackgroundColor(COLOR_BG_HEADER);
        cellOSInfo.setBorderColor(COLOR_BORDER);
        cellOSInfo.setBorderWidth(1f);
        cellOSInfo.setPadding(6);

        Paragraph pOS = new Paragraph("ORDEM DE SERVIÇO Nº #" + os.getId(), FONT_TITULO_OS);
        pOS.setAlignment(Element.ALIGN_RIGHT);

        String dataEntradaStr = os.getDataEntrada() != null ? os.getDataEntrada().format(FORMATTER) : "-";
        Paragraph pData = new Paragraph("Emissão: " + dataEntradaStr, FONT_TEXTO_MUTED);
        pData.setAlignment(Element.ALIGN_RIGHT);

        Paragraph pStatus = new Paragraph("Status: " + (os.getStatus() != null ? os.getStatus().toUpperCase() : "AGUARDANDO"), FONT_TEXTO_BOLD);
        pStatus.setAlignment(Element.ALIGN_RIGHT);

        cellOSInfo.addElement(pOS);
        cellOSInfo.addElement(pData);
        cellOSInfo.addElement(pStatus);
        headerTable.addCell(cellOSInfo);

        document.add(headerTable);
    }

    private static void adicionarDivisor(Document document) throws DocumentException {
        LineSeparator separator = new LineSeparator();
        separator.setLineColor(COLOR_BORDER_LIGHT);
        separator.setLineWidth(0.8f);
        document.add(new Chunk(separator));
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
    }

    private static void adicionarDadosClienteEquipamento(Document document, Cliente cliente, Equipamento equip) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{50, 50});

        // 1. Cabeçalhos das duas colunas
        PdfPCell h1 = criarCelulaHeaderSecao("DADOS DO CLIENTE");
        PdfPCell h2 = criarCelulaHeaderSecao("DADOS DO EQUIPAMENTO");
        table.addCell(h1);
        table.addCell(h2);

        // 2. Conteúdo Cliente
        PdfPCell cCliente = new PdfPCell();
        cCliente.setBorderColor(COLOR_BORDER_LIGHT);
        cCliente.setBorderWidth(0.8f);
        cCliente.setPadding(7);

        if (cliente != null) {
            cCliente.addElement(new Paragraph("Nome: " + cliente.getNome(), FONT_TEXTO_BOLD));
            cCliente.addElement(new Paragraph("Telefone / WhatsApp: " + valorOuTraco(cliente.getTelefone()), FONT_TEXTO));
            cCliente.addElement(new Paragraph("CPF / CNPJ: " + valorOuTraco(cliente.getCpfCnpj()), FONT_TEXTO));
            cCliente.addElement(new Paragraph("Endereço: " + valorOuTraco(cliente.getEndereco()), FONT_TEXTO));
        } else {
            cCliente.addElement(new Paragraph("Cliente não vinculado / Avulso", FONT_TEXTO));
        }
        table.addCell(cCliente);

        // 3. Conteúdo Equipamento
        PdfPCell cEquip = new PdfPCell();
        cEquip.setBorderColor(COLOR_BORDER_LIGHT);
        cEquip.setBorderWidth(0.8f);
        cEquip.setPadding(7);

        if (equip != null) {
            String aparelho = String.format("%s %s %s", valorOuVazio(equip.getTipo()), valorOuVazio(equip.getMarca()), valorOuVazio(equip.getModelo())).trim();
            cEquip.addElement(new Paragraph("Aparelho: " + (aparelho.isEmpty() ? "Não especificado" : aparelho), FONT_TEXTO_BOLD));
            cEquip.addElement(new Paragraph("Nº de Série: " + valorOuTraco(equip.getNumeroSerie()) + " | Cor: " + valorOuTraco(equip.getCor()), FONT_TEXTO));
            cEquip.addElement(new Paragraph("Avarias de Entrada: " + valorOuTraco(equip.getAvarias()), FONT_TEXTO));
            cEquip.addElement(new Paragraph("Acessórios Deixados: " + valorOuTraco(equip.getAcessorios()), FONT_TEXTO));
        } else {
            cEquip.addElement(new Paragraph("Equipamento não identificado", FONT_TEXTO));
        }
        table.addCell(cEquip);

        document.add(table);
    }

    private static void adicionarDiagnostico(Document document, OrdemServico os) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell hDef = criarCelulaHeaderSecao("RELATO DO PROBLEMA E DIAGNÓSTICO TÉCNICO");
        table.addCell(hDef);

        PdfPCell cDef = new PdfPCell();
        cDef.setBorderColor(COLOR_BORDER_LIGHT);
        cDef.setBorderWidth(0.8f);
        cDef.setPadding(7);

        Paragraph pProb = new Paragraph();
        pProb.add(new Chunk("Defeito Relatado: ", FONT_TEXTO_BOLD));
        pProb.add(new Chunk(valorOuTraco(os.getProblemaRelatado()), FONT_TEXTO));
        cDef.addElement(pProb);

        String diag = (os.getDiagnosticoTecnico() != null && !os.getDiagnosticoTecnico().trim().isEmpty())
                ? os.getDiagnosticoTecnico().trim()
                : "Em análise técnica / Conforme detalhamento de itens e serviços abaixo.";
        Paragraph pDiag = new Paragraph();
        pDiag.add(new Chunk("Diagnóstico Técnico: ", FONT_TEXTO_BOLD));
        pDiag.add(new Chunk(diag, FONT_TEXTO));
        cDef.addElement(pDiag);

        table.addCell(cDef);
        document.add(table);
    }

    private static void adicionarTabelaServicos(Document document, List<ServicoItem> servicos) throws DocumentException {
        if (servicos == null || servicos.isEmpty()) return;

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{82, 18});

        PdfPCell hPrincipal = criarCelulaHeaderSecao("1. SERVIÇOS E MÃO DE OBRA EXECUTADA");
        hPrincipal.setColspan(2);
        table.addCell(hPrincipal);

        table.addCell(criarCelulaTabelaHeader("Descrição do Serviço / Tarefa Realizada", Element.ALIGN_LEFT));
        table.addCell(criarCelulaTabelaHeader("Valor (R$)", Element.ALIGN_RIGHT));

        boolean zebra = false;
        for (ServicoItem s : servicos) {
            Color bg = zebra ? COLOR_BG_ZEBRA : Color.WHITE;

            PdfPCell cDesc = new PdfPCell(new Phrase(s.getDescricao(), FONT_TEXTO));
            cDesc.setBackgroundColor(bg);
            cDesc.setBorderColor(COLOR_BORDER_LIGHT);
            cDesc.setPadding(5);

            PdfPCell cVal = new PdfPCell(new Phrase(String.format("%.2f", s.getValor()), FONT_TEXTO_BOLD));
            cVal.setBackgroundColor(bg);
            cVal.setBorderColor(COLOR_BORDER_LIGHT);
            cVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cVal.setPadding(5);

            table.addCell(cDesc);
            table.addCell(cVal);
            zebra = !zebra;
        }

        document.add(table);
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
    }

    private static void adicionarTabelaPecas(Document document, List<PecaItem> pecas) throws DocumentException {
        if (pecas == null || pecas.isEmpty()) return;

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{55, 12, 16, 17});

        PdfPCell hPrincipal = criarCelulaHeaderSecao("2. PEÇAS E COMPONENTES APLICADOS");
        hPrincipal.setColspan(4);
        table.addCell(hPrincipal);

        table.addCell(criarCelulaTabelaHeader("Descrição da Peça / Componente", Element.ALIGN_LEFT));
        table.addCell(criarCelulaTabelaHeader("Qtd.", Element.ALIGN_CENTER));
        table.addCell(criarCelulaTabelaHeader("Valor Unit. (R$)", Element.ALIGN_RIGHT));
        table.addCell(criarCelulaTabelaHeader("Subtotal (R$)", Element.ALIGN_RIGHT));

        boolean zebra = false;
        for (PecaItem p : pecas) {
            Color bg = zebra ? COLOR_BG_ZEBRA : Color.WHITE;

            PdfPCell cNome = new PdfPCell(new Phrase(p.getNome(), FONT_TEXTO));
            cNome.setBackgroundColor(bg);
            cNome.setBorderColor(COLOR_BORDER_LIGHT);
            cNome.setPadding(5);

            PdfPCell cQtd = new PdfPCell(new Phrase(String.valueOf(p.getQuantidade()), FONT_TEXTO));
            cQtd.setBackgroundColor(bg);
            cQtd.setBorderColor(COLOR_BORDER_LIGHT);
            cQtd.setHorizontalAlignment(Element.ALIGN_CENTER);
            cQtd.setPadding(5);

            PdfPCell cUn = new PdfPCell(new Phrase(String.format("%.2f", p.getValorUnitario()), FONT_TEXTO));
            cUn.setBackgroundColor(bg);
            cUn.setBorderColor(COLOR_BORDER_LIGHT);
            cUn.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cUn.setPadding(5);

            PdfPCell cTot = new PdfPCell(new Phrase(String.format("%.2f", p.getSubtotal()), FONT_TEXTO_BOLD));
            cTot.setBackgroundColor(bg);
            cTot.setBorderColor(COLOR_BORDER_LIGHT);
            cTot.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cTot.setPadding(5);

            table.addCell(cNome);
            table.addCell(cQtd);
            table.addCell(cUn);
            table.addCell(cTot);
            zebra = !zebra;
        }

        document.add(table);
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
    }

    private static void adicionarResumoFinanceiroETermos(Document document, OrdemServico os,
                                                         List<ServicoItem> servicos, List<PecaItem> pecas) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{58, 42});

        // Lado Esquerdo: Termos, Prazos e Garantia
        PdfPCell cTermos = new PdfPCell();
        cTermos.setBorderColor(COLOR_BORDER_LIGHT);
        cTermos.setBorderWidth(0.8f);
        cTermos.setPadding(7);

        Paragraph pTitTermos = new Paragraph("CONDIÇÕES GERAIS E GARANTIA", FONT_TEXTO_BOLD);
        cTermos.addElement(pTitTermos);

        CaixaDAO caixaDAO = new CaixaDAO();
        String termosCustom = caixaDAO.obterConfig("termos_garantia_pdf", "").trim();
        if (!termosCustom.isEmpty()) {
            for (String linha : termosCustom.split("\n")) {
                if (!linha.trim().isEmpty()) {
                    cTermos.addElement(new Paragraph("• " + linha.trim(), FONT_TEXTO_MUTED));
                }
            }
        } else {
            Paragraph pT1 = new Paragraph("• Validade deste orçamento: 10 (dez) dias a contar da data de emissão.", FONT_TEXTO_MUTED);
            Paragraph pT2 = new Paragraph("• Garantia: 90 (noventa) dias sobre serviços e peças aplicadas (Art. 26 CDC).", FONT_TEXTO_MUTED);
            Paragraph pT3 = new Paragraph("• Equipamentos não retirados em até 90 dias após notificação estarão sujeitos a taxa de guarda.", FONT_TEXTO_MUTED);
            cTermos.addElement(pT1);
            cTermos.addElement(pT2);
            cTermos.addElement(pT3);
        }
        table.addCell(cTermos);

        // Lado Direito: Quadro Consolidado de Valores
        PdfPCell cTotais = new PdfPCell();
        cTotais.setBackgroundColor(COLOR_BG_TOTAL);
        cTotais.setBorderColor(COLOR_BORDER);
        cTotais.setBorderWidth(1f);
        cTotais.setPadding(7);

        double totalServicos = (servicos != null) ? servicos.stream().mapToDouble(ServicoItem::getValor).sum() : os.getValorServico();
        double totalPecas = (pecas != null) ? pecas.stream().mapToDouble(PecaItem::getSubtotal).sum() : 0.0;
        double valorTotalFinal = (os.getValorTotal() > 0) ? os.getValorTotal() : (totalServicos + totalPecas);

        PdfPTable subTotalTable = new PdfPTable(2);
        subTotalTable.setWidthPercentage(100);
        subTotalTable.setWidths(new float[]{60, 40});

        adicionarLinhaSubtotal(subTotalTable, "Subtotal Serviços:", String.format("R$ %.2f", totalServicos));
        adicionarLinhaSubtotal(subTotalTable, "Subtotal Peças:", String.format("R$ %.2f", totalPecas));

        PdfPCell divisorTot = new PdfPCell();
        divisorTot.setColspan(2);
        divisorTot.setBorder(Rectangle.TOP);
        divisorTot.setBorderColor(COLOR_BORDER);
        divisorTot.setBorderWidth(1f);
        divisorTot.setPaddingTop(3);
        subTotalTable.addCell(divisorTot);

        PdfPCell lblTot = new PdfPCell(new Phrase("TOTAL GERAL:", FONT_TOTAL_TITULO));
        lblTot.setBorder(Rectangle.NO_BORDER);
        lblTot.setVerticalAlignment(Element.ALIGN_MIDDLE);
        
        PdfPCell valTot = new PdfPCell(new Phrase(String.format("R$ %.2f", valorTotalFinal), FONT_TOTAL_VALOR));
        valTot.setBorder(Rectangle.NO_BORDER);
        valTot.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valTot.setVerticalAlignment(Element.ALIGN_MIDDLE);

        subTotalTable.addCell(lblTot);
        subTotalTable.addCell(valTot);

        cTotais.addElement(subTotalTable);
        table.addCell(cTotais);

        document.add(table);
    }

    private static void adicionarLinhaSubtotal(PdfPTable table, String label, String valor) {
        PdfPCell cLbl = new PdfPCell(new Phrase(label, FONT_TEXTO_MUTED));
        cLbl.setBorder(Rectangle.NO_BORDER);
        cLbl.setPaddingBottom(3);

        PdfPCell cVal = new PdfPCell(new Phrase(valor, FONT_TEXTO));
        cVal.setBorder(Rectangle.NO_BORDER);
        cVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cVal.setPaddingBottom(3);

        table.addCell(cLbl);
        table.addCell(cVal);
    }

    private static void adicionarAssinaturas(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{48, 48});
        table.setSpacingBefore(18);

        PdfPCell cAss1 = new PdfPCell();
        cAss1.setBorder(Rectangle.NO_BORDER);
        cAss1.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pLinha1 = new Paragraph("____________________________________________", FONT_TEXTO_MUTED);
        pLinha1.setAlignment(Element.ALIGN_CENTER);
        Paragraph pNome1 = new Paragraph("Assinatura do Cliente / De Acordo", FONT_TEXTO_BOLD);
        pNome1.setAlignment(Element.ALIGN_CENTER);
        Paragraph pData1 = new Paragraph("Data: ____ / ____ / ________", FONT_TEXTO_MUTED);
        pData1.setAlignment(Element.ALIGN_CENTER);

        cAss1.addElement(pLinha1);
        cAss1.addElement(pNome1);
        cAss1.addElement(pData1);

        PdfPCell cAss2 = new PdfPCell();
        cAss2.setBorder(Rectangle.NO_BORDER);
        cAss2.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pLinha2 = new Paragraph("____________________________________________", FONT_TEXTO_MUTED);
        pLinha2.setAlignment(Element.ALIGN_CENTER);
        Paragraph pNome2 = new Paragraph("Responsável Técnico / Loja", FONT_TEXTO_BOLD);
        pNome2.setAlignment(Element.ALIGN_CENTER);
        Paragraph pData2 = new Paragraph("Data: ____ / ____ / ________", FONT_TEXTO_MUTED);
        pData2.setAlignment(Element.ALIGN_CENTER);

        cAss2.addElement(pLinha2);
        cAss2.addElement(pNome2);
        cAss2.addElement(pData2);

        table.addCell(cAss1);
        table.addCell(cAss2);

        document.add(table);
    }

    private static void adicionarRodape(Document document) throws DocumentException {
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        footer.setSpacingBefore(12);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColor(COLOR_BORDER_LIGHT);
        cell.setBorderWidth(0.5f);
        cell.setPaddingTop(4);

        CaixaDAO caixaDAO = new CaixaDAO();
        String nomeLoja = caixaDAO.obterConfig("nome_loja", "Sistema de Gestão de Ordens de Serviço");
        String agora = LocalDateTime.now().format(FORMATTER);
        Paragraph p = new Paragraph("Documento emitido eletronicamente em " + agora + " • " + nomeLoja, FONT_RODAPE);
        p.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p);

        footer.addCell(cell);
        document.add(footer);
    }

    // --- Métodos Utilitários de Estilização ---

    private static PdfPCell criarCelulaHeaderSecao(String titulo) {
        PdfPCell cell = new PdfPCell(new Phrase(titulo, FONT_HEADER_SECAO));
        cell.setBackgroundColor(COLOR_BG_HEADER);
        cell.setBorderColor(COLOR_BORDER);
        cell.setBorderWidth(0.8f);
        cell.setPadding(4.5f);
        return cell;
    }

    private static PdfPCell criarCelulaTabelaHeader(String texto, int alinhamento) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_TABELA_HEADER));
        cell.setBackgroundColor(COLOR_BG_HEADER);
        cell.setBorderColor(COLOR_BORDER_LIGHT);
        cell.setHorizontalAlignment(alinhamento);
        cell.setPadding(4.5f);
        return cell;
    }

    private static void adicionarTimelinePDF(Document document, List<HistoricoOS> historico) throws DocumentException {
        if (historico == null || historico.isEmpty()) return;

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{25, 30, 45});

        PdfPCell hPrincipal = criarCelulaHeaderSecao("HISTÓRICO / LINHA DO TEMPO DA OS");
        hPrincipal.setColspan(3);
        table.addCell(hPrincipal);

        table.addCell(criarCelulaTabelaHeader("Data e Horário", Element.ALIGN_LEFT));
        table.addCell(criarCelulaTabelaHeader("Status Registrado", Element.ALIGN_LEFT));
        table.addCell(criarCelulaTabelaHeader("Observação / Evento", Element.ALIGN_LEFT));

        boolean zebra = false;
        for (HistoricoOS h : historico) {
            Color bg = zebra ? COLOR_BG_ZEBRA : Color.WHITE;

            PdfPCell cData = new PdfPCell(new Phrase(h.getDataHoraFormatada(), FONT_TEXTO));
            cData.setBackgroundColor(bg);
            cData.setBorderColor(COLOR_BORDER_LIGHT);
            cData.setPadding(4.5f);

            PdfPCell cStatus = new PdfPCell(new Phrase(h.getStatus(), FONT_TEXTO_BOLD));
            cStatus.setBackgroundColor(bg);
            cStatus.setBorderColor(COLOR_BORDER_LIGHT);
            cStatus.setPadding(4.5f);

            String obs = (h.getObservacao() != null && !h.getObservacao().trim().isEmpty()) ? h.getObservacao().trim() : "-";
            PdfPCell cObs = new PdfPCell(new Phrase(obs, FONT_TEXTO_MUTED));
            cObs.setBackgroundColor(bg);
            cObs.setBorderColor(COLOR_BORDER_LIGHT);
            cObs.setPadding(4.5f);

            table.addCell(cData);
            table.addCell(cStatus);
            table.addCell(cObs);
            zebra = !zebra;
        }

        document.add(table);
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
    }

    private static String valorOuTraco(String val) {
        if (val == null || val.trim().isEmpty() || "null".equalsIgnoreCase(val.trim())) {
            return "-";
        }
        return val.trim();
    }

    private static String valorOuVazio(String val) {
        if (val == null || "null".equalsIgnoreCase(val.trim())) {
            return "";
        }
        return val.trim();
    }
}
