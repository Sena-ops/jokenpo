import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

    private static String host;
    private static Integer port;
    private static String regrasJogo = "\nRegras:\n - (R)ocha vence (T)esoura\n"
            + " - (T)esoura vence (P)apel\n - (P)apel vence (R)ocha\n";
    private static int vitorias = 0;
    private static int derrotas = 0;
    private static int empates = 0;

    public static void main(String[] args) throws Exception {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("--- Bem-Vindo ao Jokenpo (Cliente) ---");
        System.out.print("Digite o IP do servidor: ");
        host = inFromUser.readLine();
        System.out.print("Digite a porta do servidor: ");
        port = Integer.parseInt(inFromUser.readLine());

        while (true) {
            System.out.println("Selecione o modo de jogo:");
            System.out.println("1. Jogar contra outro jogador");
            System.out.println("2. Jogar contra a CPU");

            String mode = inFromUser.readLine();
            while (!mode.equals("1") && !mode.equals("2")) {
                System.out.println("Opção inválida. Selecione novamente.");
                mode = inFromUser.readLine();
            }

            Socket clientSocket = new Socket(host, port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            outToServer.writeBytes(mode + "\n");

            if (mode.equals("1")) {
                System.out.println("Aguardando outro jogador...");
                System.out.println(inFromServer.readLine()); // Jogador 2 conectado. Vamos começar!
            }

            String input;
            do {
                System.out.println("Selecione (R)ocha, (P)apel, (T)esoura ou escreva \"-regras\" para ler as regras:");
                input = inFromUser.readLine();
                if (input.equals("-regras")) {
                    System.out.println(regrasJogo);
                }
            } while (!input.equals("R") && !input.equals("P") && !input.equals("T"));

            outToServer.writeBytes(input + "\n");
            System.out.println("Sua resposta (" + input + ") foi transmitida para o servidor. Aguarde pelo resultado...");

            String response = inFromServer.readLine();
            System.out.println("Resposta do Servidor: " + response);

            updateStats(response);

            System.out.println("Estatísticas: Vitórias: " + vitorias + ", Derrotas: " + derrotas + ", Empates: " + empates);

            String playAgain;
            do {
                System.out.println("Deseja jogar novamente? (s/n)");
                playAgain = inFromUser.readLine();
            } while (!playAgain.equalsIgnoreCase("s") && !playAgain.equalsIgnoreCase("n"));

            if (playAgain.equalsIgnoreCase("n")) {
                break;
            }

            clientSocket.close();
        }
    }

    private static void updateStats(String response) {
        if (response.contains("venceu")) {
            vitorias++;
        } else if (response.contains("perdeu")) {
            derrotas++;
        } else if (response.contains("Empate")) {
            empates++;
        }
    }
}
