using System;
using System.Diagnostics;
using System.IO;
using System.Text.RegularExpressions;
using System.Windows.Forms;

namespace SistemaLojaLauncher
{
    static class Program
    {
        [STAThread]
        static void Main()
        {
            try
            {
                string baseDir = AppDomain.CurrentDomain.BaseDirectory;
                string jarPath = Path.Combine(baseDir, "SistemaLoja.jar");

                if (!File.Exists(jarPath))
                {
                    MessageBox.Show(
                        "O arquivo 'SistemaLoja.jar' não foi encontrado na mesma pasta do executável.\n\n" +
                        "Caminho esperado:\n" + jarPath,
                        "Sistema Loja - Arquivo Ausente",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Error
                    );
                    return;
                }

                string javaw = EncontrarJavaValido();

                if (string.IsNullOrEmpty(javaw) || !File.Exists(javaw))
                {
                    DialogResult resultado = MessageBox.Show(
                        "O sistema necessita do Java 21 (ou superior) para funcionar.\n\n" +
                        "Motivo: O Java instalado no computador é uma versão antiga (ex: Java 8) ou o Java 21 não foi encontrado.\n\n" +
                        "Deseja abrir a página para baixar e instalar o Java 21 (JRE) gratuitamente agora?",
                        "Sistema Loja - Java 21 Necessário",
                        MessageBoxButtons.YesNo,
                        MessageBoxIcon.Warning
                    );

                    if (resultado == DialogResult.Yes)
                    {
                        Process.Start(new ProcessStartInfo("https://adoptium.net/temurin/releases/?version=21") { UseShellExecute = true });
                    }
                    return;
                }

                // Iniciar a aplicação silenciosamente (sem janela preta de prompt)
                ProcessStartInfo psi = new ProcessStartInfo();
                psi.FileName = javaw;
                psi.Arguments = "-jar \"" + jarPath + "\"";
                psi.WorkingDirectory = baseDir;
                psi.UseShellExecute = false;
                psi.CreateNoWindow = true;

                Process.Start(psi);
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    "Ocorreu um erro ao iniciar a aplicação:\n\n" + ex.Message,
                    "Erro ao Iniciar",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
                );
            }
        }

        static string EncontrarJavaValido()
        {
            string baseDir = AppDomain.CurrentDomain.BaseDirectory;

            // 1. Prioridade máxima: JRE embutida na própria pasta do sistema (pasta 'jre')
            string localJre = Path.Combine(baseDir, "jre", "bin", "javaw.exe");
            if (File.Exists(localJre)) return localJre;

            // 2. Verificar JAVA_HOME
            string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
            if (!string.IsNullOrEmpty(javaHome))
            {
                string cand = Path.Combine(javaHome, "bin", "javaw.exe");
                if (File.Exists(cand) && IsJava21OuMaior(cand)) return cand;
            }

            // 3. Procurar em C:\Program Files\Eclipse Adoptium, BellSoft, Java, etc.
            string[] programFilesRoots = new string[] {
                Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles),
                Environment.GetEnvironmentVariable("ProgramFiles(x86)")
            };

            foreach (string pf in programFilesRoots)
            {
                if (string.IsNullOrEmpty(pf) || !Directory.Exists(pf)) continue;

                string[] subPastas = new string[] { "Eclipse Adoptium", "Java", "BellSoft", "Amazon Corretto", "Microsoft" };
                foreach (string sp in subPastas)
                {
                    string dir = Path.Combine(pf, sp);
                    if (Directory.Exists(dir))
                    {
                        try
                        {
                            foreach (string sub in Directory.GetDirectories(dir))
                            {
                                string cand = Path.Combine(sub, "bin", "javaw.exe");
                                if (File.Exists(cand) && IsJava21OuMaior(cand)) return cand;
                            }
                        }
                        catch { }
                    }
                }
            }

            // 4. Procurar no ambiente de desenvolvimento do usuário (Antigravity / VS Code)
            string userProfile = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
            string[] devJreBases = new string[] {
                Path.Combine(userProfile, ".antigravity-ide", "extensions"),
                Path.Combine(userProfile, ".vscode", "extensions")
            };

            foreach (string baseDev in devJreBases)
            {
                if (Directory.Exists(baseDev))
                {
                    try
                    {
                        foreach (string extDir in Directory.GetDirectories(baseDev, "redhat.java*"))
                        {
                            string jreSub = Path.Combine(extDir, "jre");
                            if (Directory.Exists(jreSub))
                            {
                                foreach (string vDir in Directory.GetDirectories(jreSub))
                                {
                                    string cand = Path.Combine(vDir, "bin", "javaw.exe");
                                    if (File.Exists(cand) && IsJava21OuMaior(cand)) return cand;
                                }
                            }
                        }
                    }
                    catch { }
                }
            }

            // 5. Verificar o javaw padrão do PATH (apenas se for versão >= 21)
            try
            {
                ProcessStartInfo wherePsi = new ProcessStartInfo("where", "javaw.exe")
                {
                    RedirectStandardOutput = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                };
                using (Process p = Process.Start(wherePsi))
                {
                    string outStr = p.StandardOutput.ReadLine();
                    p.WaitForExit();
                    if (!string.IsNullOrEmpty(outStr) && File.Exists(outStr.Trim()))
                    {
                        string cand = outStr.Trim();
                        if (IsJava21OuMaior(cand)) return cand;
                    }
                }
            }
            catch { }

            return null;
        }

        static bool IsJava21OuMaior(string javawPath)
        {
            try
            {
                string binDir = Path.GetDirectoryName(javawPath);
                string javaExe = Path.Combine(binDir, "java.exe");
                if (!File.Exists(javaExe)) javaExe = javawPath;

                ProcessStartInfo psi = new ProcessStartInfo(javaExe, "-version")
                {
                    RedirectStandardError = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                };

                using (Process p = Process.Start(psi))
                {
                    string output = p.StandardError.ReadToEnd();
                    p.WaitForExit(3000);

                    // Formatos típicos:
                    // openjdk version "21.0.12"
                    // java version "1.8.0_503"
                    Match m = Regex.Match(output, @"version\s+""?(\d+)(\.|\"")");
                    if (m.Success)
                    {
                        int major = int.Parse(m.Groups[1].Value);
                        if (major >= 21) return true;
                        // Se for 1 (ex: 1.8), é Java 8 -> incompatível
                        return false;
                    }
                }
            }
            catch { }
            return false;
        }
    }
}
