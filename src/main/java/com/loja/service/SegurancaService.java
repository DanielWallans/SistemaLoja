package com.loja.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SegurancaService {
    private static final String SALT_FIXO = "AssistenciaLoja2026!#$";

    public static String gerarHashSenha(String senhaPlana) {
        if (senhaPlana == null) senhaPlana = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String valorComSalt = senhaPlana + SALT_FIXO;
            byte[] hashBytes = digest.digest(valorComSalt.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo de criptografia SHA-256 não disponível", e);
        }
    }

    public static boolean verificarSenha(String senhaPlana, String hashArmazenado) {
        if (senhaPlana == null || hashArmazenado == null) return false;
        String novoHash = gerarHashSenha(senhaPlana);
        return novoHash.equalsIgnoreCase(hashArmazenado);
    }
}
