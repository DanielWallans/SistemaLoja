package com.loja.service;

import com.loja.model.CaixaSessao;
import com.loja.model.Cliente;
import com.loja.model.Equipamento;
import com.loja.model.OrdemServico;
import com.loja.model.PecaItem;
import com.loja.model.ServicoItem;

import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WhatsAppService {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static boolean enviarOrcamentoWhatsApp(OrdemServico os, Cliente cliente, Equipamento equip, 
                                                  List<ServicoItem> servicos, List<PecaItem> pecas) {
        try {
            String telefoneLimpo = "";
            if (cliente != null && cliente.getTelefone() != null) {
                telefoneLimpo = cliente.getTelefone().replaceAll("[^0-9]", "");
            }

            if (telefoneLimpo.length() == 10 || telefoneLimpo.length() == 11) {
                telefoneLimpo = "55" + telefoneLimpo;
            }

            if (telefoneLimpo.isEmpty()) {
                String input = JOptionPane.showInputDialog(null, 
                        "O cliente não possui um número de WhatsApp válido cadastrado.\n" +
                        "Informe o telefone com DDD (ex: 11999998888) ou clique OK para abrir o WhatsApp Web:", 
                        "WhatsApp do Cliente", JOptionPane.QUESTION_MESSAGE);
                if (input != null && !input.trim().isEmpty()) {
                    telefoneLimpo = input.replaceAll("[^0-9]", "");
                    if (telefoneLimpo.length() == 10 || telefoneLimpo.length() == 11) {
                        telefoneLimpo = "55" + telefoneLimpo;
                    }
                }
            }

            LocalDateTime dataEntrada = (os != null && os.getDataEntrada() != null) ? os.getDataEntrada() : LocalDateTime.now();
            String nomeCliente = (cliente != null && cliente.getNome() != null && !cliente.getNome().isEmpty()) ? cliente.getNome() : "Cliente";
            int osId = os != null ? os.getId() : 0;
            double valorTotal = os != null ? os.getValorTotal() : 0.0;
            String problema = (os != null && os.getProblemaRelatado() != null) ? os.getProblemaRelatado() : "Não especificado";

            StringBuilder msg = new StringBuilder();
            msg.append("Olá, *").append(nomeCliente).append("*! Tudo bem?\n\n");
            msg.append("Segue o *Orçamento Técnico* da sua Ordem de Serviço:\n");
            msg.append("📋 *OS Nº:* #").append(osId).append("\n");
            msg.append("📅 *Data:* ").append(dataEntrada.format(formatter)).append("\n");
            if (equip != null) {
                String tipo = equip.getTipo() != null ? equip.getTipo() : "";
                String marca = equip.getMarca() != null ? equip.getMarca() : "";
                String modelo = equip.getModelo() != null ? equip.getModelo() : "";
                String aparelhoStr = (tipo + " " + marca + " " + modelo).trim();
                if (!aparelhoStr.isEmpty()) {
                    msg.append("💻 *Aparelho:* ").append(aparelhoStr).append("\n");
                }
                if (equip.getNumeroSerie() != null && !equip.getNumeroSerie().isEmpty()) {
                    msg.append("🔍 *N/S:* ").append(equip.getNumeroSerie()).append("\n");
                }
            }
            msg.append("⚠️ *Defeito:* ").append(problema).append("\n");
            if (os != null && os.getDiagnosticoTecnico() != null && !os.getDiagnosticoTecnico().trim().isEmpty()) {
                msg.append("🔬 *Diagnóstico:* ").append(os.getDiagnosticoTecnico().trim()).append("\n");
            }
            msg.append("\n────────────────────\n");

            if (servicos != null && !servicos.isEmpty()) {
                msg.append("🛠️ *SERVIÇOS / MÃO DE OBRA:*\n");
                for (ServicoItem s : servicos) {
                    msg.append(" • ").append(s.getDescricao()).append(" - R$ ").append(String.format("%.2f", s.getValor())).append("\n");
                }
                msg.append("\n");
            }

            if (pecas != null && !pecas.isEmpty()) {
                msg.append("📦 *PEÇAS / COMPONENTES:*\n");
                for (PecaItem p : pecas) {
                    msg.append(" • ").append(p.getQuantidade()).append("x ").append(p.getNome())
                       .append(" (R$ ").append(String.format("%.2f", p.getValorUnitario())).append(" un.) = R$ ")
                       .append(String.format("%.2f", p.getSubtotal())).append("\n");
                }
                msg.append("\n");
            }

            msg.append("────────────────────\n");
            msg.append("💰 *VALOR TOTAL DO ORÇAMENTO: R$ ").append(String.format("%.2f", valorTotal)).append("*\n");
            msg.append("⏱️ *Garantia:* 90 dias sobre serviços e peças aplicadas.\n\n");
            msg.append("Podemos aprovar o serviço para darmos início à manutenção?");

            // Copia o texto para a Área de Transferência (Clipboard)
            try {
                StringSelection selection = new StringSelection(msg.toString());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            } catch (Exception ignored) {}

            String encodedMsg = URLEncoder.encode(msg.toString(), StandardCharsets.UTF_8).replace("+", "%20");
            String url = !telefoneLimpo.isEmpty() ? 
                    ("https://api.whatsapp.com/send?phone=" + telefoneLimpo + "&text=" + encodedMsg) : 
                    ("https://api.whatsapp.com/send?text=" + encodedMsg);

            return abrirLinkNavegador(url);
        } catch (Exception ex) {
            System.err.println("[ERRO WHATSAPP] Falha ao abrir link do WhatsApp: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean enviarResumoFechamentoWhatsApp(CaixaSessao sessao, String telefoneDono, String nomeLoja) {
        try {
            String telefoneLimpo = "";
            if (telefoneDono != null) {
                telefoneLimpo = telefoneDono.replaceAll("[^0-9]", "");
            }

            if (telefoneLimpo.length() == 10 || telefoneLimpo.length() == 11) {
                telefoneLimpo = "55" + telefoneLimpo;
            }

            String dataRef = (sessao != null && sessao.getDataFechamento() != null) ? sessao.getDataFechamento().format(formatterData) : 
                            ((sessao != null && sessao.getDataAbertura() != null) ? sessao.getDataAbertura().format(formatterData) : "Hoje");

            StringBuilder msg = new StringBuilder();
            msg.append("📊 *RESUMO DE FECHAMENTO DE CAIXA*\n");
            msg.append("🏪 *Loja:* ").append(nomeLoja != null && !nomeLoja.isEmpty() ? nomeLoja : "Assistência Técnica Pro").append("\n");
            msg.append("📅 *Data / Turno:* ").append(dataRef).append(" (Turno #").append(sessao != null ? sessao.getId() : 1).append(")\n");
            msg.append("─────────────────────────\n");
            if (sessao != null) {
                msg.append("💰 *Faturamento Total Bruto:* R$ ").append(String.format("%.2f", sessao.getTotalVendasBruto())).append("\n");
                msg.append("💵 *Dinheiro:* R$ ").append(String.format("%.2f", sessao.getTotalDinheiro())).append("\n");
                msg.append("📱 *PIX:* R$ ").append(String.format("%.2f", sessao.getTotalPix())).append("\n");
                msg.append("💳 *Débito:* R$ ").append(String.format("%.2f", sessao.getTotalDebitoBruto()))
                   .append(" _(Líq: R$ ").append(String.format("%.2f", sessao.getTotalDebitoLiquido())).append(")_\n");
                msg.append("💳 *Crédito:* R$ ").append(String.format("%.2f", sessao.getTotalCreditoBruto()))
                   .append(" _(Líq: R$ ").append(String.format("%.2f", sessao.getTotalCreditoLiquido())).append(")_\n");
                msg.append("✨ *Faturamento Líquido Real:* R$ ").append(String.format("%.2f", sessao.getTotalVendasLiquido())).append("\n");
                msg.append("─────────────────────────\n");
                msg.append("📉 *Sangrias / Retiradas:* R$ ").append(String.format("%.2f", sessao.getTotalSangrias())).append("\n");
                msg.append("🟢 *Suprimentos / Reforço:* R$ ").append(String.format("%.2f", sessao.getTotalSuprimentos())).append("\n");
                msg.append("💰 *Saldo Final em Gaveta:* R$ ").append(String.format("%.2f", sessao.getSaldoFinalInformado())).append("\n");
                if (sessao.getSangriaMalote() > 0) {
                    msg.append("💼 *Sangria / Malote para Cofre:* R$ ").append(String.format("%.2f", sessao.getSangriaMalote())).append("\n");
                }
                msg.append("⚖️ *Conferência Física:* ").append(sessao.getStatusDiferencaFormatado()).append("\n");

                if (sessao.getJustificativaDiferenca() != null && !sessao.getJustificativaDiferenca().trim().isEmpty()) {
                    msg.append("⚠️ *Justificativa Quebra/Sobra:* ").append(sessao.getJustificativaDiferenca().trim()).append("\n");
                }

                msg.append("─────────────────────────\n");
                msg.append("✅ *Fechado por:* ").append(sessao.getOperadorFechamento() != null ? sessao.getOperadorFechamento() : sessao.getOperadorAbertura())
                   .append(" às ").append(sessao.getHoraFechamentoFormatada()).append("\n");
            }

            // Copia o texto para a Área de Transferência
            try {
                StringSelection selection = new StringSelection(msg.toString());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            } catch (Exception ignored) {}

            String encodedMsg = URLEncoder.encode(msg.toString(), StandardCharsets.UTF_8).replace("+", "%20");
            String url = !telefoneLimpo.isEmpty() ? 
                    ("https://api.whatsapp.com/send?phone=" + telefoneLimpo + "&text=" + encodedMsg) : 
                    ("https://api.whatsapp.com/send?text=" + encodedMsg);

            return abrirLinkNavegador(url);
        } catch (Exception ex) {
            System.err.println("[ERRO WHATSAPP] Falha ao enviar resumo de fechamento: " + ex.getMessage());
            return false;
        }
    }

    public static boolean abrirLinkNavegador(String url) {
        // 1. Tenta Desktop.getDesktop().browse
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
        } catch (Throwable e) {
            System.err.println("[AVISO] Desktop.browse falhou: " + e.getMessage());
        }

        // 2. Fallback nativo do Windows: rundll32 url.dll,FileProtocolHandler
        try {
            Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            return true;
        } catch (Throwable e) {
            System.err.println("[AVISO] rundll32 falhou: " + e.getMessage());
        }

        // 3. Fallback nativo: cmd.exe /c start "" "<url>"
        try {
            new ProcessBuilder("cmd.exe", "/c", "start", "\"\"", url.replace("&", "^&")).start();
            return true;
        } catch (Throwable e) {
            System.err.println("[ERRO] Falha geral ao abrir navegador: " + e.getMessage());
        }

        return false;
    }
}
