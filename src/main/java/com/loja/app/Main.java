package com.loja.app;

import com.loja.model.Produto;
import com.loja.model.SaldoInsuficienteException;
import com.loja.service.CaixaService;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        CaixaService caixa = new CaixaService();

        Produto p1 = new Produto(1, "ThinkPad T440", 1500.00, 10);

        System.out.println("======= SISTEMA DE LOJA v1.1 =======");
        caixa.abrirCaixa(100.00);

        int opcao = 0;
        while (opcao != 4) {
            System.out.println("\n--- MENU DE OPERAÇÕES ---");
            System.out.println("1. Realizar Venda");
            System.out.println("2. Realizar Sangria");
            System.out.println("3. Consultar Saldo");
            System.out.println("4. Fechar Caixa e Sair");
            System.out.print("Escolha: ");

            opcao = teclado.nextInt();

            switch (opcao) {
                case 1:
                    System.out.print("Qtd de " + p1.getNome() + ": ");
                    int qtd = teclado.nextInt();
                    try {
                        caixa.realizarVenda(p1, qtd);
                    } catch (SaldoInsuficienteException e) {
                        System.out.println("[ERRO] " + e.getMessage());
                    }
                    break;
                case 2:
                    System.out.print("Valor da Sangria: ");
                    double valorS = teclado.nextDouble();
                    caixa.realizarSangria(valorS);
                    break;
                case 3:
                    caixa.consultarSaldoAtual();
                    break;
                case 4:
                    System.out.println("\n[SISTEMA] Iniciando protocolo de fechamento...");
                    caixa.fecharCaixa();
                    System.out.println("\nObrigado por utilizar o Sistema de Loja v1.1.");
                    System.out.println("ThinkPad T440 - Sessão encerrada com segurança.");
                    System.out.println("------------------------------------------");
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        }
        System.out.println("Sistema encerrado.");
        teclado.close();
    }

}