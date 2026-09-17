package com.loja.view.dialogs;

import com.loja.repository.OSFotoDAO;
import com.loja.view.components.OSFotosPanel;
import com.loja.view.theme.UIComponents;

import javax.swing.*;
import java.awt.*;

public class GerenciadorFotosOSDialog extends JDialog {

    private final OSFotosPanel fotosPanel;

    public GerenciadorFotosOSDialog(Window owner, int osId, OSFotoDAO fotoDAO) {
        super(owner, "Fotos e Evidências da Máquina • OS #" + osId, ModalityType.APPLICATION_MODAL);

        setSize(860, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        this.fotosPanel = new OSFotosPanel(this, osId, fotoDAO);
        add(fotosPanel, BorderLayout.CENTER);

        JPanel pnlRodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnFechar = UIComponents.criarBotaoSecundario("Fechar", this::dispose);
        pnlRodape.add(btnFechar);
        add(pnlRodape, BorderLayout.SOUTH);
    }

    public OSFotosPanel getFotosPanel() {
        return fotosPanel;
    }
}
