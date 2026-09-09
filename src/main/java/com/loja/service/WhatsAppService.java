package com.loja.service;

import com.loja.model.CaixaSessao;
import com.loja.model.Cliente;
import com.loja.model.Equipamento;
import com.loja.model.OrdemServico;
import com.loja.model.PecaItem;
import com.loja.model.ServicoItem;

import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

            // Adicionar 55 (Brasil) se não tiver código de país
            if (telefoneLimpo.length() == 10 || telefoneLimpo.length() == 11) {
                telefoneLimpo = "55" + telefoneLimpo;
            }

            StringBuilder msg = new StringBuilder();
            msg.append("Olá, *").append(cliente != null ? cliente.getNome() : "Cliente").append("*! Tudo bem?\n\n");
            msg.append("Segue o *Orçamento Técnico* da sua Ordem de Serviço:\n");
            msg.append("📋 *OS Nº:* #").append(os.getId()).append("\n");
            msg.append("📅 *Data:* ").append(os.getDataEntrada().format(formatter)).append("\n");
            if (equip != null) {
                msg.append("💻 *Aparelho:* ").append(equip.getTipo()).append(" ").append(equip.getMarca()).append(" ").append(equip.getModelo()).append("\n");
                msg.append("🔍 *N/S:* ").append(equip.getNumeroSerie()).append("\n");
            }
            msg.append("⚠️ *Defeito:* ").append(os.getProblemaRelatado()).append("\n");
            if (os.getDiagnosticoTecnico() != null && !os.getDiagnosticoTecnico().trim().isEmpty()) {
                msg.append("🔬 *Diagnóstico:* ").append(os.getDiagnosticoTecnico()).append("\n");
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
            msg.append("💰 *VALOR TOTAL DO ORÇAMENTO: R$ ").append(String.format("%.2f", os.getValorTotal())).append("*\n");
            msg.append("⏱️ *Garantia:* 90 dias sobre serviços e peças aplicadas.\n\n");
            msg.append("Podemos aprovar o serviço para darmos início à manutenção?");

            String encodedMsg = URLEncoder.encode(msg.toString(), StandardCharsets.UTF_8);
            String url = "https://wa.me/" + telefoneLimpo + "?text=" + encodedMsg;

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
            return false;
        } catch (Exception ex) {
            System.err.println("[ERRO WHATSAPP] Falha ao abrir link do WhatsApp: " + ex.getMessage());
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

            String dataRef = sessao.getDataFechamento() != null ? sessao.getDataFechamento().format(formatterData) : 
                            (sessao.getDataAbertura() != null ? sessao.getDataAbertura().format(formatterData) : "Hoje");

            StringBuilder msg = new StringBuilder();
            msg.append("📊 *RESUMO DE FECHAMENTO DE CAIXA*\n");
            msg.append("🏪 *Loja:* ").append(nomeLoja != null && !nomeLoja.isEmpty() ? nomeLoja : "Assistência Técnica Pro").append("\n");
            msg.append("📅 *Data / Turno:* ").append(dataRef).append(" (Turno #").append(sessao.getId()).append(")\n");
            msg.append("─────────────────────────\n");
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
            msg.append("💼 *Sangria / Malote para Cofre:* R$ ").append(String.format("%.2f", sessao.getSangriaMalote())).append("\n");
            msg.append("🪙 *Fundo Troco Deixado na Gaveta:* R$ ").append(String.format("%.2f", sessao.getFundoTrocoDeixado())).append("\n");
            msg.append("⚖️ *Conferência Física:* ").append(sessao.getStatusDiferencaFormatado()).append("\n");

            if (sessao.getJustificativaDiferenca() != null && !sessao.getJustificativaDiferenca().trim().isEmpty()) {
                msg.append("⚠️ *Justificativa Quebra/Sobra:* ").append(sessao.getJustificativaDiferenca().trim()).append("\n");
            }

            msg.append("─────────────────────────\n");
            msg.append("✅ *Fechado por:* ").append(sessao.getOperadorFechamento() != null ? sessao.getOperadorFechamento() : sessao.getOperadorAbertura())
               .append(" às ").append(sessao.getHoraFechamentoFormatada()).append("\n");

            String encodedMsg = URLEncoder.encode(msg.toString(), StandardCharsets.UTF_8);
            String url = !telefoneLimpo.isEmpty() ? 
                    ("https://wa.me/" + telefoneLimpo + "?text=" + encodedMsg) : 
                    ("https://api.whatsapp.com/send?text=" + encodedMsg);

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
            return false;
        } catch (Exception ex) {
            System.err.println("[ERRO WHATSAPP] Falha ao enviar resumo de fechamento: " + ex.getMessage());
            return false;
        }
    }
}
