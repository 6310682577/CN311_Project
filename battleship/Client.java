package battleship;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.Scanner;

// A simple Client Server Protocol .. Client for Echo Server

public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String player;
    private boolean status = true;
    
    public static String phase = "preparing";
    public static int numRows = 10;
    public static int numCols = 10;
    public static int playerShips;
    public static int opponentShips;
    public static String[][] grid = new String[numRows][numCols];
    public static int[][] missedGuesses = new int[numRows][numCols];


    public Client(Socket socket, String player) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.player = player;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            bufferedWriter.write(player);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            
            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                if (status == true) {
                    String messageToSend = scanner.nextLine();
                    bufferedWriter.write(player + ": " + messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    }
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromServer;

                while (socket.isConnected()) {
                    if (phase.equalsIgnoreCase("preparing")) {
                        try {
                            status = false;
                            msgFromServer = bufferedReader.readLine();
                            System.out.println(msgFromServer);
                            if (msgFromServer.equalsIgnoreCase("Start")) {
                                Thread.sleep(200);
                                createOceanMap();
                                deployPlayerShips();
                                status = true;
                                phase = "Battle";
                            }
                        } catch (IOException | InterruptedException e) {
                            closeEverything(socket, bufferedReader, bufferedWriter);
                        }
                    }
                    else if (phase.equalsIgnoreCase("Battle")) {
                        try {
                            msgFromServer = bufferedReader.readLine();
                            System.out.println(msgFromServer);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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


    // BattleShip
    public static void createOceanMap(){
        //First section of Ocean Map
        System.out.print("  ");
        for(int i = 0; i < numCols; i++)
                System.out.print(i);
        System.out.println();

        //Middle section of Ocean Map
        for(int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = " ";
                if (j == 0)
                    System.out.print(i + "|" + grid[i][j]);
                else if (j == grid[i].length - 1)
                    System.out.print(grid[i][j] + "|" + i);
                else
                    System.out.print(grid[i][j]);
            }
            System.out.println();
        }

        //Last section of Ocean Map
        System.out.print("  ");
        for(int i = 0; i < numCols; i++)
            System.out.print(i);
        System.out.println();
    }

    public static void deployPlayerShips(){
        Scanner input = new Scanner(System.in);
        System.out.println("\nDeploy your ships:");
        //Deploying five ships for player
        playerShips = 5;
        opponentShips = 5;
        for (int i = 1; i <= playerShips; ) {
            System.out.print("Enter (X Y) coordinate for your " + i + " ship: ");
            String inp = input.nextLine();

            String[] temp = inp.split(" ");

            int y = Integer.parseInt(temp[0]);
            int x = Integer.parseInt(temp[1]);

            if((x >= 0 && x < numRows) && (y >= 0 && y < numCols) && (grid[x][y] == " "))
            {
                grid[x][y] =  "@";
                i++;
            }
            else if((x >= 0 && x < numRows) && (y >= 0 && y < numCols) && grid[x][y] == "@")
                System.out.println("You can't place two or more ships on the same location");
            else if((x < 0 || x >= numRows) || (y < 0 || y >= numCols))
                System.out.println("You can't place ships outside the " + numRows + " by " + numCols + " grid");
        }
        printOceanMap();
    }

    public static void printOceanMap(){
        System.out.println();
        //First section of Ocean Map
        System.out.print("  ");
        for(int i = 0; i < numCols; i++)
            System.out.print(i);
        System.out.println();

        //Middle section of Ocean Map
        for(int x = 0; x < grid.length; x++) {
            System.out.print(x + "|");

            for (int y = 0; y < grid[x].length; y++){
                System.out.print(grid[x][y]);
            }

            System.out.println("|" + x);
        }

        //Last section of Ocean Map
        System.out.print("  ");
        for(int i = 0; i < numCols; i++)
            System.out.print(i);
        System.out.println();
    }

    public static void playerTurn(String input){
        System.out.println("\nYOUR TURN");
        int x = -1, y = -1;
        do {
            String[] temp = input.split(" ");

            y = Integer.parseInt(temp[0]);
            x = Integer.parseInt(temp[1]);

            if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols)) //valid guess
            {
                if (grid[x][y] == "x") //if computer ship is already there; computer loses ship
                {
                    System.out.println("Boom! You sunk the ship!");
                    grid[x][y] = "!"; //Hit mark
                    --opponentShips;
                }
                else if (grid[x][y] == "@") {
                    System.out.println("Oh no, you sunk your own ship :(");
                    grid[x][y] = "x";
                    --playerShips;
                }
                else if (grid[x][y] == " ") {
                    System.out.println("Sorry, you missed");
                    grid[x][y] = "-";
                }
            }
            else if ((x < 0 || x >= numRows) || (y < 0 || y >= numCols))  //invalid guess
                System.out.println("You can't place ships outside the " + numRows + " by " + numCols + " grid");
        }while((x < 0 || x >= numRows) || (y < 0 || y >= numCols));  //keep re-prompting till valid guess
    }

    public static void main(String[] args) throws UnknownHostException, IOException {

        System.out.println("Enter Name: ");
        Scanner scanner = new Scanner(System.in);
        String player = scanner.nextLine();
        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket, player);
        client.listenForMessage();
        client.sendMessage();
    }
}
