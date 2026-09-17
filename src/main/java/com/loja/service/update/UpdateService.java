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

    public static final String VERSAO_ATUAL = "1.2.5";
    public static final String DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/DanielWallans/SistemaLoja/master/versao.json";

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

            // Fallback caso master/main alternem
            if (urlStr.contains("/master/")) {
                String fallbackUrl = urlStr.replace("/master/", "/main/");
                return checarUrl(fallbackUrl);
            } else if (urlStr.contains("/main/")) {
                String fallbackUrl = urlStr.replace("/main/", "/master/");
                return checarUrl(fallbackUrl);
            }
        } catch (Exception e) {
            System.err.println("[INFO] Checagem de atualizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o ignorada ou sem internet: " + e.getMessage());
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

        // 1. Script PowerShell em segundo plano (invisÃƒÂ­vel, sem janela preta do prompt)
        File psScript = new File(baseDir, "atualizar_sistema.ps1");
        String psContent =
                "$ErrorActionPreference = 'SilentlyContinue'\r\n" +
                "Start-Sleep -Milliseconds 800\r\n" +
                "try {\r\n" +
                "    $p = Get-Process -Id " + pid + " -ErrorAction SilentlyContinue\r\n" +
                "    if ($p) { $p.WaitForExit(6000) }\r\n" +
                "} catch {}\r\n" +
                "Set-Location -LiteralPath '" + baseDir.getAbsolutePath().replace("'", "''") + "'\r\n" +
                "$source = 'SistemaLoja.jar.update'\r\n" +
                "if (Test-Path $source) {\r\n" +
                "    for ($i = 0; $i -lt 25; $i++) {\r\n" +
                "        try {\r\n" +
                "            Copy-Item $source 'SystemPro.jar' -Force -ErrorAction Stop\r\n" +
                "            Copy-Item $source 'SistemaLoja.jar' -Force -ErrorAction Stop\r\n" +
                "            Remove-Item $source -Force -ErrorAction Stop\r\n" +
                "            break\r\n" +
                "        } catch {\r\n" +
                "            Start-Sleep -Milliseconds 400\r\n" +
                "        }\r\n" +
                "    }\r\n" +
                "}\r\n" +
                "if (Test-Path 'SystemPro.exe') {\r\n" +
                "    Start-Process 'SystemPro.exe'\r\n" +
                "} elseif (Test-Path 'SistemaLoja.exe') {\r\n" +
                "    Start-Process 'SistemaLoja.exe'\r\n" +
                "} elseif (Test-Path 'SystemPro.jar') {\r\n" +
                "    Start-Process 'javaw.exe' -ArgumentList '-jar', 'SystemPro.jar'\r\n" +
                "}\r\n" +
                "Start-Sleep -Seconds 1\r\n" +
                "Remove-Item -LiteralPath $MyInvocation.MyCommand.Path -Force -ErrorAction SilentlyContinue\r\n";

        try (FileWriter fw = new FileWriter(psScript, StandardCharsets.UTF_8)) {
            fw.write(psContent);
        }

        // 2. Script BAT de contingÃƒÂªncia 100% seguro (sem blocos com parÃƒÂªnteses)
        File batScript = new File(baseDir, "atualizar_sistema.bat");
        String batContent = "@echo off\r\n" +
                "chcp 65001 > nul\r\n" +
                "cd /d \"%~dp0\"\r\n" +
                "ping 127.0.0.1 -n 3 > nul\r\n" +
                "set /a tries=0\r\n" +
                ":loop_copy\r\n" +
                "set /a tries+=1\r\n" +
                "if exist \"SistemaLoja.jar.update\" (\r\n" +
                "    copy /y \"SistemaLoja.jar.update\" \"SystemPro.jar\" > nul 2>&1\r\n" +
                "    copy /y \"SistemaLoja.jar.update\" \"SistemaLoja.jar\" > nul 2>&1\r\n" +
                "    del /f /q \"SistemaLoja.jar.update\" > nul 2>&1\r\n" +
                ")\r\n" +
                "if not exist \"SistemaLoja.jar.update\" goto :start_app\r\n" +
                "if %tries% lss 20 (\r\n" +
                "    ping 127.0.0.1 -n 2 > nul\r\n" +
                "    goto :loop_copy\r\n" +
                ")\r\n" +
                ":start_app\r\n" +
                "if exist \"SystemPro.exe\" (\r\n" +
                "    start \"\" \"SystemPro.exe\"\r\n" +
                ") else if exist \"SistemaLoja.exe\" (\r\n" +
                "    start \"\" \"SistemaLoja.exe\"\r\n" +
                ") else (\r\n" +
                "    start \"\" javaw -jar \"SystemPro.jar\"\r\n" +
                ")\r\n" +
                "ping 127.0.0.1 -n 2 > nul\r\n" +
                "del /f /q \".teste_perm\" >nul 2>&1\r\n" +
                "(goto) 2>nul & del \"%~f0\"\r\n";

        try (FileWriter fw = new FileWriter(batScript, StandardCharsets.UTF_8)) {
            fw.write(batContent);
        }

        // 3. Executar o PowerShell de forma 100% invisÃƒÂ­vel (sem janela preta)
        try {
            new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-ExecutionPolicy", "Bypass",
                    "-WindowStyle", "Hidden",
                    "-File", psScript.getAbsolutePath()
            ).directory(baseDir).start();
        } catch (Exception e) {
            new ProcessBuilder("cmd.exe", "/c", "start", "/min", "\"System Pro Updater\"", batScript.getAbsolutePath())
                    .directory(baseDir)
                    .start();
        }

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

