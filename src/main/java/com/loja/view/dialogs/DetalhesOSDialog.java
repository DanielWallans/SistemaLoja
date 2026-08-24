package com.loja.view.dialogs;

import com.loja.model.*;
import com.loja.repository.ClienteDAO;
import com.loja.repository.EquipamentoDAO;
import com.loja.repository.OrdemServicoDAO;
import com.loja.repository.ProdutoDAO;
import com.loja.service.CaixaService;
import com.loja.service.OrcamentoPDFService;
import com.loja.service.WhatsAppService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DetalhesOSDialog extends JDialog {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OrdemServico osOriginal;
    private final OrdemServicoDAO osDAO;
    private final ClienteDAO clienteDAO;
    private final EquipamentoDAO equipDAO;
    private final ProdutoDAO produtoDAO;
    private final CaixaService caixaService;
    private boolean alterado = false;

    private JComboBox<String> cbStatus;
    private JTextArea txtDiagnostico;
    private JTextField txtMaoDeObra;
    private DefaultListModel<String> pecasListModel;
    private DefaultListModel<String> servicosListModel;
    private JLabel lblTotal;

    public DetalhesOSDialog(Frame owner, OrdemServico os, OrdemServicoDAO osDAO, ClienteDAO clienteDAO, 
                            EquipamentoDAO equipDAO, ProdutoDAO produtoDAO, CaixaService caixaService) {
        super(owner, "Ordem de Serviço #" + os.getId() + " - Detalhes e Andamento", true);
        this.osOriginal = os;
        this.osDAO = osDAO;
        this.clienteDAO = clienteDAO;
        this.equipDAO = equipDAO;
        this.produtoDAO = produtoDAO;
        this.caixaService = caixaService;

        initComponents();
        setSize(860, 760);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        Cliente cliente = clienteDAO.buscarPorId(osOriginal.getClienteId());
        Equipamento equip = equipDAO.buscarPorId(osOriginal.getEquipamentoId());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // 1. Cabeçalho OS & Cliente
        JPanel pnlHeader = new JPanel(new GridLayout(2, 2, 10, 5));
        pnlHeader.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Informações Gerais ", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));
        
        pnlHeader.add(new JLabel("OS Nº: #" + osOriginal.getId() + "  |  Data Entrada: " + osOriginal.getDataEntrada().format(formatter)));
        pnlHeader.add(new JLabel("Data Saída: " + (osOriginal.getDataSaida() != null ? osOriginal.getDataSaida().format(formatter) : "Em aberto")));
        pnlHeader.add(new JLabel("Cliente: " + (cliente != null ? cliente.getNome() + " (Tel: " + cliente.getTelefone() + ")" : "Não encontrado")));
        pnlHeader.add(new JLabel("Endereço: " + (cliente != null && cliente.getEndereco() != null ? cliente.getEndereco() : "N/A")));
        mainPanel.add(pnlHeader);
        mainPanel.add(Box.createVerticalStrut(8));

        // 2. Dados do Equipamento
        JPanel pnlEquip = new JPanel(new GridLayout(3, 2, 10, 4));
        pnlEquip.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Equipamento em Manutenção ", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));
        if (equip != null) {
            pnlEquip.add(new JLabel("Tipo / Aparelho: " + equip.getTipo() + " " + equip.getMarca() + " " + equip.getModelo()));
            pnlEquip.add(new JLabel("Nº de Série: " + equip.getNumeroSerie() + "  |  Cor: " + equip.getCor()));
            pnlEquip.add(new JLabel("Avarias Visíveis: " + equip.getAvarias()));
            pnlEquip.add(new JLabel("Senha de Teste: " + equip.getSenhaAcesso()));
            pnlEquip.add(new JLabel("Acessórios Deixados: " + equip.getAcessorios()));
            pnlEquip.add(new JLabel("Patrimônio: " + equip.getPatrimonio()));
        } else {
            pnlEquip.add(new JLabel("Equipamento não localizado."));
        }
        mainPanel.add(pnlEquip);
        mainPanel.add(Box.createVerticalStrut(8));

        // 3. Checklist e Defeito
        JPanel pnlCheckDefeito = new JPanel(new GridLayout(2, 1, 6, 6));
        pnlCheckDefeito.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Checklist & Defeito Relatado ", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));
        
        JTextArea txtCheck = new JTextArea(osOriginal.getChecklistEntrada() != null ? osOriginal.getChecklistEntrada() : "Nenhum checklist registrado.", 2, 40);
        txtCheck.setEditable(false);
        pnlCheckDefeito.add(new JScrollPane(txtCheck));

        JTextArea txtDefeito = new JTextArea("Defeito Relatado: " + osOriginal.getProblemaRelatado(), 2, 40);
        txtDefeito.setEditable(false);
        pnlCheckDefeito.add(new JScrollPane(txtDefeito));
        mainPanel.add(pnlCheckDefeito);
        mainPanel.add(Box.createVerticalStrut(8));

        // 4. Serviços & Peças
        JPanel pnlItens = new JPanel(new GridLayout(1, 2, 10, 0));

        // Serviços
        JPanel pnlServicos = new JPanel(new BorderLayout(5, 5));
        pnlServicos.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Serviços / Mão de Obra ", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 11)));
        servicosListModel = new DefaultListModel<>();
        JList<String> listServicos = new JList<>(servicosListModel);
        pnlServicos.add(new JScrollPane(listServicos), BorderLayout.CENTER);

        // Peças
        JPanel pnlPecas = new JPanel(new BorderLayout(5, 5));
        pnlPecas.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Peças / Componentes ", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 11)));
        pecasListModel = new DefaultListModel<>();
        JList<String> listPecas = new JList<>(pecasListModel);
        pnlPecas.add(new JScrollPane(listPecas), BorderLayout.CENTER);

        pnlItens.add(pnlServicos);
        pnlItens.add(pnlPecas);
        pnlItens.setPreferredSize(new Dimension(800, 110));
        mainPanel.add(pnlItens);
        mainPanel.add(Box.createVerticalStrut(8));

        // 5. Botão de Destaque: Abrir Ambiente do Técnico
        JPanel pnlBtnOrcamento = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        JButton btnAbrirAmbienteOrcamento = new JButton("🛠️ ABRIR AMBIENTE TÉCNICO DE MONTAGEM DE ORÇAMENTO");
        btnAbrirAmbienteOrcamento.setFont(btnAbrirAmbienteOrcamento.getFont().deriveFont(Font.BOLD, 13f));
        btnAbrirAmbienteOrcamento.setPreferredSize(new Dimension(550, 36));
        btnAbrirAmbienteOrcamento.addActionListener(e -> abrirMontagemOrcamento());
        pnlBtnOrcamento.add(btnAbrirAmbienteOrcamento);
        mainPanel.add(pnlBtnOrcamento);
        mainPanel.add(Box.createVerticalStrut(8));

        // 6. Diagnóstico Técnico, Status e Mão de Obra
        JPanel pnlAndamento = new JPanel(new GridBagLayout());
        pnlAndamento.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Status Rápido & Diagnóstico ", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        cbStatus = new JComboBox<>(new String[]{
                "Aguardando Orçamento",
                "Aguardando Aprovação do Cliente",
                "Aprovado - Em Manutenção",
                "Aguardando Peça",
                "Pronto (Aguardando Retirada)",
                "Entregue (Finalizado)",
                "Orçamento Recusado / Cancelada"
        });
        cbStatus.setSelectedItem(osOriginal.getStatus());

        txtMaoDeObra = new JTextField(String.format("%.2f", osOriginal.getValorServico()).replace(",", "."), 10);
        txtDiagnostico = new JTextArea(osOriginal.getDiagnosticoTecnico() != null ? osOriginal.getDiagnosticoTecnico() : "", 2, 30);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        pnlAndamento.add(new JLabel("Status Atual:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3;
        pnlAndamento.add(cbStatus, gbc);
        gbc.gridx = 2; gbc.weightx = 0.2;
        pnlAndamento.add(new JLabel("Mão de Obra (R$):"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.3;
        pnlAndamento.add(txtMaoDeObra, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        pnlAndamento.add(new JLabel("Diagnóstico Técnico:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weightx = 0.8;
        pnlAndamento.add(new JScrollPane(txtDiagnostico), gbc);

        mainPanel.add(pnlAndamento);

        JScrollPane scrollPrincipal = new JScrollPane(mainPanel);
        scrollPrincipal.setBorder(null);
        add(scrollPrincipal, BorderLayout.CENTER);

        // Barra inferior: Total e Ações
        JPanel bottomPanel = new JPanel(new BorderLayout(15, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 12, 16));

        lblTotal = new JLabel("VALOR TOTAL DA OS: R$ " + String.format("%.2f", osOriginal.getValorTotal()));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 15f));
        lblTotal.setForeground(new Color(39, 174, 96));
        bottomPanel.add(lblTotal, BorderLayout.WEST);

        JPanel btnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnWhatsApp = new JButton("📲 WhatsApp");
        JButton btnPDF = new JButton("📄 Gerar PDF");
        JButton btnComprovante = new JButton("🖨️ Comprovante");
        JButton btnSalvarAlteracoes = new JButton("💾 Salvar");
        btnSalvarAlteracoes.setFont(btnSalvarAlteracoes.getFont().deriveFont(Font.BOLD));

        btnWhatsApp.addActionListener(e -> enviarWhatsApp(cliente, equip));
        btnPDF.addActionListener(e -> gerarPDF(cliente, equip));
        btnComprovante.addActionListener(e -> exibirComprovante(cliente, equip));
        btnSalvarAlteracoes.addActionListener(e -> salvarAndamento());

        btnActions.add(btnWhatsApp);
        btnActions.add(btnPDF);
        btnActions.add(btnComprovante);
        btnActions.add(btnSalvarAlteracoes);
        bottomPanel.add(btnActions, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        recarregarItens();
    }

    private void recarregarItens() {
        servicosListModel.clear();
        List<ServicoItem> servicos = osDAO.obterServicosOS(osOriginal.getId());
        if (servicos.isEmpty() && osOriginal.getValorServico() > 0) {
            servicosListModel.addElement("Mão de Obra: R$ " + String.format("%.2f", osOriginal.getValorServico()));
        } else {
            servicos.forEach(s -> servicosListModel.addElement(s.getDescricao() + " (R$ " + String.format("%.2f", s.getValor()) + ")"));
        }

        pecasListModel.clear();
        List<String> pecas = osDAO.obterPecasOS(osOriginal.getId());
        if (pecas.isEmpty()) {
            pecasListModel.addElement("Nenhuma peça vinculada.");
        } else {
            pecas.forEach(pecasListModel::addElement);
        }

        OrdemServico atualizada = osDAO.buscarPorId(osOriginal.getId());
        if (atualizada != null) {
            osOriginal.setStatus(atualizada.getStatus());
            osOriginal.setValorServico(atualizada.getValorServico());
            osOriginal.setValorTotal(atualizada.getValorTotal());
            lblTotal.setText("VALOR TOTAL DA OS: R$ " + String.format("%.2f", atualizada.getValorTotal()));
            cbStatus.setSelectedItem(atualizada.getStatus());
            txtMaoDeObra.setText(String.format("%.2f", atualizada.getValorServico()).replace(",", "."));
        }
    }

    private void abrirMontagemOrcamento() {
        MontarOrcamentoDialog dialog = new MontarOrcamentoDialog((Frame) getOwner(), osOriginal, osDAO, clienteDAO, equipDAO, produtoDAO);
        dialog.setVisible(true);
        if (dialog.isSalvo()) {
            this.alterado = true;
            recarregarItens();
        }
    }

    private void salvarAndamento() {
        String novoStatus = (String) cbStatus.getSelectedItem();
        String diagnostico = txtDiagnostico.getText().trim();
        double maoDeObra;

        try {
            maoDeObra = Double.parseDouble(txtMaoDeObra.getText().trim().replace(",", "."));
            if (maoDeObra < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe um valor numérico válido para a mão de obra!", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtMaoDeObra.requestFocus();
            return;
        }

        boolean mudouParaEntregue = novoStatus != null && novoStatus.contains("Entregue") && (osOriginal.getStatus() == null || !osOriginal.getStatus().contains("Entregue"));

        osDAO.atualizarStatusEServico(osOriginal.getId(), novoStatus, maoDeObra, diagnostico);

        if (mudouParaEntregue) {
            OrdemServico osAtual = osDAO.buscarPorId(osOriginal.getId());
            double totalArrecadado = osAtual != null ? osAtual.getValorTotal() : (osOriginal.getValorTotal() - osOriginal.getValorServico() + maoDeObra);
            try {
                Produto servicoFicticio = new Produto(9999, "Manutenção OS #" + osOriginal.getId(), totalArrecadado, 99999);
                caixaService.realizarVenda(servicoFicticio, 1);
                JOptionPane.showMessageDialog(this, "OS Finalizada! O valor de R$ " + String.format("%.2f", totalArrecadado) + " entrou no Caixa automaticamente.", "Entrada no Caixa", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao creditar valor no caixa: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

        this.alterado = true;
        JOptionPane.showMessageDialog(this, "Andamento da OS #" + osOriginal.getId() + " atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        recarregarItens();
    }

    private void gerarPDF(Cliente cliente, Equipamento equip) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Orçamento em PDF");
        fileChooser.setSelectedFile(new File("Orcamento_OS_" + osOriginal.getId() + ".pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File arquivoDestino = fileChooser.getSelectedFile();
            if (!arquivoDestino.getName().toLowerCase().endsWith(".pdf")) {
                arquivoDestino = new File(arquivoDestino.getAbsolutePath() + ".pdf");
            }

            try {
                List<ServicoItem> servicos = osDAO.obterServicosOS(osOriginal.getId());
                if (servicos.isEmpty() && osOriginal.getValorServico() > 0) {
                    servicos.add(new ServicoItem("Mão de Obra Geral / Diagnóstico", osOriginal.getValorServico()));
                }
                List<PecaItem> pecas = osDAO.obterPecasItensOS(osOriginal.getId());

                OrcamentoPDFService.gerarOrcamentoPDF(arquivoDestino, osOriginal, cliente, equip, servicos, pecas);
                int opt = JOptionPane.showConfirmDialog(this, 
                        "Orçamento gerado com sucesso em:\n" + arquivoDestino.getAbsolutePath() + "\n\nDeseja abrir o arquivo PDF agora?", 
                        "PDF Gerado", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (opt == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(arquivoDestino);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao gerar PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void enviarWhatsApp(Cliente cliente, Equipamento equip) {
        List<ServicoItem> servicos = osDAO.obterServicosOS(osOriginal.getId());
        if (servicos.isEmpty() && osOriginal.getValorServico() > 0) {
            servicos.add(new ServicoItem("Mão de Obra Geral / Diagnóstico", osOriginal.getValorServico()));
        }
        List<PecaItem> pecas = osDAO.obterPecasItensOS(osOriginal.getId());

        boolean sucesso = WhatsAppService.enviarOrcamentoWhatsApp(osOriginal, cliente, equip, servicos, pecas);
        if (sucesso) {
            JOptionPane.showMessageDialog(this, "WhatsApp aberto no navegador com o orçamento pré-formatado!", "WhatsApp", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Não foi possível abrir o WhatsApp automaticamente. Verifique o número de telefone do cliente.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void exibirComprovante(Cliente cliente, Equipamento equip) {
        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("           COMPROVANTE DE ORDEM DE SERVIÇO                 \n");
        sb.append("============================================================\n");
        sb.append("Número da OS:       #").append(osOriginal.getId()).append("\n");
        sb.append("Data de Entrada:    ").append(osOriginal.getDataEntrada().format(formatter)).append("\n");
        sb.append("Status Atual:       ").append(cbStatus.getSelectedItem()).append("\n");
        sb.append("------------------------------------------------------------\n");
        sb.append("[DADOS DO CLIENTE]\n");
        if (cliente != null) {
            sb.append("  Nome:             ").append(cliente.getNome()).append("\n");
            sb.append("  Telefone:         ").append(cliente.getTelefone()).append("\n");
            sb.append("  CPF / CNPJ:       ").append(cliente.getCpfCnpj()).append("\n");
            sb.append("  Endereço:         ").append(cliente.getEndereco()).append("\n");
        }
        sb.append("------------------------------------------------------------\n");
        sb.append("[DADOS DO EQUIPAMENTO]\n");
        if (equip != null) {
            sb.append("  Aparelho:         ").append(equip.getTipo()).append(" ").append(equip.getMarca()).append(" ").append(equip.getModelo()).append("\n");
            sb.append("  Nº de Série:      ").append(equip.getNumeroSerie()).append("\n");
            sb.append("  Cor:              ").append(equip.getCor()).append("\n");
            sb.append("  Avarias:          ").append(equip.getAvarias()).append("\n");
            sb.append("  Acessórios:       ").append(equip.getAcessorios()).append("\n");
        }
        sb.append("------------------------------------------------------------\n");
        sb.append("DEFEITO RELATADO:   ").append(osOriginal.getProblemaRelatado()).append("\n");
        sb.append("DIAGNÓSTICO TÉCNICO:").append(txtDiagnostico.getText().trim().isEmpty() ? " Pendente" : " " + txtDiagnostico.getText().trim()).append("\n");
        sb.append("------------------------------------------------------------\n");
        sb.append("VALOR TOTAL DA OS:  ").append(lblTotal.getText().replace("VALOR TOTAL DA OS: ", "")).append("\n");
        sb.append("============================================================\n");
        sb.append("Termo de Garantia: 90 dias sobre os serviços e peças aplicadas.\n");
        sb.append("\nAssinatura do Cliente: ____________________________________\n");

        JTextArea areaTexto = new JTextArea(sb.toString(), 22, 50);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaTexto.setEditable(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(areaTexto), "Comprovante da OS #" + osOriginal.getId(), JOptionPane.PLAIN_MESSAGE);
    }

    public boolean isAlterado() {
        return alterado;
    }
}
