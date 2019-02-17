package test;

import core.Connect4Server;
import org.junit.jupiter.api.Test;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class Connect4ServerTest {
    private static Connect4Server server = new Connect4Server();
    @Test
    void start() {

        try {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    new JFXPanel(); // Initializes the JavaFx Platform
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {

                            try {
                                server.start(new Stage()); // Create and
                                // initialize app.
                                DataInputStream fromServer;
                                // Create a socket to connect to the server
                                Socket socket = new Socket("127.0.0.1", 8888);
                                // Create an input stream to receive data from the server
                                fromServer = new DataInputStream(socket.getInputStream());

                                int status = fromServer.readInt();
                                assertNotEquals(status,33);
                                assertTrue(status==1 || status == 2); //assert that we can connect to server.
                            } catch (IOException ex) {
                                fail("IO Exception");
                            }
                        }
                    });
                }
            });

            thread.start();// Initialize the thread
            Thread.sleep(10000);
        } catch (Exception ex) {
            fail("Exception Thrown");
        }
    }
}