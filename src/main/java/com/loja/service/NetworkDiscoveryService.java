package com.loja.service;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

/**
 * Serviço genérico e 100% dinâmico para identificação de adaptadores de rede
 * e localização de servidores MySQL ativos na rede local (LAN / Wi-Fi).
 *
 * Não utiliza nenhum IP fixo, nome de rede proprietário ou presunção de ambiente.
 */
public class NetworkDiscoveryService {

    public static class ServidorDescoberto {
        private final String titulo;
        private final String host;
        private final int porta;
        private final String detalhes;

        public ServidorDescoberto(String titulo, String host, int porta, String detalhes) {
            this.titulo = titulo;
            this.host = host;
            this.porta = porta;
            this.detalhes = detalhes;
        }

        public String getTitulo() { return titulo; }
        public String getHost() { return host; }
        public int getPorta() { return porta; }
        public String getDetalhes() { return detalhes; }

        @Override
        public String toString() {
            return titulo + " [" + host + ":" + porta + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ServidorDescoberto that = (ServidorDescoberto) o;
            return porta == that.porta && Objects.equals(host, that.host);
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, porta);
        }
    }

    /**
     * Retorna a lista de endereços IPv4 ativos configurados nos adaptadores desta máquina.
     */
    public static List<String> obterIpsLocais() {
        List<String> ips = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    NetworkInterface ni = interfaces.nextElement();
                    if (!ni.isUp() || ni.isLoopback()) continue;
                    for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                        InetAddress addr = ia.getAddress();
                        if (addr instanceof Inet4Address && !addr.isLoopbackAddress() && !addr.isLinkLocalAddress()) {
                            String ip = addr.getHostAddress();
                            if (!ips.contains(ip)) {
                                ips.add(ip);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return ips;
    }

    /**
     * Retorna o IP principal desta máquina na rede local atual para exibição informativa.
     */
    public static String obterIpPrincipal() {
        List<String> ips = obterIpsLocais();
        if (ips.isEmpty()) {
            return "127.0.0.1";
        }
        // Prioriza faixas padrão de rede local (192.168.x.x ou 10.x.x.x)
        for (String ip : ips) {
            if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                return ip;
            }
        }
        return ips.get(0);
    }

    /**
     * Identifica dinamicamente os prefixos de sub-rede (/24) a partir dos adaptadores IPv4 ativos.
     */
    public static List<String> obterPrefixosSubredeLocais() {
        List<String> subnets = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    NetworkInterface ni = interfaces.nextElement();
                    if (!ni.isUp() || ni.isLoopback()) continue;
                    for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                        InetAddress addr = ia.getAddress();
                        if (addr instanceof Inet4Address && !addr.isLoopbackAddress() && !addr.isLinkLocalAddress()) {
                            String ip = addr.getHostAddress();
                            int lastDot = ip.lastIndexOf('.');
                            if (lastDot > 0) {
                                String prefix = ip.substring(0, lastDot + 1);
                                if (!subnets.contains(prefix)) {
                                    subnets.add(prefix);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return subnets;
    }

    /**
     * Executa varredura multithread paralela na sub-rede local desta máquina
     * para encontrar servidores MySQL respondendo nas portas 3306 e 3307.
     */
    public static void buscarServidoresAsync(Consumer<List<ServidorDescoberto>> onComplete, Consumer<String> onProgress) {
        new Thread(() -> {
            if (onProgress != null) {
                SwingUtilities.invokeLater(() -> onProgress.accept("Identificando adaptadores de rede desta máquina..."));
            }

            Set<String> ipsDestaMaquina = new HashSet<>(obterIpsLocais());
            Set<String> alvos = new LinkedHashSet<>();

            // 1. Sempre inclui localhost
            alvos.add("127.0.0.1");

            // 2. Inclui os próprios IPs desta máquina
            alvos.addAll(ipsDestaMaquina);

            // 3. Identifica as sub-redes ativas desta máquina e adiciona para varredura
            List<String> prefixos = obterPrefixosSubredeLocais();
            for (String prefix : prefixos) {
                for (int i = 1; i <= 254; i++) {
                    alvos.add(prefix + i);
                }
            }

            if (onProgress != null) {
                SwingUtilities.invokeLater(() -> onProgress.accept("Verificando servidores na rede local..."));
            }

            int[] portas = {3306, 3307};
            List<ServidorDescoberto> encontrados = new CopyOnWriteArrayList<>();
            ExecutorService pool = Executors.newFixedThreadPool(50);

            for (String ip : alvos) {
                for (int porta : portas) {
                    pool.submit(() -> {
                        try (Socket s = new Socket()) {
                            s.connect(new InetSocketAddress(ip, porta), 250);

                            // Conexão TCP aceita!
                            String titulo;
                            String detalhes;

                            if (ip.equals("127.0.0.1") || ip.equalsIgnoreCase("localhost")) {
                                titulo = "Este Computador (Local)";
                                detalhes = "Servidor em execução local";
                            } else if (ipsDestaMaquina.contains(ip)) {
                                titulo = "Este Computador (IP na Rede)";
                                detalhes = "IP local acessível por outros computadores";
                            } else {
                                titulo = "Servidor na Rede Local";
                                detalhes = "Servidor ativo no endereço " + ip;
                            }

                            ServidorDescoberto srv = new ServidorDescoberto(titulo, ip, porta, detalhes);
                            if (!encontrados.contains(srv)) {
                                encontrados.add(srv);
                            }
                        } catch (Exception ignored) {}
                    });
                }
            }

            pool.shutdown();
            try {
                pool.awaitTermination(4, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {}

            // Ordenação amigável: Localhost e Este Computador primeiro, depois servidores remotos
            List<ServidorDescoberto> ordenados = new ArrayList<>(encontrados);
            ordenados.sort((a, b) -> {
                if (a.getHost().equals("127.0.0.1")) return -1;
                if (b.getHost().equals("127.0.0.1")) return 1;
                if (ipsDestaMaquina.contains(a.getHost()) && !ipsDestaMaquina.contains(b.getHost())) return -1;
                if (!ipsDestaMaquina.contains(a.getHost()) && ipsDestaMaquina.contains(b.getHost())) return 1;
                return a.getHost().compareTo(b.getHost());
            });

            if (onComplete != null) {
                SwingUtilities.invokeLater(() -> onComplete.accept(ordenados));
            }
        }).start();
    }
}
