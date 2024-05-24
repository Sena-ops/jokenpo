
import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class Server {

    private static Integer port = 5000;

    public static void main(String args[]) throws Exception {
        String outputClient1 = "";
        String outputClient2 = "";
        String inputClient1;
        String inputClient2;

        System.out.println("--- Bem-Vindo ao Jokenpo (Servidor) ---");

        Server.port = Server.getPort();
        ServerSocket welcomeSocket = new ServerSocket(Server.port);

        System.out.println("Ok, estamos trabalhando na porta " + welcomeSocket.getLocalPort() + "...");

        while (!welcomeSocket.isClosed()) {
            Socket client1 = welcomeSocket.accept();
            if (client1.isConnected()) {
                System.out.println("Jogador 1 (" + (client1.getLocalAddress().toString()).substring(1) + ":" + client1.getLocalPort() + ") entrou... Escolhendo modo de jogo...");
            }
            DataOutputStream outToClient1 = new DataOutputStream(client1.getOutputStream());
            BufferedReader inFromClient1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));

            String mode = inFromClient1.readLine();

            if (mode.equals("1")) { // Modo contra outro jogador
                System.out.println("Modo contra outro jogador selecionado.");

                Socket client2 = welcomeSocket.accept();
                if (client2.isConnected()) {
                    System.out.println("Jogador 2 (" + (client2.getLocalAddress().toString()).substring(1) + ":" + client1.getLocalPort() + ") entrou... Vamos começar!");
                }
                DataOutputStream outToClient2 = new DataOutputStream(client2.getOutputStream());
                BufferedReader inFromClient2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

                inputClient1 = inFromClient1.readLine();
                inputClient2 = inFromClient2.readLine();

                outputClient1 = getResult(inputClient1, inputClient2);
                outputClient2 = getResult(inputClient2, inputClient1);

                outToClient1.writeBytes(outputClient1 + "\n");
                outToClient2.writeBytes(outputClient2 + "\n");

                client2.close();
            } else if (mode.equals("2")) { // Modo contra CPU
                System.out.println("Modo contra CPU selecionado.");

                inputClient1 = inFromClient1.readLine();
                inputClient2 = generateCPUChoice();

                System.out.println("Jogada do jogador: " + inputClient1 + " | Jogada da CPU: " + inputClient2);

                outputClient1 = getResult(inputClient1, inputClient2);

                outToClient1.writeBytes(outputClient1 + "\n");
            }

            client1.close();
            System.out.println("Esperando um novo jogo...");
        }
    }

    private static String generateCPUChoice() {
        String[] choices = {"R", "P", "T"};
        Random rand = new Random();
        return choices[rand.nextInt(choices.length)];
    }

    private static String getResult(String player, String opponent) {
        if (player.equals(opponent)) {
            return "Empate";
        } else if ((player.equals("R") && opponent.equals("T")) ||
                (player.equals("T") && opponent.equals("P")) ||
                (player.equals("P") && opponent.equals("R"))) {
            return "Parabéns, você venceu! :) ";
        } else {
            return "Que pena, você perdeu! :( ";
        }
    }

    public static boolean portaValida(Integer port) {
        return port >= 1 && port <= 65535;
    }

    private static int getPort() {
        Integer input;
        try (Scanner sc = new Scanner(System.in)) {
            do {
                System.out.println("Por favor digite o número da porta, que esteja entre 1 e 65535 ou \ndigite \"0\" para continuar com a porta padrão(" + Server.port + "): ");
                input = sc.nextInt();
            } while (input != 0 && !Server.portaValida(input));
        }
        return input == 0 ? Server.port : input;
    }
}
