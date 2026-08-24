package com.loja.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.loja.model.Produto;
import com.loja.repository.*;
import com.loja.service.CaixaService;
import com.loja.view.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // 1. Configurar Tema Moderno FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("[AVISO] Não foi possível carregar o tema FlatLaf: " + ex.getMessage());
        }

        // 2. Conectar e inicializar banco de dados MySQL
        boolean dbOk = ConnectionFactory.criarTabela();
        if (!dbOk) {
            System.err.println("\n********************************************************************************");
            System.err.println(" [ATENÇÃO / ERRO CRÍTICO] NÃO FOI POSSÍVEL CONECTAR AO BANCO MYSQL NO XAMPP!");
            System.err.println("********************************************************************************");
            System.err.println(" Motivo: O MySQL não está em execução ou não responde na porta 3306.");
            System.err.println(" Como resolver:");
            System.err.println(" 1. Abra o 'XAMPP Control Panel'.");
            System.err.println(" 2. Localize a linha do 'MySQL' e clique no botão 'Start'.");
            System.err.println(" 3. Verifique se o módulo MySQL fica com fundo VERDE e porta 3306.");
            System.err.println(" 4. Reinicie este sistema para que todas as operações sejam salvas!");
            System.err.println("********************************************************************************\n");
        }

        CaixaDAO caixaDAO = new CaixaDAO();
        CaixaService caixaService = new CaixaService();
        ProdutoDAO produtoDAO = new ProdutoDAO();
        ClienteDAO clienteDAO = new ClienteDAO();
        EquipamentoDAO equipamentoDAO = new EquipamentoDAO();
        OrdemServicoDAO osDAO = new OrdemServicoDAO();

        caixaService.abrirCaixa(100.00);

        // 3. Iniciar a Interface Gráfica Desktop (Swing)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(clienteDAO, equipamentoDAO, osDAO, produtoDAO, caixaDAO, caixaService, dbOk);
            frame.setVisible(true);
        });
    }
}
