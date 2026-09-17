package com.loja.service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

public class ImagemService {

    public static final int MAX_LARGURA_FOTO = 1920;
    public static final int MAX_ALTURA_FOTO = 1080;
    public static final int TAMANHO_MINIATURA = 160;
    public static final float QUALIDADE_PADRAO = 0.85f;

    /**
     * Redimensiona e comprime uma foto em formato JPEG de alta qualidade.
     */
    public static byte[] otimizarFoto(byte[] originalBytes) throws IOException {
        if (originalBytes == null || originalBytes.length == 0) {
            return originalBytes;
        }

        BufferedImage imgOriginal = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (imgOriginal == null) {
            return originalBytes;
        }

        int largura = imgOriginal.getWidth();
        int altura = imgOriginal.getHeight();

        // Se já for menor que o máximo, apenas comprime se for muito grande
        if (largura <= MAX_LARGURA_FOTO && altura <= MAX_ALTURA_FOTO && originalBytes.length <= 800 * 1024) {
            return originalBytes;
        }

        double ratio = Math.min((double) MAX_LARGURA_FOTO / largura, (double) MAX_ALTURA_FOTO / altura);
        if (ratio > 1.0) {
            ratio = 1.0;
        }

        int novaLargura = Math.max(1, (int) (largura * ratio));
        int novaAltura = Math.max(1, (int) (altura * ratio));

        BufferedImage imgRedimensionada = new BufferedImage(novaLargura, novaAltura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = imgRedimensionada.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fundo branco caso haja transparência
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, novaLargura, novaAltura);
        g2.drawImage(imgOriginal, 0, 0, novaLargura, novaAltura, null);
        g2.dispose();

        return comprimirJpeg(imgRedimensionada, QUALIDADE_PADRAO);
    }

    /**
     * Gera miniatura quadrada proporcional de 160x160 para visualização rápida em galeria.
     */
    public static byte[] gerarMiniatura(byte[] imagemBytes) throws IOException {
        if (imagemBytes == null || imagemBytes.length == 0) {
            return null;
        }

        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imagemBytes));
        if (img == null) {
            return null;
        }

        int largura = img.getWidth();
        int altura = img.getHeight();

        double ratio = Math.min((double) TAMANHO_MINIATURA / largura, (double) TAMANHO_MINIATURA / altura);
        int novaLargura = Math.max(1, (int) (largura * ratio));
        int novaAltura = Math.max(1, (int) (altura * ratio));

        BufferedImage thumb = new BufferedImage(novaLargura, novaAltura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = thumb.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, novaLargura, novaAltura);
        g2.drawImage(img, 0, 0, novaLargura, novaAltura, null);
        g2.dispose();

        return comprimirJpeg(thumb, 0.80f);
    }

    private static byte[] comprimirJpeg(BufferedImage img, float qualidade) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            ImageIO.write(img, "jpg", baos);
            return baos.toByteArray();
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(qualidade);
            }
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }

    /**
     * Verifica se a Área de Transferência (Clipboard) do sistema contém uma imagem.
     */
    public static boolean temImagemNaAreaTransferencia() {
        try {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            return t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtém imagem copiada da Área de Transferência (Ctrl+V) em formato de bytes.
     */
    public static byte[] obterImagemDaAreaTransferencia() {
        try {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image img = (Image) t.getTransferData(DataFlavor.imageFlavor);
                if (img != null) {
                    BufferedImage bImg;
                    if (img instanceof BufferedImage) {
                        bImg = (BufferedImage) img;
                    } else {
                        bImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2 = bImg.createGraphics();
                        g2.drawImage(img, 0, 0, null);
                        g2.dispose();
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bImg, "jpg", baos);
                    return baos.toByteArray();
                }
            }
        } catch (Exception e) {
            System.err.println("[AVISO] Falha ao ler imagem do clipboard: " + e.getMessage());
        }
        return null;
    }

    /**
     * Cria um ImageIcon a partir de bytes com suporte seguro a null.
     */
    public static ImageIcon criarImageIcon(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return new ImageIcon(bytes);
    }

    /**
     * Lê todos os bytes de um arquivo.
     */
    public static byte[] lerBytesArquivo(File arquivo) throws IOException {
        try (FileInputStream fis = new FileInputStream(arquivo);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int lidos;
            while ((lidos = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, lidos);
            }
            return baos.toByteArray();
        }
    }

    /**
     * Salva bytes em um arquivo de destino.
     */
    public static void salvarEmArquivo(byte[] dados, File destino) throws IOException {
        if (dados == null) return;
        try (FileOutputStream fos = new FileOutputStream(destino)) {
            fos.write(dados);
            fos.flush();
        }
    }
}
