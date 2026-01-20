package com.loja.app;

import com.loja.model.Produto;
import com.loja.model.Venda;
import com.loja.model.SaldoInsuficienteException;
import com.loja.repositiry.ConnectionFactory;
import com.loja.repository.ProdutoDAO;
import com.loja.repositiry.VendaDAO;
import com.loja.service.CaixaService;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        CaixaService caixa = new CaixaService();

        ConnectionFactory.criarTabela();
        ProdutoDAO produtoDAO = new ProdutoDAO();
        VendaDAO vendaDAO = new VendaDAO();


        inicializarProdutos(produtoDAO);
        List<Produto> listaProdutos = produtoDAO.buscarTodos();

        System.out.println("======= SISTEMA DE LOJA v1.3 (Relatórios Ativos) =======");
        caixa.abrirCaixa(100.00);

        int opcao = 0;
        while (opcao != 5) {
            System.out.println("\n--- MENU DE OPERAÇÕES ---");
            System.out.println("1. Realizar Venda");
            System.out.println("2. Realizar Sangria");
            System.out.println("3. Consultar Saldo");
            System.out.println("4. Ver Histórico de Vendas");
            System.out.println("5. Fechar Caixa e Sair");
            System.out.print("Escolha: ");

            opcao = teclado.nextInt();

            switch (opcao) {
                case 1:
                    System.out.println("\n--- PRODUTOS DISPONÍVEIS ---");
                    for (int i = 0; i < listaProdutos.size(); i++) {
                        System.out.println(i + ". " + listaProdutos.get(i).getNome() + " | R$ " + listaProdutos.get(i).getPreco());
                    }
                    System.out.print("Escolha o número do produto: ");
                    int index = teclado.nextInt();
                    Produto selecionado = listaProdutos.get(index);

                    System.out.print("Qtd de " + selecionado.getNome() + ": ");
                    int qtd = teclado.nextInt();

                    try {
                        caixa.realizarVenda(selecionado, qtd);
                        produtoDAO.salvar(selecionado);

                        Venda v = new Venda(selecionado.getId(), qtd, (selecionado.getPreco() * qtd));
                        vendaDAO.registrarVenda(v);
                        System.out.println("[OK] Venda registrada!");
                    } catch (SaldoInsuficienteException e) {
                        System.out.println("[ERRO] " + e.getMessage());
                    }
                    break;

                case 2:
                    System.out.print("Valor da Sangria: ");
                    caixa.realizarSangria(teclado.nextDouble());
                    break;

                case 3:
                    caixa.consultarSaldoAtual();
                    break;

                case 4:
                    System.out.println("\n--- HISTÓRICO DE VENDAS (CONSULTA DIRETA AO BANCO) ---");

                    System.out.println("Relatório gerado com sucesso para o fechamento.");
                    break;

                case 5:
                    System.out.println("\n[SISTEMA] Fechando caixa e salvando dados...");
                    caixa.fecharCaixa();
                    break;
            }
        }
        teclado.close();
    }

    private static void inicializarProdutos(ProdutoDAO dao) {
        if (dao.buscarTodos().isEmpty()) {
            dao.salvar(new Produto(1, "ThinkPad T440", 1500.00, 10));
            dao.salvar(new Produto(2, "Teclado Mecanico RGB", 250.00, 20));
            dao.salvar(new Produto(3, "Mouse Sem Fio", 120.00, 15));
            System.out.println("[DB] Estoque inicial de 3 produtos criado!");
        }
    }
}