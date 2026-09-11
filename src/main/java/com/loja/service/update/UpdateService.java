package com.loja.service.update;

import com.loja.repository.ConnectionFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateService {

    public static final String VERSAO_ATUAL = "1.2.2";
    public static final String DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/DanielWallans/SistemaLoja/main/versao.json";

    public static String getUpdateUrl() {
        File configFile = ConnectionFactory.getArquivoConfig();
        if (configFile != null && configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                Properties props = new Properties();
                props.load(fis);
                String customUrl = props.getProperty("update.url");
                if (customUrl != null && !customUrl.trim().isEmpty()) {
                    return customUrl.trim();
                }
            } catch (Exception ignored) {}
        }
        return DEFAULT_UPDATE_URL;
    }

    public static UpdateInfo verificarAtualizacao() {
        try {
            String urlStr = getUpdateUrl();
            UpdateInfo info = checarUrl(urlStr);
            if (info != null) return info;

            // Fallback caso a branch 'main' nao responda, tenta 'master'
            if (urlStr.contains("/main/")) {
                String fallbackUrl = urlStr.replace("/main/", "/master/");
                return checarUrl(fallbackUrl);
            }
        } catch (Exception e) {
            System.err.println("[INFO] Checagem de atualização ignorada ou sem internet: " + e.getMessage());
        }
        return null;
    }

    private static UpdateInfo checarUrl(String urlStr) {
        try {
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3500);
            conn.setReadTimeout(3500);
            conn.setRequestProperty("User-Agent", "SystemProUpdater/1.1");

            if (conn.getResponseCode() != 200) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }

            String json = sb.toString();
            String versaoRemota = extrairCampoJson(json, "versao");
            String data = extrairCampoJson(json, "data");
            String novidades = extrairCampoJson(json, "novidades");
            String downloadUrl = extrairCampoJson(json, "download_url");

            if (versaoRemota != null && downloadUrl != null) {
                if (isNovaVersao(versaoRemota, VERSAO_ATUAL)) {
                    if (novidades != null) {
                        novidades = novidades.replace("\\n", "\n");
                    }
                    return new UpdateInfo(versaoRemota, data, novidades, downloadUrl);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static boolean isNovaVersao(String remota, String local) {
        if (remota == null || local == null) return false;
        String rClean = remota.replaceAll("[^0-9.]", "").trim();
        String lClean = local.replaceAll("[^0-9.]", "").trim();
        if (rClean.isEmpty() || lClean.isEmpty() || rClean.equalsIgnoreCase(lClean)) {
            return false;
        }

        try {
            String[] vRemota = rClean.split("\\.");
            String[] vLocal = lClean.split("\\.");

            int maxLen = Math.max(vRemota.length, vLocal.length);
            for (int i = 0; i < maxLen; i++) {
                int r = i < vRemota.length ? Integer.parseInt(vRemota[i]) : 0;
                int l = i < vLocal.length ? Integer.parseInt(vLocal[i]) : 0;
                if (r > l) return true;
                if (r < l) return false;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static String extrairCampoJson(String json, String campo) {
        Pattern pattern = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static void baixarEAplicarAtualizacao(String downloadUrl, Consumer<Integer> progressoCallback) throws Exception {
        File baseDir = getAppDirectory();
        File novoJar = new File(baseDir, "SistemaLoja.jar.update");

        URL url = URI.create(downloadUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(6000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("User-Agent", "SystemProUpdater/1.1");
        conn.setInstanceFollowRedirects(true);

        int responseCode = conn.getResponseCode();
        if (responseCode == 301 || responseCode == 302 || responseCode == 307 || responseCode == 308) {
            String location = conn.getHeaderField("Location");
            if (location != null) {
                conn.disconnect();
                url = URI.create(location).toURL();
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "SystemProUpdater/1.1");
                responseCode = conn.getResponseCode();
            }
        }

        if (responseCode != 200) {
            throw new IOException("Falha no download (HTTP " + responseCode + ")");
        }

        int totalBytes = conn.getContentLength();
        int baixados = 0;

        try (InputStream in = new BufferedInputStream(conn.getInputStream());
             FileOutputStream out = new FileOutputStream(novoJar)) {

            byte[] buffer = new byte[8192];
            int lidos;
            while ((lidos = in.read(buffer)) != -1) {
                out.write(buffer, 0, lidos);
                baixados += lidos;
                if (totalBytes > 0 && progressoCallback != null) {
                    int progresso = (int) (((double) baixados / totalBytes) * 100.0);
                    progressoCallback.accept(progresso);
                }
            }
        }

        long pid = ProcessHandle.current().pid();

        // Criar o script de substituição e reinício do sistema no Windows com tentativas
        File batScript = new File(baseDir, "atualizar_sistema.bat");
        String batContent = "@echo off\r\n" +
                "chcp 65001 > nul\r\n" +
                "setlocal enabledelayedexpansion\r\n" +
                ":: 1. Fixar diretorio de trabalho na pasta do aplicativo\r\n" +
                "cd /d \"%~dp0\"\r\n" +
                "echo [System Pro Updater] Iniciando atualizacao... >> \"atualizacao.log\" 2>&1\r\n" +
                "echo [System Pro Updater] Diretorio: \"%~dp0\" >> \"atualizacao.log\" 2>&1\r\n" +
                ":: 2. Testar permissao de gravacao (se instalado em Program Files, solicitar elevacao)\r\n" +
                "echo teste_permissao > \".teste_perm\" 2>nul\r\n" +
                "if not exist \".teste_perm\" (\r\n" +
                "    if \"%~1\" neq \"elevated\" (\r\n" +
                "        echo [System Pro Updater] Solicitando permissao de Administrador (UAC)... >> \"atualizacao.log\" 2>&1\r\n" +
                "        powershell -Command \"Start-Process cmd.exe -ArgumentList '/c \"\"%~f0\"\" elevated' -Verb RunAs\"\r\n" +
                "        exit /b 0\r\n" +
                "    )\r\n" +
                ") else (\r\n" +
                "    del /f /q \".teste_perm\" >nul 2>&1\r\n" +
                ")\r\n" +
                ":: 3. Aguardar o processo Java (" + pid + ") encerrar completamente\r\n" +
                "powershell -Command \"try { $p = Get-Process -Id " + pid + " -ErrorAction SilentlyContinue; if ($p) { $p.WaitForExit(7000) } } catch {}\" >> \"atualizacao.log\" 2>&1\r\n" +
                ":: 4. Loop de substituicao com espera confiavel (ping)\r\n" +
                "set TRIES=0\r\n" +
                ":retry_copy\r\n" +
                "ping 127.0.0.1 -n 2 > nul\r\n" +
                "set /a TRIES+=1\r\n" +
                "set COPY_OK=0\r\n" +
                "if exist \"SistemaLoja.jar.update\" (\r\n" +
                "    copy /y \"SistemaLoja.jar.update\" \"SystemPro.jar\" >> \"atualizacao.log\" 2>&1\r\n" +
                "    if !errorlevel! equ 0 (\r\n" +
                "        copy /y \"SistemaLoja.jar.update\" \"SistemaLoja.jar\" >> \"atualizacao.log\" 2>&1\r\n" +
                "        set COPY_OK=1\r\n" +
                "    )\r\n" +
                ")\r\n" +
                "if \"!COPY_OK!\"==\"0\" (\r\n" +
                "    if !TRIES! lss 25 (\r\n" +
                "        echo [System Pro Updater] Tentativa !TRIES! falhou (arquivo ocupado), tentando novamente... >> \"atualizacao.log\" 2>&1\r\n" +
                "        goto retry_copy\r\n" +
                "    ) else (\r\n" +
                "        echo [System Pro Updater] ERRO CRITICO: Nao foi possivel sobrescrever o JAR apos 25 tentativas. >> \"atualizacao.log\" 2>&1\r\n" +
                "    )\r\n" +
                ") else (\r\n" +
                "    echo [System Pro Updater] JARs atualizados com sucesso! >> \"atualizacao.log\" 2>&1\r\n" +
                "    del /f /q \"SistemaLoja.jar.update\" >> \"atualizacao.log\" 2>&1\r\n" +
                ")\r\n" +
                ":: 5. Iniciar aplicacao atualizada\r\n" +
                ":start_app\r\n" +
                "if exist \"SystemPro.exe\" (\r\n" +
                "    start \"\" \"SystemPro.exe\"\r\n" +
                ") else if exist \"SistemaLoja.exe\" (\r\n" +
                "    start \"\" \"SistemaLoja.exe\"\r\n" +
                ") else (\r\n" +
                "    start \"\" javaw -jar \"SystemPro.jar\"\r\n" +
                ")\r\n" +
                "ping 127.0.0.1 -n 2 > nul\r\n" +
                "(goto) 2>nul & del \"%~f0\"\r\n";

        try (FileWriter fw = new FileWriter(batScript, StandardCharsets.UTF_8)) {
            fw.write(batContent);
        }

        // Executar o script em segundo plano e fechar o Java atual para liberar o arquivo
        new ProcessBuilder("cmd.exe", "/c", "start", "\"System Pro Updater\"", batScript.getAbsolutePath())
                .directory(baseDir)
                .start();

        System.exit(0);
    }

    public static File getAppDirectory() {
        try {
            File jarDir = new File(UpdateService.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            if (jarDir != null && jarDir.isDirectory()) {
                return jarDir;
            }
        } catch (Exception ignored) {}
        return new File(".");
    }
}
