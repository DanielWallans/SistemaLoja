package com.loja.service;

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
}
