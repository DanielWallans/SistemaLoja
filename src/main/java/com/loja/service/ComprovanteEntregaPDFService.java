package com.loja.service;

import com.loja.model.Cliente;
import com.loja.model.Equipamento;
import com.loja.model.HistoricoOS;
import com.loja.model.OrdemServico;
import com.loja.model.PagamentoItem;
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
 * Serviço responsável pela geração do Comprovante de Entrega e Termo de Garantia da OS em PDF.
 * Padrão visual corporativo, monocromático e executivo com validação jurídica de entrega.
 */
public class ComprovanteEntregaPDFService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Paleta de Cores Corporativa
    private static final Color COLOR_TEXT_PRIMARY = new Color(20, 24, 27);
    private static final Color COLOR_TEXT_SECONDARY = new Color(80, 85, 90);
    private static final Color COLOR_BORDER = new Color(180, 185, 190);
    private static final Color COLOR_BORDER_LIGHT = new Color(220, 224, 228);
    private static final Color COLOR_BG_HEADER = new Color(238, 240, 242);
    private static final Color COLOR_BG_ZEBRA = new Color(250, 250, 251);
    private static final Color COLOR_BG_TOTAL = new Color(242, 244, 246);
    private static final Color COLOR_BG_ENTREGUE = new Color(232, 245, 233);
    private static final Color COLOR_TEXT_ENTREGUE = new Color(46, 125, 50);

    // Tipografia Executiva
    private static final Font FONT_EMPRESA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, COLOR_TEXT_PRIMARY);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, COLOR_TEXT_SECONDARY);
    private static final Font FONT_TITULO_DOC = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_TEXT_PRIMARY);
    private static final Font FONT_SUBTITULO_DOC = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_TEXT_PRIMARY);
    private static final Font FONT_HEADER_SECAO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, COLOR_TEXT_PRIMARY);
    private static final Font FONT_TABELA_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_TEXT_PRIMARY);
    private static final Font FONT_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT_PRIMARY);
    private static final Font FONT_TEXTO_MUTED = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, COLOR_TEXT_SECONDARY);
    private static final Font FONT_TEXTO_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_TEXT_PRIMARY);
    private static final Font FONT_TOTAL_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_TEXT_PRIMARY);
    private static final Font FONT_TOTAL_VALOR = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COLOR_TEXT_PRIMARY);
    private static final Font FONT_TERMO_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_TEXT_PRIMARY);
    private static final Font FONT_TERMO_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 8.2f, COLOR_TEXT_PRIMARY);
    private static final Font FONT_RODAPE = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXT_SECONDARY);

    public static File gerarComprovanteEntregaPDF(File arquivoDestino, OrdemServico os, Cliente cliente, Equipamento equip,
                                                  List<ServicoItem> servicos, List<PecaItem> pecas,
                                                  List<HistoricoOS> historico) throws Exception {
        Document document = new Document(PageSize.A4, 32, 32, 32, 32);
        PdfWriter.getInstance(document, new FileOutputStream(arquivoDestino));
        document.open();

        // 1. Cabeçalho Principal (Empresa e Identificação do Comprovante de Entrega)
        adicionarCabecalho(document, os);

        // Linha divisória sutil
        adicionarDivisor(document);

        // 2. Dados do Cliente e Dados do Equipamento
        adicionarDadosClienteEquipamento(document, cliente, equip);

        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));

        // 3. Relato do Problema e Diagnóstico / Solução Aplicada
        adicionarDiagnosticoESolucao(document, os);

        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));

        // 4. Tabela de Serviços Executados
        adicionarTabelaServicos(document, servicos);

        // 5. Tabela de Peças / Componentes Aplicados
        adicionarTabelaPecas(document, pecas);

        // 6. Resumo Financeiro, Formas de Pagamento e Quitação
        adicionarResumoFinanceiroEPagamentos(document, os, servicos, pecas);

        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));

        // 7. Termo de Declaração de Retirada e Garantia Legal (CDC)
        adicionarTermoEntregaEGarantia(document);

        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 8)));

        // 8. Área de Assinaturas (Cliente e Loja)
        adicionarAssinaturas(document, cliente);

        // 9. Rodapé de Autenticidade
        adicionarRodape(document);

        document.close();
        return arquivoDestino;
    }

    private static void adicionarCabecalho(Document document, OrdemServico os) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60, 40});
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
        if (!cnpj.isEmpty()) sbInfo.append("CNPJ/CPF: ").append(cnpj);
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

        Paragraph pDocTipo = new Paragraph("Comprovante Oficial de Retirada e Termo de Garantia", FONT_SUBTITULO);
        cellEmpresa.addElement(pDocTipo);
        headerTable.addCell(cellEmpresa);

        // Lado Direito: Caixa de Controle da OS
        PdfPCell cellOSInfo = new PdfPCell();
        cellOSInfo.setBackgroundColor(COLOR_BG_HEADER);
        cellOSInfo.setBorderColor(COLOR_BORDER);
        cellOSInfo.setBorderWidth(1f);
        cellOSInfo.setPadding(6);

        Paragraph pTitulo = new Paragraph("COMPROVANTE DE ENTREGA", FONT_TITULO_DOC);
        pTitulo.setAlignment(Element.ALIGN_RIGHT);

        Paragraph pOS = new Paragraph("ORDEM DE SERVIÇO Nº #" + os.getId(), FONT_SUBTITULO_DOC);
        pOS.setAlignment(Element.ALIGN_RIGHT);

        String dataEntradaStr = os.getDataEntrada() != null ? os.getDataEntrada().format(FORMATTER) : "-";
        Paragraph pEntrada = new Paragraph("Entrada: " + dataEntradaStr, FONT_TEXTO_MUTED);
        pEntrada.setAlignment(Element.ALIGN_RIGHT);

        LocalDateTime dataEntrega = os.getDataSaida() != null ? os.getDataSaida() : LocalDateTime.now();
        Paragraph pEntrega = new Paragraph("Entrega: " + dataEntrega.format(FORMATTER), FONT_TEXTO_MUTED);
        pEntrega.setAlignment(Element.ALIGN_RIGHT);

        Paragraph pStatus = new Paragraph("STATUS: ENTREGUE (FINALIZADO)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, COLOR_TEXT_ENTREGUE));
        pStatus.setAlignment(Element.ALIGN_RIGHT);

        cellOSInfo.addElement(pTitulo);
        cellOSInfo.addElement(pOS);
        cellOSInfo.addElement(pEntrada);
        cellOSInfo.addElement(pEntrega);
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

        PdfPCell h1 = criarCelulaHeaderSecao("DADOS DO CLIENTE / RECEBEDOR");
        PdfPCell h2 = criarCelulaHeaderSecao("DADOS DO EQUIPAMENTO ENTREGUE");
        table.addCell(h1);
        table.addCell(h2);

        // Cliente
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

        // Equipamento
        PdfPCell cEquip = new PdfPCell();
        cEquip.setBorderColor(COLOR_BORDER_LIGHT);
        cEquip.setBorderWidth(0.8f);
        cEquip.setPadding(7);

        if (equip != null) {
            String aparelho = String.format("%s %s %s", valorOuVazio(equip.getTipo()), valorOuVazio(equip.getMarca()), valorOuVazio(equip.getModelo())).trim();
            cEquip.addElement(new Paragraph("Aparelho: " + (aparelho.isEmpty() ? "Não especificado" : aparelho), FONT_TEXTO_BOLD));
            cEquip.addElement(new Paragraph("Nº de Série: " + valorOuTraco(equip.getNumeroSerie()) + " | Cor: " + valorOuTraco(equip.getCor()), FONT_TEXTO));
            cEquip.addElement(new Paragraph("Acessórios Retirados / Devolvidos: " + valorOuTraco(equip.getAcessorios()), FONT_TEXTO));
            cEquip.addElement(new Paragraph("Condições na Entrada: " + valorOuTraco(equip.getAvarias()), FONT_TEXTO_MUTED));
        } else {
            cEquip.addElement(new Paragraph("Equipamento não identificado", FONT_TEXTO));
        }
        table.addCell(cEquip);

        document.add(table);
    }

    private static void adicionarDiagnosticoESolucao(Document document, OrdemServico os) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell hDef = criarCelulaHeaderSecao("RELATO DO PROBLEMA E SOLUÇÃO TÉCNICA APLICADA");
        table.addCell(hDef);

        PdfPCell cDef = new PdfPCell();
        cDef.setBorderColor(COLOR_BORDER_LIGHT);
        cDef.setBorderWidth(0.8f);
        cDef.setPadding(7);

        Paragraph pProb = new Paragraph();
        pProb.add(new Chunk("Defeito Relatado na Entrada: ", FONT_TEXTO_BOLD));
        pProb.add(new Chunk(valorOuTraco(os.getProblemaRelatado()), FONT_TEXTO));
        cDef.addElement(pProb);

        String solucao = (os.getDiagnosticoTecnico() != null && !os.getDiagnosticoTecnico().trim().isEmpty())
                ? os.getDiagnosticoTecnico().trim()
                : "Serviços e testes técnicos executados conforme discriminado abaixo.";

        Paragraph pSol = new Paragraph();
        pSol.add(new Chunk("Solução Técnica / Laudo de Saída: ", FONT_TEXTO_BOLD));
        pSol.add(new Chunk(solucao, FONT_TEXTO));
        cDef.addElement(pSol);

        table.addCell(cDef);
        document.add(table);
    }

    private static void adicionarTabelaServicos(Document document, List<ServicoItem> servicos) throws DocumentException {
        if (servicos == null || servicos.isEmpty()) return;

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{82, 18});
        table.setSpacingBefore(2);

        PdfPCell hPrincipal = criarCelulaHeaderSecao("SERVIÇOS EXECUTADOS E MÃO DE OBRA APLICADA");
        hPrincipal.setColspan(2);
        table.addCell(hPrincipal);

        table.addCell(criarCelulaTabelaHeader("Descrição do Serviço Realizado", Element.ALIGN_LEFT));
        table.addCell(criarCelulaTabelaHeader("Valor (R$)", Element.ALIGN_RIGHT));

        boolean zebra = false;
        for (ServicoItem s : servicos) {
            Color bg = zebra ? COLOR_BG_ZEBRA : Color.WHITE;

            PdfPCell cDesc = new PdfPCell(new Phrase(s.getDescricao(), FONT_TEXTO));
            cDesc.setBackgroundColor(bg);
            cDesc.setBorderColor(COLOR_BORDER_LIGHT);
            cDesc.setPadding(4.5f);

            PdfPCell cVal = new PdfPCell(new Phrase(String.format("R$ %.2f", s.getValor()), FONT_TEXTO));
            cVal.setBackgroundColor(bg);
            cVal.setBorderColor(COLOR_BORDER_LIGHT);
            cVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cVal.setPadding(4.5f);

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
        table.setWidths(new float[]{55, 15, 15, 15});
        table.setSpacingBefore(2);

        PdfPCell hPrincipal = criarCelulaHeaderSecao("PEÇAS E COMPONENTES SUBSTITUÍDOS / APLICADOS");
        hPrincipal.setColspan(4);
        table.addCell(hPrincipal);

        table.addCell(criarCelulaTabelaHeader("Componente / Peça", Element.ALIGN_LEFT));
        table.addCell(criarCelulaTabelaHeader("Qtd", Element.ALIGN_CENTER));
        table.addCell(criarCelulaTabelaHeader("Unitário", Element.ALIGN_RIGHT));
        table.addCell(criarCelulaTabelaHeader("Subtotal", Element.ALIGN_RIGHT));

        boolean zebra = false;
        for (PecaItem p : pecas) {
            Color bg = zebra ? COLOR_BG_ZEBRA : Color.WHITE;

            PdfPCell cNome = new PdfPCell(new Phrase(p.getNome(), FONT_TEXTO));
            cNome.setBackgroundColor(bg);
            cNome.setBorderColor(COLOR_BORDER_LIGHT);
            cNome.setPadding(4.5f);

            PdfPCell cQtd = new PdfPCell(new Phrase(String.valueOf(p.getQuantidade()), FONT_TEXTO));
            cQtd.setBackgroundColor(bg);
            cQtd.setBorderColor(COLOR_BORDER_LIGHT);
            cQtd.setHorizontalAlignment(Element.ALIGN_CENTER);
            cQtd.setPadding(4.5f);

            PdfPCell cUnit = new PdfPCell(new Phrase(String.format("R$ %.2f", p.getValorUnitario()), FONT_TEXTO));
            cUnit.setBackgroundColor(bg);
            cUnit.setBorderColor(COLOR_BORDER_LIGHT);
            cUnit.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cUnit.setPadding(4.5f);

            PdfPCell cSub = new PdfPCell(new Phrase(String.format("R$ %.2f", p.getSubtotal()), FONT_TEXTO));
            cSub.setBackgroundColor(bg);
            cSub.setBorderColor(COLOR_BORDER_LIGHT);
            cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cSub.setPadding(4.5f);

            table.addCell(cNome);
            table.addCell(cQtd);
            table.addCell(cUnit);
            table.addCell(cSub);
            zebra = !zebra;
        }

        document.add(table);
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
    }

    private static void adicionarResumoFinanceiroEPagamentos(Document document, OrdemServico os,
                                                            List<ServicoItem> servicos, List<PecaItem> pecas) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{55, 45});

        // Lado Esquerdo: Forma de Pagamento e Quitação
        PdfPCell cPagamentos = new PdfPCell();
        cPagamentos.setBorderColor(COLOR_BORDER_LIGHT);
        cPagamentos.setBorderWidth(0.8f);
        cPagamentos.setPadding(7);

        Paragraph pPagTitulo = new Paragraph("FORMA DE PAGAMENTO / QUITAÇÃO:", FONT_HEADER_SECAO);
        cPagamentos.addElement(pPagTitulo);

        CaixaDAO caixaDAO = new CaixaDAO();
        List<PagamentoItem> pagamentos = caixaDAO.obterPagamentosOS(os.getId());

        if (pagamentos != null && !pagamentos.isEmpty()) {
            for (PagamentoItem pag : pagamentos) {
                String parcStr = (pag.getParcelas() > 1) ? (" em " + pag.getParcelas() + "x") : "";
                Paragraph pItem = new Paragraph("• " + pag.getModalidade() + parcStr + ": R$ " + String.format("%.2f", pag.getValorBruto()), FONT_TEXTO);
                cPagamentos.addElement(pItem);
            }
            Paragraph pQuitado = new Paragraph("STATUS FINANCEIRO: QUITADO / PAGO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, COLOR_TEXT_ENTREGUE));
            pQuitado.setSpacingBefore(3);
            cPagamentos.addElement(pQuitado);
        } else {
            Paragraph pAviso = new Paragraph("• Pagamento liquidado no ato da entrega.", FONT_TEXTO);
            Paragraph pQuitado = new Paragraph("STATUS FINANCEIRO: QUITADO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, COLOR_TEXT_ENTREGUE));
            cPagamentos.addElement(pAviso);
            cPagamentos.addElement(pQuitado);
        }
        table.addCell(cPagamentos);

        // Lado Direito: Quadro Consolidado de Valores
        PdfPCell cTotais = new PdfPCell();
        cTotais.setBackgroundColor(COLOR_BG_TOTAL);
        cTotais.setBorderColor(COLOR_BORDER);
        cTotais.setBorderWidth(1f);
        cTotais.setPadding(7);

        double totalServicos = (servicos != null && !servicos.isEmpty())
                ? servicos.stream().mapToDouble(ServicoItem::getValor).sum()
                : os.getValorServico();
        double totalPecas = (pecas != null && !pecas.isEmpty())
                ? pecas.stream().mapToDouble(PecaItem::getSubtotal).sum()
                : 0.0;
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

        PdfPCell lblTot = new PdfPCell(new Phrase("TOTAL PAGO:", FONT_TOTAL_TITULO));
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

    private static void adicionarTermoEntregaEGarantia(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell hTermo = criarCelulaHeaderSecao("DECLARAÇÃO DE RETIRADA, TESTE E TERMO DE GARANTIA LEGAL");
        table.addCell(hTermo);

        PdfPCell cTermo = new PdfPCell();
        cTermo.setBorderColor(COLOR_BORDER_LIGHT);
        cTermo.setBorderWidth(0.8f);
        cTermo.setPadding(7);

        Paragraph p1 = new Paragraph("1. DECLARAÇÃO DE RETIRADA E TESTE: Declaro para os devidos fins que retirei nesta data o equipamento acima especificado devidamente testado na minha presença, funcionando em perfeitas condições e com todos os seus acessórios informados na entrada.", FONT_TERMO_TEXTO);
        p1.setSpacingAfter(3);

        Paragraph p2 = new Paragraph("2. GARANTIA LEGAL (90 DIAS): Fica assegurado o prazo de garantia legal de 90 (noventa) dias corridos a contar da data de retirada deste documento, exclusivamente sobre os serviços executados e peças aplicadas, nos estritos termos do Art. 26, II da Lei Federal nº 8.078/1990 (Código de Defesa do Consumidor).", FONT_TERMO_TEXTO);
        p2.setSpacingAfter(3);

        Paragraph p3 = new Paragraph("3. EXCLUSÕES DE GARANTIA: A presente garantia perderá validade caso ocorra: rompimento ou violação dos selos de garantia; intervenção ou reparo por terceiros não autorizados; danos decorrentes de quedas, batidas, mau uso, umidade, derramamento de líquidos, sobretensão elétrica ou descargas atmosféricas.", FONT_TERMO_TEXTO);

        cTermo.addElement(p1);
        cTermo.addElement(p2);
        cTermo.addElement(p3);
        table.addCell(cTermo);

        document.add(table);
    }

    private static void adicionarAssinaturas(Document document, Cliente cliente) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{48, 48});
        table.setSpacingBefore(16);

        String nomeCliente = (cliente != null && cliente.getNome() != null) ? cliente.getNome() : "Cliente";

        // Assinatura Cliente
        PdfPCell cAss1 = new PdfPCell();
        cAss1.setBorder(Rectangle.NO_BORDER);
        cAss1.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pLinha1 = new Paragraph("____________________________________________", FONT_TEXTO_MUTED);
        pLinha1.setAlignment(Element.ALIGN_CENTER);
        Paragraph pNome1 = new Paragraph(nomeCliente, FONT_TEXTO_BOLD);
        pNome1.setAlignment(Element.ALIGN_CENTER);
        Paragraph pDoc1 = new Paragraph("CPF/RG: ________________________", FONT_TEXTO_MUTED);
        pDoc1.setAlignment(Element.ALIGN_CENTER);
        Paragraph pSub1 = new Paragraph("Assinatura do Recebedor / Cliente", FONT_TEXTO_MUTED);
        pSub1.setAlignment(Element.ALIGN_CENTER);

        cAss1.addElement(pLinha1);
        cAss1.addElement(pNome1);
        cAss1.addElement(pDoc1);
        cAss1.addElement(pSub1);

        // Assinatura Loja
        PdfPCell cAss2 = new PdfPCell();
        cAss2.setBorder(Rectangle.NO_BORDER);
        cAss2.setHorizontalAlignment(Element.ALIGN_CENTER);

        CaixaDAO caixaDAO = new CaixaDAO();
        String nomeLoja = caixaDAO.obterConfig("nome_loja", "Assistência Técnica");

        Paragraph pLinha2 = new Paragraph("____________________________________________", FONT_TEXTO_MUTED);
        pLinha2.setAlignment(Element.ALIGN_CENTER);
        Paragraph pNome2 = new Paragraph(nomeLoja, FONT_TEXTO_BOLD);
        pNome2.setAlignment(Element.ALIGN_CENTER);
        Paragraph pData2 = new Paragraph("Data de Retirada: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), FONT_TEXTO_MUTED);
        pData2.setAlignment(Element.ALIGN_CENTER);
        Paragraph pSub2 = new Paragraph("Responsável Técnico / Entrega", FONT_TEXTO_MUTED);
        pSub2.setAlignment(Element.ALIGN_CENTER);

        cAss2.addElement(pLinha2);
        cAss2.addElement(pNome2);
        cAss2.addElement(pData2);
        cAss2.addElement(pSub2);

        table.addCell(cAss1);
        table.addCell(cAss2);

        document.add(table);
    }

    private static void adicionarRodape(Document document) throws DocumentException {
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        footer.setSpacingBefore(10);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColor(COLOR_BORDER_LIGHT);
        cell.setBorderWidth(0.5f);
        cell.setPaddingTop(4);

        CaixaDAO caixaDAO = new CaixaDAO();
        String nomeLoja = caixaDAO.obterConfig("nome_loja", "Sistema de Gestão de Ordens de Serviço");
        String agora = LocalDateTime.now().format(FORMATTER);
        Paragraph p = new Paragraph("Comprovante de entrega emitido eletronicamente em " + agora + " • " + nomeLoja, FONT_RODAPE);
        p.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p);

        footer.addCell(cell);
        document.add(footer);
    }

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
