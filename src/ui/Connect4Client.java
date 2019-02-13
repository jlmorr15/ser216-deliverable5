package ui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connect4Client {
	//Which player are we?
	private int playerNumber;

	//last status code
	int currentStatus;

	//Store the last move made.
	int[] lastMove;

	// Indicate whether the player has the turn
	private boolean myTurn = false;
	private boolean firstTurn = true;

	// Indicate selected row and column by the current move
	private int rowSelected;
	private int columnSelected;

	// Input and output streams from/to server
	private DataInputStream fromServer;
	private DataOutputStream toServer;

	// Continue to play?
	private boolean continueToPlay = true;
	private int winningPlayer;

	// Wait for the player to mark a cell
	private boolean waiting = true;

	// Host name or ip
	private String host = "localhost";


	/**
	 * Initiate connection to server in constructor
	 */
	public Connect4Client() {
		lastMove = new int[3];
		connectToServer();
	}

	/**
	 * Open a socket to the server
	 */
	private void connectToServer() {

		try {
			// Create a socket to connect to the server
			Socket socket = new Socket(host, 8888);

			// Create an input stream to receive data from the server
			fromServer = new DataInputStream(socket.getInputStream());

			// Create an output stream to send data to the server
			toServer = new DataOutputStream(socket.getOutputStream());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Control the game on a separate thread
		new Thread(() -> {
			try {
				// Get a player number from server
				receiveInfoFromServer();
				if(whoAmI()==1) {
					receiveInfoFromServer(); //get status
					myTurn = true;
				}

				if(getStatus()==-1) {
					receiveInfoFromServer(); //wait for player 2
				}

				System.out.println("Pre Status: "+getStatus());

				// Continue to play
				while (continueToPlay) {
					if(!firstTurn) {
						receiveInfoFromServer(); // Receive other player's move
						if((getStatus()-10)==playerNumber)
							myTurn=true;
					} else {
						firstTurn = false;
					}
					if(isMyTurn()) {
						do{
							waiting = true;
							waitForPlayerAction(); // Wait for player 1 to move
							receiveInfoFromServer(); // Receive result of move
						} while((getStatus()>=400 && getStatus()<=407) || getStatus()==500);
						myTurn = !myTurn;
						if(getStatus()-100 == playerNumber) {
							continueToPlay=false;
							winningPlayer = getStatus()-100;
						}
					}
					System.out.println("Status: "+getStatus());
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	/**
	 * Send a move to the client and stop waiting on player
	 * @param col
	 * @throws IOException
	 */
	public void sendMove(int col) throws IOException {
		if(isMyTurn()) {
			toServer.writeInt(col); // Send the selected column
			waiting = false;
		}
	}

	/**
	 * Who made the last move, and where?
	 * @return
	 */
	public int[] getLastMove() {
		return lastMove;
	}

	/**
	 * Is it my turn?
	 * @return
	 */
	public boolean isMyTurn() {
		//return (currentStatus-10)==whoAmI();
		return myTurn;
	}

	/**
	 * Wait for player input
	 * @throws InterruptedException
	 */
	private void waitForPlayerAction() throws InterruptedException {
		while (waiting) {
			Thread.sleep(100);
		}

		waiting = true;
	}

	/**
	 * Which player am I?
	 * @return Player ID
	 */
	public int whoAmI() {
		return playerNumber;
	}

	/**
	 * Status of the game
	 * @return
	 */
	public int getStatus() {
		return currentStatus;
	}

	/**
	 * Is the game running?
	 * @return
	 */
	public boolean isGameStarted() {
		return (getStatus() != -1 && whoWon()==0);
	}

	/**
	 * Who won the game?
	 * @return
	 */
	public int whoWon() {
		if(!continueToPlay)
			return winningPlayer;
		return 0;
	}

	/**
	 * Receives data from the server about the status of the game.
	 * Server Status Codes - Lets keep it organized eh?
	 * 1|2 : Identifies your player #
	 * 40X : Error Column X is full
	 * 500 : Internal Server errors, not our fault, not our problem...
	 * 777 : Indicates the next 3 transmissions are the player that moved, the row, and then the column
	 * 999 : Tell player 1 to start.
	 * 10X : Player X wins
	 * 1X  : Player X's turn
	 * -1  : Waiting for player 2 to join
	 * @throws IOException
	 */
	private void receiveInfoFromServer() throws IOException {
		// Receive game/server status
		int status = fromServer.readInt();

		switch (status) {
			case 1:
				playerNumber = 1;
				break;
			case 2:
				playerNumber = 2;
				break;
			case 400:
			case 401:
			case 402:
			case 403:
			case 404:
			case 405:
			case 406:
				//column x is full
				currentStatus = status;
				break;
			case 500:
				currentStatus = 500;
				break;
			case 777:
				//next 3 transmissions define the move made.
				lastMove[0] = fromServer.readInt();
				lastMove[1] = fromServer.readInt();
				lastMove[2] = fromServer.readInt();
				receiveInfoFromServer();
				break;
			case 999:
				currentStatus = 11;
				break;
			case 101:
			case 102:
				currentStatus = status;
				winningPlayer = status-100;
				continueToPlay = false;
				lastMove[0] = fromServer.readInt(); //get the winning player
				lastMove[1] = fromServer.readInt(); //row
				lastMove[2] = fromServer.readInt(); //column
				break;
			case 11:
			case 12:
				currentStatus = status;
				/*
				if(status==11 && whoAmI()==1)
					myTurn=true;
				else if(status==12 && whoAmI()==2)
					myTurn=true;
				else
					myTurn=false;
				 */
				break;
			case -1:
				currentStatus = status;
				break;
		}
	}
}