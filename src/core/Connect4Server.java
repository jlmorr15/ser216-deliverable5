package core;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.NoSuchElementException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Connect4Server extends Application {
    public static int PLAYER1 = 1; // Indicate player 1
    public static int PLAYER2 = 2; // Indicate player 2

    private int sessionNo = 0;
    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {
        TextArea taLog = new TextArea();

        // Create a scene and place it in the stage
        Scene scene = new Scene(new ScrollPane(taLog), 450, 200);
        primaryStage.setTitle("Connect4 Server"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        new Thread( () -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(8888);
                Platform.runLater(() -> taLog.appendText(new Date() +
                        ": Server started at socket 8888\n"));

                // Ready to create a session for every two players
                while (true) {
                    Platform.runLater(() -> taLog.appendText(new Date() +
                            ": Wait for players to join session " + sessionNo + '\n'));

                    // Connect to player 1
                    Socket player1 = serverSocket.accept();

                    Platform.runLater(() -> {
                        taLog.appendText(new Date() + ": Player 1 joined session "
                                + sessionNo + '\n');
                        taLog.appendText("Player 1's IP address" +
                                player1.getInetAddress().getHostAddress() + '\n');
                    });

                    // Notify that the player is Player 1
                    new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);
                    //Notify Player 1 we are waiting for player 2.
                    new DataOutputStream(player1.getOutputStream()).writeInt(-1);
                    // Connect to player 2
                    Socket player2 = serverSocket.accept();

                    Platform.runLater(() -> {
                        taLog.appendText(new Date() +": Player 2 joined session " + sessionNo + '\n');
                        taLog.appendText("Player 2's IP address" + player2.getInetAddress().getHostAddress() + '\n');
                    });

                    // Notify that the player is Player 2
                    new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);

                    // Display this session and increment session number
                    Platform.runLater(() ->
                            taLog.appendText(new Date() +
                                    ": Start a thread for session " + sessionNo++ + '\n'));

                    // Launch a new thread for this session of two players
                    new Thread(new HandleASession(player1, player2)).start();
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    // Define the thread class for handling a new session for two players
    class HandleASession implements Runnable {
        private Socket player1;
        private Socket player2;
        private Connect4 game;



        // Continue to play
        private boolean continueToPlay = true;

        /** Construct a thread */
        private HandleASession(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;

            //Start the game.
            this.game = new Connect4("P");
        }

        private int getInputFromPlayer(DataInputStream fromPlayer, DataOutputStream toPlayer) {
            boolean filledColumn = false;
            int column = -1;
            try {
                do {
                    column = fromPlayer.readInt();

                    System.out.println("Received "+column+" from player");

                    if(!game.columnHasOpenSlots(column)) {
                        toPlayer.writeInt(400+column); //400 error on column number will signal GUI to buzz
                        filledColumn = true;
                    } else
                        this.game.move(this.game.getActivePlayer(),column);
                } while(filledColumn && !game.columnHasOpenSlots(column));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return column;
        }

        /** Implement the run() method for the thread */
        public void run() {
            try {
                // Create data input and output streams
                DataInputStream fromPlayer1 = new DataInputStream(
                        player1.getInputStream());
                DataOutputStream toPlayer1 = new DataOutputStream(
                        player1.getOutputStream());
                DataInputStream fromPlayer2 = new DataInputStream(
                        player2.getInputStream());
                DataOutputStream toPlayer2 = new DataOutputStream(
                        player2.getOutputStream());

                // Write anything to notify player 1 to start
                // This is just to let player 1 know to start
                toPlayer1.writeInt(999);

                // Continuously serve the players and determine and report
                // the game status to the players
                while (true) {
                    // Receive a move from active player if the game is still running.
                    if(game.gameIsActive()) {
                        int column;
                        if(this.game.getActivePlayer().signature.equals("X")) {
                            column = getInputFromPlayer(fromPlayer1, toPlayer1);
                        } else {
                            column = getInputFromPlayer(fromPlayer2, toPlayer2);
                        }

                        if(column == -1) {
                            //something bad happened!
                            toPlayer1.writeInt(500);
                            toPlayer2.writeInt(500);
                        } else {
                            try {
                                Connect4.GamePiece piece = game.getTopPieceInColumn(column);
                                if(game.checkWinConditions(piece)) {
                                    int winCode = 100+(game.getActivePlayer().signature.equals("X") ? 1:2);
                                    toPlayer1.writeInt(winCode);
                                    toPlayer2.writeInt(winCode);
                                    break;
                                } else {
                                    int whoMoved = game.getActivePlayer().signature.equals("X") ? 1:2;
                                    game.switchTurns();
                                    //both players need an update from server, not just the waiting player.
                                    //+1 row to account for control row
                                    sendMove(toPlayer2, whoMoved, piece.getRow()+1, piece.getCol());
                                    sendMove(toPlayer1, whoMoved, piece.getRow()+1, piece.getCol());
                                    //send each player who's turn it is now.
                                    if(this.game.getActivePlayer().signature.equals("X")) {
                                        toPlayer1.writeInt(11);
                                        toPlayer2.writeInt(11);
                                    } else {
                                        toPlayer1.writeInt(12);
                                        toPlayer2.writeInt(12);
                                    }
                                }
                            } catch (NoSuchElementException ex) {
                                toPlayer1.writeInt(500);
                                toPlayer2.writeInt(500);
                            }
                        }
                    } else {
                        break; //shouldn't really ever reach this, but y'know, just in case.
                    }
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }

        /** Send the move to other player */
        private void sendMove(DataOutputStream out, int player, int row, int column)
                throws IOException {
            out.writeInt(777);
            out.writeInt(player);
            out.writeInt(row); // Send row index
            out.writeInt(column); // Send column index
        }
    }
}
