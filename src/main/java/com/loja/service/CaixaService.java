package com.loja.service;

import com.loja.model.Produto;
import com.loja.model.SaldoInsuficienteException;
import com.loja.repository.CaixaDAO;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CaixaService {
    private double saldoEmCaixa;
    private List<String> historicoVendas = new ArrayList<>();
    private DecimalFormat df = new DecimalFormat("R$ #,##0.00");
    private CaixaDAO caixaDAO = new CaixaDAO();

    public void abrirCaixa(double valorInicial) {
        // Carrega o saldo persistido no banco de dados
        this.saldoEmCaixa = caixaDAO.obterSaldo();
        System.out.println("[INFO] Caixa aberto. Saldo em caixa: " + df.format(saldoEmCaixa));
    }

    public void realizarVenda(Produto produto, int quantidade) throws SaldoInsuficienteException {
        if (!produto.temEstoque(quantidade)) {
            throw new SaldoInsuficienteException("Estoque insuficiente para " + produto.getNome());
        }

        double total = produto.getPreco() * quantidade;
        produto.baixarEstoque(quantidade);
        this.saldoEmCaixa += total;
        
        // Persiste o saldo atualizado no banco
        caixaDAO.atualizarSaldo(this.saldoEmCaixa);

        String registro = String.format("Venda: %d x %s | Total: %s",
                quantidade, produto.getNome(), df.format(total));
        historicoVendas.add(registro);
        System.out.println("[SUCESSO] " + registro);
    }

    public void realizarSangria(double valor) {
        if (valor <= saldoEmCaixa) {
            saldoEmCaixa -= valor;
            // Persiste o saldo atualizado e registra a sangria no banco
            caixaDAO.atualizarSaldo(saldoEmCaixa);
            caixaDAO.registrarSangria(valor);
            System.out.println("[SANGRIA] Retirada de " + df.format(valor) + " realizada.");
        } else {
            System.out.println("[ALERTA] Saldo insuficiente para sangria.");
        }
    }

    public void fecharCaixa() {
        System.out.println("\n==========================================");
        System.out.println("          RELATÓRIO DE FECHAMENTO         ");
        System.out.println("==========================================");
        System.out.println(" SALDO FINAL EM CAIXA:  " + df.format(saldoEmCaixa));
        System.out.println(" TOTAL DE OPERAÇÕES:    " + historicoVendas.size());
        System.out.println("------------------------------------------");
        System.out.println(" DETALHAMENTO DO DIA (VENDAS):");

        if (historicoVendas.isEmpty()) {
            System.out.println(" [!] Nenhuma venda registrada hoje.");
        } else {
            historicoVendas.forEach(venda -> System.out.println(" > " + venda));
        }

        // Exibe histórico de sangrias salvas no banco
        List<String> sangrias = caixaDAO.obterHistoricoSangrias();
        if (!sangrias.isEmpty()) {
            System.out.println("------------------------------------------");
            System.out.println(" DETALHAMENTO DE RETIRADAS (SANGRIA):");
            sangrias.forEach(sangria -> System.out.println(" > " + sangria));
        }

        System.out.println("==========================================");
    }

    public void consultarSaldoAtual() {
        System.out.println("[CONSULTA] Saldo atual em caixa: " + df.format(saldoEmCaixa));
    }
}