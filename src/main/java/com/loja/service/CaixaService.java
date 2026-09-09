package com.loja.service;

import com.loja.model.CaixaSessao;
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

    public boolean isCaixaAberto() {
        return caixaDAO.obterSessaoAberta() != null;
    }

    public CaixaSessao getSessaoAberta() {
        return caixaDAO.obterSessaoAberta();
    }

    public CaixaSessao abrirTurno(String operador, double fundoTrocoInicial) {
        CaixaSessao sessao = caixaDAO.abrirNovaSessao(operador, fundoTrocoInicial);
        if (sessao != null) {
            this.saldoEmCaixa = fundoTrocoInicial;
            System.out.println("[INFO] Turno #" + sessao.getId() + " aberto por " + operador + ". Saldo inicial: " + df.format(fundoTrocoInicial));
        }
        return sessao;
    }

    public boolean encerrarTurno(CaixaSessao sessao) {
        boolean ok = caixaDAO.encerrarSessao(sessao);
        if (ok) {
            this.saldoEmCaixa = sessao.getFundoTrocoDeixado();
            System.out.println("[INFO] Turno #" + sessao.getId() + " encerrado com sucesso.");
        }
        return ok;
    }

    public void abrirCaixa(double valorInicial) {
        // Carrega o saldo persistido no banco de dados
        this.saldoEmCaixa = caixaDAO.obterSaldo();
        System.out.println("[INFO] Caixa carregado. Saldo em gaveta: " + df.format(saldoEmCaixa));
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
        realizarSangria(valor, "Sangria de Caixa");
    }

    public boolean realizarSangria(double valor, String justificativa) {
        this.saldoEmCaixa = caixaDAO.obterSaldo();
        if (valor <= saldoEmCaixa) {
            boolean ok = caixaDAO.registrarSangria(valor, justificativa);
            if (ok) {
                this.saldoEmCaixa = caixaDAO.obterSaldo();
                System.out.println("[SANGRIA] Retirada de " + df.format(valor) + " realizada. Motivo: " + justificativa);
                return true;
            }
        } else {
            System.out.println("[ALERTA] Saldo insuficiente para sangria.");
        }
        return false;
    }

    public boolean realizarSuprimento(double valor, String justificativa) {
        boolean ok = caixaDAO.registrarSuprimento(valor, justificativa);
        if (ok) {
            this.saldoEmCaixa = caixaDAO.obterSaldo();
            System.out.println("[SUPRIMENTO] Entrada de " + df.format(valor) + " realizada. Motivo: " + justificativa);
            return true;
        }
        return false;
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

    public double consultarSaldoAtual() {
        this.saldoEmCaixa = caixaDAO.obterSaldo();
        return this.saldoEmCaixa;
    }
}