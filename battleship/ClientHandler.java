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
    public static ArrayList<String> playerList = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String player;
    public static HashMap<String, String> data = new HashMap<String, String>();
    public static int count = 0;
    public static String[] temp; 
    public static String state = "Wait";
    private static ArrayList<Integer> value = new ArrayList<>();

    public ClientHandler(Socket socket, int playerCount) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.player = bufferedReader.readLine();
            clientHandlers.add(this);
            playerList.add(this.player);
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
                            if (count == 2) {
                                state = "Deploy";
                                count = 0;
                            }
                        }
                    }
                }
                if (state.equalsIgnoreCase("Deploy")) {
                    deployShip();
                    state = "Battle";
                    count = 0;
                }
                if (state.equalsIgnoreCase("Battle")) {
                    for (ClientHandler clientHandler : clientHandlers) {
                        if (!clientHandler.player.equals(player)) {
                            for (String players: playerList) {
                                if (clientHandler.player == players) {
                                    data.put(players, messageFromClient);
                                }
                            }
                            count += 1;
                            if (count == 3) {
                                if (data.get(playerList.get(0)).equalsIgnoreCase("0") || data.get(playerList.get(1)).equalsIgnoreCase("0")) {
                                    state = "EndGame";
                                    System.out.println("EndGame");
                                    broadcastMessage("EndGame");
                                    Thread.sleep(200);
                                }
                                else {
                                    broadcastMessage("Shoot");
                                    count = 1;
                                    data.forEach((key, value) 
                                    -> System.out.println("Player" + key + " Ships : " + value));
                                }
                            }
                        }
                    }
                    if (state.equalsIgnoreCase("EndGame")){
                        System.out.println("===================================================");
                        for (String player: playerList) {
                            value.add(Integer.parseInt(data.get(player)));
                        }
                        if (value.get(0) == value.get(1)) {
                            broadcastMessage("Draw");
                        }
                        else if (value.get(0) > value.get(1)) {
                            broadcastMessage("Winner is : Player" + playerList.get(0));
                        }
                        else {
                            broadcastMessage("Winner is : Player" + playerList.get(1));
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void deployShip() {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                for (String players: playerList) {
                    if (clientHandler.player != players)
                    clientHandler.bufferedWriter.write(data.get(players));
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
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
