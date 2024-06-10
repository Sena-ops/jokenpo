import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private static Integer port = 5000;
    private static ConcurrentHashMap<Integer, Socket> waitingPlayers = new ConcurrentHashMap<>();
    private static AtomicInteger playerCounter = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        System.out.println("--- Bem-Vindo ao Jokenpo (Servidor) ---");

        port = getPort();
        ServerSocket welcomeSocket = new ServerSocket(port);

        // Exibir o endereço IP do servidor
        String serverIp = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Servidor iniciado no IP: " + serverIp + " e porta: " + port);

        ExecutorService pool = Executors.newFixedThreadPool(10);

        while (true) {
            Socket client = welcomeSocket.accept();
            int playerId = playerCounter.incrementAndGet();
            System.out.println("Jogador " + playerId + " conectado: " + client.getInetAddress().getHostAddress());
            pool.execute(new ClientHandler(client, playerId, welcomeSocket));
        }
    }

    private static int getPort() {
        Integer input;
        try (Scanner sc = new Scanner(System.in)) {
            do {
                System.out.println("Por favor digite o numero da porta, que esteja entre 1 e 65535 ou \ndigite \"0\" para continuar com a porta padrao(" + port + "): ");
                input = sc.nextInt();
            } while (input != 0 && !portaValida(input));
        }
        return input == 0 ? port : input;
    }

    public static boolean portaValida(Integer port) {
        return port >= 1 && port <= 65535;
    }

    private static class ClientHandler implements Runnable {
        private Socket client;
        private int playerId;
        private ServerSocket welcomeSocket;

        public ClientHandler(Socket client, int playerId, ServerSocket welcomeSocket) {
            this.client = client;
            this.playerId = playerId;
            this.welcomeSocket = welcomeSocket;
        }

        @Override
        public void run() {
            try {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());

                String modeClient = inFromClient.readLine();

                if (modeClient.equals("1")) { // Modo contra outro jogador
                    System.out.println("Jogador " + playerId + " escolheu jogar contra outro jogador.");

                    waitingPlayers.put(playerId, client);
                    while (waitingPlayers.size() < 2) {
                        Thread.sleep(1000); // Aguardar até que outro jogador entre na fila
                    }

                    int opponentId = waitingPlayers.keySet().stream()
                            .filter(id -> id != playerId)
                            .findFirst()
                            .orElse(-1);

                    if (opponentId != -1) {
                        Socket client2 = waitingPlayers.remove(opponentId);
                        waitingPlayers.remove(playerId);

                        System.out.println("Jogadores " + playerId + " e " + opponentId + " conectados. Iniciando jogo...");

                        DataOutputStream outToClient2 = new DataOutputStream(client2.getOutputStream());
                        BufferedReader inFromClient2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

                        outToClient.writeBytes("Jogador 2 conectado. Vamos comecar!\n");
                        outToClient2.writeBytes("Jogador 1 conectado. Vamos comecar!\n");

                        String inputClient1 = inFromClient.readLine();
                        String inputClient2 = inFromClient2.readLine();

                        String outputClient1 = getResult(inputClient1, inputClient2);
                        String outputClient2 = getResult(inputClient2, inputClient1);

                        outToClient.writeBytes(outputClient1 + "\n");
                        outToClient2.writeBytes(outputClient2 + "\n");

                        client2.close();
                    } else {
                        System.out.println("Erro ao encontrar oponente para o jogador " + playerId);
                    }
                } else if (modeClient.equals("2")) { // Modo contra CPU
                    System.out.println("Modo contra CPU selecionado.");

                    String inputClient = inFromClient.readLine();
                    String inputCPU = generateCPUChoice();

                    String outputClient = getResult(inputClient, inputCPU);

                    outToClient.writeBytes(outputClient + "\n");
                }

                client.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        private String generateCPUChoice() {
            String[] choices = {"R", "P", "T"};
            Random rand = new Random();
            return choices[rand.nextInt(choices.length)];
        }

        private String getResult(String player, String opponent) {
            if (player.equals(opponent)) {
                return "Empate";
            } else if ((player.equals("R") && opponent.equals("T")) ||
                    (player.equals("T") && opponent.equals("P")) ||
                    (player.equals("P") && opponent.equals("R"))) {
                return "Parabens, voce venceu! :) ";
            } else {
                return "Que pena, voce perdeu! :( ";
            }
        }
    }
}
