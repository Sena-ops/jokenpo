import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

    private static String host;
    private static Integer port = 5000;

    private static String regrasJogo = "\nRegras:\n - (R)ocha vence (T)esoura\n"
            + " - (T)esoura vence (P)apel\n - (P)apel vence (R)ocha\n";

    public static void main(String args[]) throws Exception {
        String input = "";
        String response;
        String mode = "";
        int wins = 0, losses = 0, ties = 0;

        System.out.println("--- Bem-Vindo ao Jokenpo (Cliente) ---");

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        // Solicitar o endereço IP do servidor
        System.out.println("Por favor, insira o endereço IP do servidor:");
        host = inFromUser.readLine();

        while (true) {
            Socket clientSocket = new Socket(Client.host, Client.port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Escolha do modo de jogo
            do {
                System.out.println("Escolha o modo de jogo: (1) Contra outro jogador, (2) Contra a CPU:");
                mode = inFromUser.readLine();
            } while (!mode.equals("1") && !mode.equals("2"));

            // Envia o modo escolhido para o servidor
            outToServer.writeBytes(mode + "\n");

            do {
                if (input.equals("-regras")) {
                    System.out.println(Client.regrasJogo);
                }

                System.out.println("Comece o jogo selecionando (R)ocha (P)apel, (T)esoura ou escreva \"-regras\" para ler as regras: ");
                input = inFromUser.readLine();
            } while (!input.equals("R") && !input.equals("P") && !input.equals("T"));

            outToServer.writeBytes(input + "\n");
            System.out.println("Sua resposta (" + input + ") foi transmitida para o servidor. Aguarde pelo resultado...");

            response = inFromServer.readLine();
            System.out.println("Resposta do Servidor: " + response);

            // Atualiza estatísticas
            if (response.contains("Parabens, voce venceu")) {
                wins++;
            } else if (response.contains("Que pena, voce perdeu")) {
                losses++;
            } else if (response.contains("Empate")) {
                ties++;
            }

            // Mostra estatísticas atuais
            System.out.println("Estatísticas atuais:");
            System.out.println("Vitorias: " + wins);
            System.out.println("Derrotas: " + losses);
            System.out.println("Empates: " + ties);

            clientSocket.close();

            // Pergunta ao usuário se ele quer jogar novamente
            System.out.println("Deseja jogar novamente? (s/n): ");
            String playAgain = inFromUser.readLine();
            if (!playAgain.equalsIgnoreCase("s")) {
                System.out.println("Obrigado por jogar! Ate a proxima.");
                break;
            }
        }
    }
}
