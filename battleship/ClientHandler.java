package battleship;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String player;
    public static HashMap<String, String> data = new HashMap<String, String>();
    public static int count = 0;
    public static String[] temp; 
    public static String state = "Wait";

    public ClientHandler(Socket socket, int playerCount) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.player = bufferedReader.readLine();
            clientHandlers.add(this);
            data.put(player, "Hello");
            broadcastMessage("Player " + playerCount + " has ready!");
            if (playerCount == 2) {
                System.out.println("playerCount : " + playerCount);
                broadcastMessage("Start");
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (state.equalsIgnoreCase("Wait")) {
                    for (ClientHandler clientHandler : clientHandlers) { 
                        if (!clientHandler.player.equals(player)) {
                            data.put(player, messageFromClient);
                            count += 1;
                            System.out.println(count);
                            if (count == 2) {
                                state = "Deploy";
                                count = 0;
                            }
                        }
                    }
                }
                if (state.equalsIgnoreCase("Deploy")) {
                    deployShip();
                }
                // data.forEach((key, value) -> System.out.println(key + "=" + value));
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void deployShip() {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler.player.equals(player)) {
                    clientHandler.bufferedWriter.write(data.get(player));
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                    System.out.println(player + " " + data.get(player));
                    Thread.sleep(200);
                }
            } catch (IOException | InterruptedException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void broadcastMessage(String messagetoSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                clientHandler.bufferedWriter.write(messagetoSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + player + " has left the game!");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null)
                bufferedReader.close();
            if (bufferedWriter != null)
                bufferedWriter.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
