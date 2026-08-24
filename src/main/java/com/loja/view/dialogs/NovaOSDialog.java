package com.loja.view.dialogs;

import com.loja.model.Cliente;
import com.loja.model.Equipamento;
import com.loja.model.OrdemServico;
import com.loja.repository.ClienteDAO;
import com.loja.repository.EquipamentoDAO;
import com.loja.repository.OrdemServicoDAO;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class NovaOSDialog extends JDialog {
    private final ClienteDAO clienteDAO;
    private final EquipamentoDAO equipDAO;
    private final OrdemServicoDAO osDAO;
    private boolean salvo = false;
    private OrdemServico osCriada = null;

    // Passo 1 & 2
    private JComboBox<EquipamentoDialog.ClienteComboItem> cbClientes;
    private JButton btnNovoCliente;
    private JComboBox<EquipamentoComboItem> cbEquipamentos;
    private JButton btnNovoEquipamento;

    // CardLayout para Checklist Dinâmico
    private CardLayout clChecklist;
    private JPanel pnlChecklistCards;
    private String tipoChecklistAtual = "NOTEBOOK";

    // Checklist Notebook
    private JComboBox<String> nbLigando, nbCarregando, nbTela, nbTeclado, nbArmazenamento, nbRuido, nbLiquido, nbCabo;
    private JTextField nbOutros;

    // Checklist Desktop
    private JComboBox<String> dtLigando, dtFonte, dtGabinete, dtCaboForca, dtPerifericos, dtRuido, dtPoeira, dtArmazenamento;
    private JTextField dtOutros;

    // Checklist Impressora
    private JComboBox<String> impLigando, impPapel, impCartucho, impNivelTinta, impCabeca, impCaboUsb, impCaboForca, impManchas, impRuido;
    private JTextField impOutros;

    // Checklist Celular/Tablet/Outros
    private JComboBox<String> celLigando, celTouch, celConector, celCameras, celCarregador;
    private JTextField celOutros;

    // Passo 4 & 5
    private JTextArea txtProblema;
    private JTextArea txtObservacoes;

    public static class EquipamentoComboItem {
        public final int id;
        public final String tipo;
        public final String descricao;

        public EquipamentoComboItem(int id, String tipo, String descricao) {
            this.id = id;
            this.tipo = tipo;
            this.descricao = descricao;
        }

        @Override
        public String toString() {
            return "#" + id + " - [" + tipo + "] " + descricao;
        }
    }

    public NovaOSDialog(Frame owner, ClienteDAO clienteDAO, EquipamentoDAO equipDAO, OrdemServicoDAO osDAO) {
        super(owner, "Abertura de Ordem de Serviço (Checklist Dinâmico)", true);
        this.clienteDAO = clienteDAO;
        this.equipDAO = equipDAO;
        this.osDAO = osDAO;

        initComponents();
        carregarClientes();
        setSize(780, 740);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. Bloco Cliente & Equipamento
        JPanel pnlCabecalho = new JPanel(new GridBagLayout());
        pnlCabecalho.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " 1 & 2. Identificação do Cliente e Aparelho ", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 13)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        cbClientes = new JComboBox<>();
        btnNovoCliente = new JButton("+ Novo Cliente");
        cbEquipamentos = new JComboBox<>();
        btnNovoEquipamento = new JButton("+ Novo Equip.");

        cbClientes.addActionListener(e -> carregarEquipamentosDoCliente());
        cbEquipamentos.addActionListener(e -> atualizarChecklistPorTipoEquipamento());
        btnNovoCliente.addActionListener(e -> abrirNovoCliente());
        btnNovoEquipamento.addActionListener(e -> abrirNovoEquipamento());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        pnlCabecalho.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        pnlCabecalho.add(cbClientes, gbc);
        gbc.gridx = 2; gbc.weightx = 0.2;
        pnlCabecalho.add(btnNovoCliente, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        pnlCabecalho.add(new JLabel("Equipamento:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        pnlCabecalho.add(cbEquipamentos, gbc);
        gbc.gridx = 2; gbc.weightx = 0.2;
        pnlCabecalho.add(btnNovoEquipamento, gbc);

        mainPanel.add(pnlCabecalho);
        mainPanel.add(Box.createVerticalStrut(10));

        // 2. Bloco Checklist Técnico Dinâmico
        clChecklist = new CardLayout();
        pnlChecklistCards = new JPanel(clChecklist);
        pnlChecklistCards.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " 3. Checklist Técnico Específico de Entrada ", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 13)));

        pnlChecklistCards.add(criarCardNotebook(), "NOTEBOOK");
        pnlChecklistCards.add(criarCardDesktop(), "DESKTOP");
        pnlChecklistCards.add(criarCardImpressora(), "IMPRESSORA");
        pnlChecklistCards.add(criarCardOutros(), "OUTROS");

        mainPanel.add(pnlChecklistCards);
        mainPanel.add(Box.createVerticalStrut(10));

        // 3. Bloco Relato do Defeito e Observações
        JPanel pnlRelato = new JPanel(new GridLayout(2, 1, 5, 8));
        pnlRelato.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " 4 & 5. Defeito Relatado e Observações Técnicas ", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 13)));

        txtProblema = new JTextArea(3, 30);
        txtProblema.setLineWrap(true);
        txtProblema.setWrapStyleWord(true);

        txtObservacoes = new JTextArea(2, 30);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);

        JPanel p1 = new JPanel(new BorderLayout(5, 5));
        p1.add(new JLabel("Defeito Relatado pelo Cliente / Constatado *:"), BorderLayout.NORTH);
        p1.add(new JScrollPane(txtProblema), BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout(5, 5));
        p2.add(new JLabel("Observações Finais do Técnico (Opcional):"), BorderLayout.NORTH);
        p2.add(new JScrollPane(txtObservacoes), BorderLayout.CENTER);

        pnlRelato.add(p1);
        pnlRelato.add(p2);
        mainPanel.add(pnlRelato);

        JScrollPane scroll = new JScrollPane(mainPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // Barra inferior de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnSalvar = new JButton("Gerar e Abrir Ordem de Serviço");
        btnSalvar.setFont(btnSalvar.getFont().deriveFont(Font.BOLD, 13f));

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvarOS());

        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnSalvar);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // --- CARDS ESPECÍFICOS DE CHECKLIST ---
    private JPanel criarCardNotebook() {
        JPanel pnl = new JPanel(new GridLayout(5, 2, 10, 6));
        nbLigando = new JComboBox<>(new String[]{"Sim", "Não"});
        nbCarregando = new JComboBox<>(new String[]{"Sim", "Não", "Não testado"});
        nbTela = new JComboBox<>(new String[]{"Não (Tela Intacta)", "Sim (Com trinca/manchas)", "Não se aplica"});
        nbTeclado = new JComboBox<>(new String[]{"Sim", "Não", "Não testado"});
        nbArmazenamento = new JComboBox<>(new String[]{"Sim", "Não", "Não testado"});
        nbRuido = new JComboBox<>(new String[]{"Não", "Sim (Ruído Anormal)"});
        nbLiquido = new JComboBox<>(new String[]{"Não", "Sim (Indício de Líquido)"});
        nbCabo = new JComboBox<>(new String[]{"Sim (Fonte/Carregador entregue)", "Não"});
        nbOutros = new JTextField("Nenhum");

        pnl.add(criarItemChecklist("1. Notebook Ligando?", nbLigando));
        pnl.add(criarItemChecklist("2. Carregando / Bateria OK?", nbCarregando));
        pnl.add(criarItemChecklist("3. Defeito na Tela?", nbTela));
        pnl.add(criarItemChecklist("4. Teclado / Touchpad OK?", nbTeclado));
        pnl.add(criarItemChecklist("5. HD/SSD Acessível?", nbArmazenamento));
        pnl.add(criarItemChecklist("6. Ruído Anormal?", nbRuido));
        pnl.add(criarItemChecklist("7. Dano por Líquido?", nbLiquido));
        pnl.add(criarItemChecklist("8. Carregador / Fonte Entregue?", nbCabo));
        pnl.add(criarItemChecklist("9. Outras Observações:", nbOutros));
        return pnl;
    }

    private JPanel criarCardDesktop() {
        JPanel pnl = new JPanel(new GridLayout(5, 2, 10, 6));
        dtLigando = new JComboBox<>(new String[]{"Sim", "Não"});
        dtFonte = new JComboBox<>(new String[]{"Sim (Fonte OK)", "Não (Fonte com defeito)", "Não testada"});
        dtGabinete = new JComboBox<>(new String[]{"Não (Gabinete Intacto)", "Sim (Avarias/Amassados)"});
        dtCaboForca = new JComboBox<>(new String[]{"Sim (Deixou cabo de força)", "Não"});
        dtPerifericos = new JComboBox<>(new String[]{"Não deixou periféricos", "Sim (Teclado/Mouse inclusos)"});
        dtRuido = new JComboBox<>(new String[]{"Não", "Sim (Ruído em Coolers/Ventoinha)"});
        dtPoeira = new JComboBox<>(new String[]{"Não", "Sim (Poeira Excessiva / Oxidação)"});
        dtArmazenamento = new JComboBox<>(new String[]{"Sim", "Não", "Não testado"});
        dtOutros = new JTextField("Nenhum");

        pnl.add(criarItemChecklist("1. Computador Ligando?", dtLigando));
        pnl.add(criarItemChecklist("2. Fonte de Alimentação OK?", dtFonte));
        pnl.add(criarItemChecklist("3. Gabinete com Avarias?", dtGabinete));
        pnl.add(criarItemChecklist("4. Cabo de Força Incluso?", dtCaboForca));
        pnl.add(criarItemChecklist("5. Mouse / Teclado Inclusos?", dtPerifericos));
        pnl.add(criarItemChecklist("6. Ruído Anormal (Coolers)?", dtRuido));
        pnl.add(criarItemChecklist("7. Poeira Excessiva / Oxidação?", dtPoeira));
        pnl.add(criarItemChecklist("8. Armazenamento (HD/SSD)?", dtArmazenamento));
        pnl.add(criarItemChecklist("9. Outras Observações:", dtOutros));
        return pnl;
    }

    private JPanel criarCardImpressora() {
        JPanel pnl = new JPanel(new GridLayout(5, 2, 10, 6));
        impLigando = new JComboBox<>(new String[]{"Sim", "Não"});
        impPapel = new JComboBox<>(new String[]{"Sim (Puxa normalmente)", "Não (Atolando/Não puxa)", "Não testado"});
        impCartucho = new JComboBox<>(new String[]{"Sim (Instalado)", "Não (Sem cartucho/toner)"});
        impNivelTinta = new JComboBox<>(new String[]{"Nível Normal/Cheio", "Tinta/Toner Baixo", "Vazio", "Não se aplica"});
        impCabeca = new JComboBox<>(new String[]{"Não (Sem falhas)", "Sim (Cabeça/Cilindro falhando)", "Não testada"});
        impCaboUsb = new JComboBox<>(new String[]{"Sim (Cabo USB/Rede entregue)", "Não"});
        impCaboForca = new JComboBox<>(new String[]{"Sim (Cabo de força/Fonte entregue)", "Não"});
        impManchas = new JComboBox<>(new String[]{"Não (Impressão limpa)", "Sim (Manchas/Borrões)", "Não testada"});
        impRuido = new JComboBox<>(new String[]{"Não", "Sim (Ruído em Engrenagens/Tracionador)"});
        impOutros = new JTextField("Nenhum");

        pnl.add(criarItemChecklist("1. Impressora Ligando?", impLigando));
        pnl.add(criarItemChecklist("2. Puxa Papel Corretamente?", impPapel));
        pnl.add(criarItemChecklist("3. Cartucho / Toner Instalado?", impCartucho));
        pnl.add(criarItemChecklist("4. Nível de Tinta / Toner:", impNivelTinta));
        pnl.add(criarItemChecklist("5. Cabeça de Impressão OK?", impCabeca));
        pnl.add(criarItemChecklist("6. Cabo USB / Rede Incluso?", impCaboUsb));
        pnl.add(criarItemChecklist("7. Cabo de Força Incluso?", impCaboForca));
        pnl.add(criarItemChecklist("8. Manchas ou Borrões?", impManchas));
        pnl.add(criarItemChecklist("9. Ruído em Engrenagens?", impRuido));
        pnl.add(criarItemChecklist("10. Outras Observações:", impOutros));
        return pnl;
    }

    private JPanel criarCardOutros() {
        JPanel pnl = new JPanel(new GridLayout(3, 2, 10, 6));
        celLigando = new JComboBox<>(new String[]{"Sim", "Não"});
        celTouch = new JComboBox<>(new String[]{"Sim (Display/Touch Intacto)", "Não (Tela trincada/com falha)"});
        celConector = new JComboBox<>(new String[]{"Sim (Carregando OK)", "Não (Conector com defeito)", "Não testado"});
        celCameras = new JComboBox<>(new String[]{"Sim", "Não", "Não se aplica"});
        celCarregador = new JComboBox<>(new String[]{"Sim (Cabo/Carregador entregue)", "Não"});
        celOutros = new JTextField("Nenhum");

        pnl.add(criarItemChecklist("1. Aparelho Ligando / Dá Imagem?", celLigando));
        pnl.add(criarItemChecklist("2. Display / Touch Intacto?", celTouch));
        pnl.add(criarItemChecklist("3. Conector de Carga / USB OK?", celConector));
        pnl.add(criarItemChecklist("4. Câmeras / Alto-falante OK?", celCameras));
        pnl.add(criarItemChecklist("5. Cabo / Carregador Incluso?", celCarregador));
        pnl.add(criarItemChecklist("6. Outras Observações:", celOutros));
        return pnl;
    }

    private JPanel criarItemChecklist(String rotulo, JComponent componente) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        JLabel lbl = new JLabel(rotulo);
        lbl.setFont(lbl.getFont().deriveFont(11f));
        p.add(lbl, BorderLayout.NORTH);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }

    private void carregarClientes() {
        cbClientes.removeAllItems();
        List<Cliente> clientes = clienteDAO.buscarTodos();
        for (Cliente c : clientes) {
            cbClientes.addItem(new EquipamentoDialog.ClienteComboItem(c.getId(), c.getNome()));
        }
        if (cbClientes.getItemCount() > 0) {
            cbClientes.setSelectedIndex(0);
            carregarEquipamentosDoCliente();
        }
    }

    private void carregarEquipamentosDoCliente() {
        cbEquipamentos.removeAllItems();
        EquipamentoDialog.ClienteComboItem item = (EquipamentoDialog.ClienteComboItem) cbClientes.getSelectedItem();
        if (item == null) return;

        List<Equipamento> equips = equipDAO.buscarPorCliente(item.id);
        for (Equipamento eq : equips) {
            String desc = eq.getMarca() + " " + eq.getModelo() + " (S/N: " + eq.getNumeroSerie() + ")";
            cbEquipamentos.addItem(new EquipamentoComboItem(eq.getId(), eq.getTipo(), desc));
        }
        atualizarChecklistPorTipoEquipamento();
    }

    private void atualizarChecklistPorTipoEquipamento() {
        EquipamentoComboItem item = (EquipamentoComboItem) cbEquipamentos.getSelectedItem();
        if (item == null) {
            clChecklist.show(pnlChecklistCards, "NOTEBOOK");
            tipoChecklistAtual = "NOTEBOOK";
            return;
        }

        String tipo = item.tipo.toLowerCase();
        if (tipo.contains("notebook") || tipo.contains("laptop") || tipo.contains("macbook")) {
            clChecklist.show(pnlChecklistCards, "NOTEBOOK");
            tipoChecklistAtual = "NOTEBOOK";
        } else if (tipo.contains("computador") || tipo.contains("desktop") || tipo.contains("pc") || tipo.contains("gabinete")) {
            clChecklist.show(pnlChecklistCards, "DESKTOP");
            tipoChecklistAtual = "DESKTOP";
        } else if (tipo.contains("impressora") || tipo.contains("multifuncional") || tipo.contains("plotter")) {
            clChecklist.show(pnlChecklistCards, "IMPRESSORA");
            tipoChecklistAtual = "IMPRESSORA";
        } else {
            clChecklist.show(pnlChecklistCards, "OUTROS");
            tipoChecklistAtual = "OUTROS";
        }
    }

    private void abrirNovoCliente() {
        ClienteDialog dialog = new ClienteDialog((Frame) getOwner(), clienteDAO, null);
        dialog.setVisible(true);
        if (dialog.isSalvo()) {
            carregarClientes();
            if (cbClientes.getItemCount() > 0) {
                cbClientes.setSelectedIndex(cbClientes.getItemCount() - 1);
            }
        }
    }

    private void abrirNovoEquipamento() {
        EquipamentoDialog.ClienteComboItem item = (EquipamentoDialog.ClienteComboItem) cbClientes.getSelectedItem();
        int clienteId = item != null ? item.id : -1;
        EquipamentoDialog dialog = new EquipamentoDialog((Frame) getOwner(), equipDAO, clienteDAO, null, clienteId);
        dialog.setVisible(true);
        if (dialog.isSalvo()) {
            carregarEquipamentosDoCliente();
            if (cbEquipamentos.getItemCount() > 0) {
                cbEquipamentos.setSelectedIndex(0);
            }
        }
    }

    private void salvarOS() {
        EquipamentoDialog.ClienteComboItem clienteItem = (EquipamentoDialog.ClienteComboItem) cbClientes.getSelectedItem();
        if (clienteItem == null) {
            JOptionPane.showMessageDialog(this, "Selecione ou cadastre um cliente!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        EquipamentoComboItem equipItem = (EquipamentoComboItem) cbEquipamentos.getSelectedItem();
        if (equipItem == null) {
            JOptionPane.showMessageDialog(this, "Selecione ou cadastre um equipamento para este cliente!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String defeito = txtProblema.getText().trim();
        if (defeito.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O relato do defeito/problema é obrigatório!", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtProblema.requestFocus();
            return;
        }

        String obs = txtObservacoes.getText().trim();

        // Montar checklist textual específico do tipo ativo
        String checklist;
        switch (tipoChecklistAtual) {
            case "DESKTOP" -> checklist = String.format(
                    "• Tipo: Desktop/PC | Ligando: %s | Fonte OK: %s\n" +
                    "• Gabinete com Avarias: %s | Cabo Força: %s | Mouse/Teclado: %s\n" +
                    "• Ruído Coolers: %s | Poeira/Oxidação: %s | HD/SSD: %s\n" +
                    "• Outras Obs.: %s",
                    dtLigando.getSelectedItem(), dtFonte.getSelectedItem(), dtGabinete.getSelectedItem(),
                    dtCaboForca.getSelectedItem(), dtPerifericos.getSelectedItem(), dtRuido.getSelectedItem(),
                    dtPoeira.getSelectedItem(), dtArmazenamento.getSelectedItem(), dtOutros.getText().trim()
            );
            case "IMPRESSORA" -> checklist = String.format(
                    "• Tipo: Impressora | Ligando: %s | Puxa Papel: %s | Cartucho Instalado: %s\n" +
                    "• Nível Tinta: %s | Cabeça Impressão: %s | Cabo USB: %s\n" +
                    "• Cabo Força: %s | Manchas/Falhas: %s | Ruído Engrenagens: %s\n" +
                    "• Outras Obs.: %s",
                    impLigando.getSelectedItem(), impPapel.getSelectedItem(), impCartucho.getSelectedItem(),
                    impNivelTinta.getSelectedItem(), impCabeca.getSelectedItem(), impCaboUsb.getSelectedItem(),
                    impCaboForca.getSelectedItem(), impManchas.getSelectedItem(), impRuido.getSelectedItem(),
                    impOutros.getText().trim()
            );
            case "OUTROS" -> checklist = String.format(
                    "• Tipo: Celular/Outro | Ligando/Imagem: %s | Touch/Display: %s\n" +
                    "• Conector Carga: %s | Câmeras/Som: %s | Cabo/Carregador: %s\n" +
                    "• Outras Obs.: %s",
                    celLigando.getSelectedItem(), celTouch.getSelectedItem(), celConector.getSelectedItem(),
                    celCameras.getSelectedItem(), celCarregador.getSelectedItem(), celOutros.getText().trim()
            );
            default -> checklist = String.format(
                    "• Tipo: Notebook | Ligando: %s | Carregamento/Bateria: %s | Tela: %s\n" +
                    "• Teclado/Touch: %s | HD/SSD: %s | Ruído: %s\n" +
                    "• Líquido: %s | Cabo/Fonte Entregue: %s\n" +
                    "• Outras Obs.: %s",
                    nbLigando.getSelectedItem(), nbCarregando.getSelectedItem(), nbTela.getSelectedItem(),
                    nbTeclado.getSelectedItem(), nbArmazenamento.getSelectedItem(), nbRuido.getSelectedItem(),
                    nbLiquido.getSelectedItem(), nbCabo.getSelectedItem(), nbOutros.getText().trim()
            );
        }

        OrdemServico os = new OrdemServico(clienteItem.id, equipItem.id, defeito, checklist, obs);
        if (osDAO.abrirOS(os)) {
            this.salvo = true;
            this.osCriada = os;
            JOptionPane.showMessageDialog(this, 
                    "Ordem de Serviço #" + os.getId() + " aberta com sucesso!\nCliente: " + clienteItem.nome, 
                    "OS Gerada com Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Falha ao gravar a OS no banco MySQL!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSalvo() {
        return salvo;
    }

    public OrdemServico getOsCriada() {
        return osCriada;
    }
}
