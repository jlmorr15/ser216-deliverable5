package ui;

import core.Connect4;
import core.Connect4ComputerPlayer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Connect4GUI extends Application {
	private String type; //game type
	private Connect4 game;
	private Connect4ComputerPlayer ai;
	private StackPane rootPane;
	private GridPane grid;
	private GridPane menu;
	private ImageView p1View,p2View;

	//Game assets
	private Image p1ImageOn;
	private Image p1ImageOnCurrent;
	private Image p1ImageOff;
	private Image p1ImageOffCurrent;
	private Image p2ImageOn;
	private Image p2ImageOnCurrent;
	private Image p2ImageOff;
	private Image p2ImageOffCurrent;
	private Image cellEmpty;
	private Image cellP1;
	private Image cellP2;
	private Image btnPlay;
	private Image btnPlayOff;
	private Image btnColFull;
	private ImageView[][] board;
	private ImageView fullColumn;
	private int[][] boardMap;

	private Connect4.Player activePlayer;
	private MediaPlayer playerSound;
	private MediaPlayer winSound;
	private MediaPlayer buzzSound;

	private Connect4Client client;
	private int playerNumber;
	private Thread onlineGame;
	private Label dataLabel;


	/**
	 * Event handlers
	 */
	EventHandler<MouseEvent> colEnter = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			((ImageView)event.getSource()).setImage(btnPlay);
			event.consume();
		}
	};

	EventHandler<MouseEvent> colExit = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			((ImageView)event.getSource()).setImage(btnPlayOff);
			event.consume();
		}
	};

	EventHandler<MouseEvent> colClick = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			sendOnlineMove(((ImageView)event.getSource()).getId());
			event.consume();
		}
	};

	/**
	 * The main method is only needed for the IDE with limited
	 * JavaFX support. Not needed for running from the command line.
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	/**
	 * delay - delays execution for msec milliseconds
	 * @param msec milliseconds to delay the program for.
	 */
	public static void delay(int msec) {
		try {
			TimeUnit.MILLISECONDS.sleep(msec);
		} catch (InterruptedException e) {
			//oh well...
		}
	}

	/**
	 * GUI Constructor
	 */
	public Connect4GUI() {
		type = "C";
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Who would you like to play?");
		alert.setHeaderText("Would you like to play the computer or another player?");
		alert.setContentText("Choose your option.");
		ButtonType btnComputer = new ButtonType("Computer");
		ButtonType btnPlayer = new ButtonType("Player");
		ButtonType btnEnd = new ButtonType("Nevermind", ButtonBar.ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(btnComputer,btnPlayer,btnEnd);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == btnComputer){
			type = "C";
		} else if (result.isPresent() && result.get() == btnPlayer) {
			type = "P";
		} else {
			Platform.exit();
			System.exit(0);
		}

		String pSound = "src/ui/player.wav";     // For example
		Media playerMedia = new Media(new File(pSound).toURI().toString());
		playerSound = new MediaPlayer(playerMedia);

		String wSound = "src/ui/winner.mp3";     // For example
		Media winMedia = new Media(new File(wSound).toURI().toString());
		winSound = new MediaPlayer(winMedia);

		String bSound = "src/ui/buzz.wav";     // For example
		Media buzzMedia = new Media(new File(bSound).toURI().toString());
		buzzSound = new MediaPlayer(buzzMedia);

		if(type.equalsIgnoreCase("C")) {
			this.game = new Connect4(type); //start a new game.
			activePlayer = game.getActivePlayer();

			if(!game.multiplayerGame()) {
				this.ai = new Connect4ComputerPlayer(game.getPlayer(2));
			}
		}
	}


	private void launchOnlineGame() {
		onlineGame = new Thread() {
			public void run() {
				client = new Connect4Client();

				do {
					playerNumber = client.whoAmI();
					delay(300);
				} while(playerNumber==0);

				while(client.whoWon()==0 && playerNumber!=0) {
					//do online stuff
					Platform.runLater(() -> {
							if(client.getStatus()>=400 && client.getStatus()<=407) {
								int col = client.getStatus()-400;
								grid.getChildren().remove(board[0][col]);
								//board[0][col].setImage(btnColFull);
								//board[0][col].removeEventHandler(MouseEvent.MOUSE_CLICKED,colClick);
								//board[0][col].removeEventHandler(MouseEvent.MOUSE_ENTERED,colEnter);
								//board[0][col].removeEventHandler(MouseEvent.MOUSE_EXITED,colExit);
								grid.add(fullColumn,col,0);
							}
							int[] lastMove = client.getLastMove();
							boardMap[lastMove[1]][lastMove[2]] = lastMove[0];

							/* debug board view
							System.out.println("---------");
							for(int row=0; row<7; row++) {
								for(int col=0; col<7; col++)
									System.out.print("["+boardMap[row][col]+"] ");
								System.out.println("");
							}
							System.out.println("---------");
							 */

							renderBoardOnline();
						}
					);
					delay(300);
				}
				if(client.whoWon()>0) {
					Platform.runLater(() -> {
								int[] lastMove = client.getLastMove();
								boardMap[lastMove[1]][lastMove[2]] = lastMove[0];
								renderBoardOnline();
								announceWinnerOnline("Player "+client.whoWon());
							}
					);
				}
			}
		};
		onlineGame.start();
	}


	/**
	 * Starts a new game when the player selects a rematch.
	 */
	private void newGame() {
		this.game = new Connect4(type); //start a new game.
		activePlayer = game.getActivePlayer();

		if(!game.multiplayerGame()) {
			this.ai = new Connect4ComputerPlayer(game.getPlayer(2));
		}
		renderBoard();
	}

	/**
	 * Announces the winnter of the game
	 * @param player The winning player
	 */
	private void announceWinner(String player) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("GAME OVER!");
		alert.setHeaderText(player+" has won the game!");
		alert.setContentText("Rematch?");
		ButtonType btnRematch = new ButtonType("Yes");
		ButtonType btnEnd = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(btnRematch,btnEnd);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == btnRematch){
			newGame();
		} else {
			Platform.exit();
			System.exit(0);
		}
	}

	/**
	 *
	 * @param player
	 */
	private void announceWinnerOnline(String player) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("GAME OVER!");
		alert.setHeaderText(player+" has won the game!");
		ButtonType btnEnd = new ButtonType("Bye!", ButtonBar.ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(btnEnd);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == btnEnd){
			Platform.exit();
			System.exit(0);
		}
	}

	/**
	 * Alerts the player that the column they clicked is full.
	 */
	private void columnFull() {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("ERROR");
		alert.setHeaderText("Look, we need to talk...");
		alert.setContentText("The shiny red button with a big X on it means you can no longer play this column...");

		alert.showAndWait();
	}

	/**
	 * Reusable way to display alerts.
	 * @param header
	 * @param text
	 */
	private void genericError(String header, String text) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("ERROR");
		alert.setHeaderText(header);
		alert.setContentText(text);

		alert.showAndWait();
	}

	private int columnIdToInt(String id) {
		int col = 0;
		switch (id) {
			case "PlayCol0":
				col = 0;
				break;
			case "PlayCol1":
				col = 1;
				break;
			case "PlayCol2":
				col = 2;
				break;
			case "PlayCol3":
				col = 3;
				break;
			case "PlayCol4":
				col = 4;
				break;
			case "PlayCol5":
				col = 5;
				break;
			case "PlayCol6":
				col = 6;
				break;
		}
		System.out.println("Column Clicked: "+col);
		return col;
	}

	/**
	 * Send a move to the clien which sends to the server.
	 * @param id column ID
	 */
	private void sendOnlineMove(String id) {
		int col=columnIdToInt(id);
		try {
			if(client.isGameStarted()) {
				if(client.isMyTurn())
					client.sendMove(col);
				else
					genericError("Hang on", "It's not your turn yet.");
			} else {
				genericError("Just a minute", "The game hasn't started yet.");
			}
		} catch(IOException ex) {
			genericError("EXCEPTION", ex.getMessage());
		}

	}

	/**
	 * This method determines which column was clicked by the player.
	 * @param id id of the ImageView item.
	 */
	private void whichColumnWasClicked(String id) {
		int col=columnIdToInt(id);

		if(!game.columnHasOpenSlots(col)) {
			buzzSound.play();
			buzzSound.seek(Duration.ZERO);
			columnFull();
		} else {
			game.move(game.getActivePlayer(), col);
			playerSound.play();
			playerSound.seek(Duration.ZERO);
			renderBoard();
			try {
				Connect4.GamePiece piece = game.getTopPieceInColumn(col);
				if(game.checkWinConditions(piece)) {
					winSound.play();
					winSound.seek(Duration.ZERO);
					announceWinner(game.getActivePlayer().getName());
				} else {
					game.switchTurns();
					if(!game.multiplayerGame()) {
						int aiMove = ai.move(game);
						game.move(game.getActivePlayer(), aiMove);
						//the AI sound gets really annoying....
						//compSound.play();
						//compSound.seek(Duration.ZERO);
						renderBoard();
						piece = game.getTopPieceInColumn(aiMove);
						if(game.checkWinConditions(piece)) {
							announceWinner(game.getActivePlayer().getName());
						} else {
							game.switchTurns();
						}
					}
				}
				togglePlayerLight();
			} catch (NoSuchElementException ex) {
				announceWinner("EXCEPTION!");
			}
		}
	}

	/**
	 * Renders the board for online play
	 */
	private void renderBoardOnline() {
		togglePlayerLight();
		for(int row=0; row<7; row++) {
			for(int col=0; col<7; col++) {
				if(row==0) {
					if(boardMap[1][col]!=0) {
						try {
							grid.getChildren().remove(board[0][col]);
							grid.getChildren().remove(fullColumn);
							grid.add(fullColumn,col,row);
						} catch (Exception ex) {
							//we really don't care...
						}
					}
				} else {
					if(boardMap[row][col] != 0) {
						//redraw!
						grid.getChildren().remove(board[row][col]);

						if(boardMap[row][col]==1)
							board[row][col].setImage(cellP1);
						else
							board[row][col].setImage(cellP2);

						grid.add(board[row][col],col,row);
					}
				}
			}
		}
		//System.out.println("I am player "+client.whoAmI());
	}


	/**
	 * Draws the board and pieces
	 */
	private void renderBoard() {

		String imgFile;
		for(int col=0; col<7; col++) {
			imgFile = "File:images/btn-play-off.png";
			if(!game.columnHasOpenSlots(col) || (activePlayer.signature.equals("O") && !game.multiplayerGame())) {
				imgFile = "File:images/btn-full.png";
			}
			Image btnImage = new Image(imgFile);
			ImageView btnView = new ImageView(btnImage);
			btnView.setId("PlayCol"+col);
			btnView.getStyleClass().add("colBtnEnabled");
			btnView.setFitHeight(100);
			btnView.setFitWidth(100);
			btnView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
				whichColumnWasClicked(((ImageView)event.getSource()).getId());
			});

			if(game.columnHasOpenSlots(col)) {
				btnView.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
					btnView.setImage(new Image("File:images/btn-play.png"));
				});
				btnView.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
					btnView.setImage(new Image("File:images/btn-play-off.png"));
				});
			}
			grid.add(btnView,col,0);
		}
		for(int row=1; row<=6; row++) {
			for(int col=0; col<7; col++) {
				imgFile = "File:images/btn-off.png";

				if(game.getItemInSlot(row-1,col)!=null) {
					imgFile = game.getItemInSlot(row-1,col).player.signature.equals("X") ? "File:images/btn-yellow.png":"File:images/btn-purple.png";
				}

				Image btnImage = new Image(imgFile);
				ImageView btnView = new ImageView(btnImage);
				btnView.setFitHeight(100);
				btnView.setFitWidth(100);
				this.grid.add(btnView,col,row);
			}
		}
	}

	/**
	 * Toggles the player's turn light on/off
	 */
	private void togglePlayerLight() {
		if(type.equalsIgnoreCase("P")) {
			if(playerNumber>0) {
				//dataLabel.setText("Game Started: "+client.isGameStarted()+"\nCurrent Status: "+client.getStatus());
				if(!client.isGameStarted())
					dataLabel.setText("Waiting for Player 2");
				else {
					dataLabel.setText("Game Started!\nI am player: "+client.whoAmI()+"\nMy Turn? "+client.isMyTurn()+"\nStatus: "+client.getStatus());

					menu.getChildren().remove(p1View);
					menu.getChildren().remove(p2View);

					if(client.isMyTurn()) {
						if(playerNumber==1) {
							p1View.setImage(p1ImageOnCurrent);
							p2View.setImage(p2ImageOff);
						} else {
							p1View.setImage(p1ImageOff);
							p2View.setImage(p2ImageOnCurrent);
						}
					} else {
						if(playerNumber==1) {
							p1View.setImage(p1ImageOffCurrent);
							p2View.setImage(p2ImageOn);
						} else {
							p1View.setImage(p1ImageOn);
							p2View.setImage(p2ImageOffCurrent);
						}
					}

					menu.add(p1View,0,0);
					menu.add(p2View,0,1);
				}
					//menu.getChildren().remove(dataLabel);
			}
		} else {
			if(game.currentlyPlayerOnesTurn()) {
				Image p1Image = new Image("File:images/player-1-on.png");
				p1View.setImage(p1Image);

				Image p2Image = new Image("File:images/player-2-off.png");
				p2View.setImage(p2Image);
			} else {
				Image p1Image = new Image("File:images/player-1-off.png");
				p1View.setImage(p1Image);

				Image p2Image = new Image("File:images/player-2-on.png");
				p2View.setImage(p2Image);
			}
		}

	}


	/**
	 * GUI's Start Method
	 * @param primaryStage Stage that JavaFX passes in
	 */
	@Override // Override the start method in the Application class
	public void start(Stage primaryStage) {
		//create root pane
		rootPane = new StackPane();
		rootPane.setId("root");
		rootPane.setAlignment(Pos.CENTER);


		// Create a grid and set its properties
		grid = new GridPane();
		grid.setId("grid");
		grid.setAlignment(Pos.CENTER_RIGHT);
		grid.setPadding(new Insets(11.5, 12.5, 13.5, 14.5));
		grid.setHgap(5.5);
		grid.setVgap(5.5);


		menu = new GridPane();
		menu.setId("menu");
		menu.setAlignment(Pos.TOP_LEFT);
		menu.setPadding(new Insets(11.5,12.5,13.5,14.5));
		menu.setHgap(5.5);
		menu.setVgap(5.5);

		cellEmpty = new Image("File:images/btn-off.png");
		cellP1 = new Image("File:images/btn-yellow.png");
		cellP2 = new Image("File:images/btn-purple.png");
		btnPlay = new Image("File:images/btn-play.png");
		btnPlayOff = new Image("File:images/btn-play-off.png");
		btnColFull = new Image("File:images/btn-full.png");
		fullColumn = new ImageView(btnColFull);
		fullColumn.setFitWidth(100);
		fullColumn.setFitHeight(100);

		p1ImageOn = new Image("File:images/player-1-on.png");
		p1ImageOff = new Image("File:images/player-1-off.png");
		p1ImageOnCurrent = new Image("File:images/player-1-on-current.png");
		p1ImageOffCurrent = new Image("File:images/player-1-off-current.png");

		p2ImageOn = new Image("File:images/player-2-on.png");
		p2ImageOff = new Image("File:images/player-2-off.png");
		p2ImageOnCurrent = new Image("File:images/player-2-on-current.png");
		p2ImageOffCurrent = new Image("File:images/player-2-off-current.png");


		p1View = new ImageView(p1ImageOff);
		p1View.setFitHeight(125);
		p1View.setFitWidth(450);
		menu.add(p1View,0,0);

		p2View = new ImageView(p2ImageOff);
		p1View.setFitHeight(125);
		p1View.setFitWidth(450);
		menu.add(p2View,0,1);

		dataLabel = new Label("Connecting to server...");
		dataLabel.setTextFill(Color.web("#ffffff"));
		dataLabel.setFont(new Font("Arial", 30));
		menu.add(dataLabel,0,2);


		if(type.equalsIgnoreCase("C")) {
			renderBoard();
		} else {
			board = new ImageView[7][7];
			boardMap = new int[7][7];
			for(int row=0; row<7; row++) {
				for(int col=0; col<7; col++) {
					boardMap[row][col] = 0; //row 0 is technically unused (controls), but we'll set it anyway.
					if(row==0) {
						board[row][col] = new ImageView(btnPlayOff);
						board[row][col].setId("PlayCol"+col);
						board[row][col].getStyleClass().add("colBtnEnabled");
						board[row][col].setFitHeight(100);
						board[row][col].setFitWidth(100);
						board[row][col].addEventHandler(MouseEvent.MOUSE_CLICKED, colClick);

						if(boardMap[1][col]==0) {
							board[0][col].addEventHandler(MouseEvent.MOUSE_ENTERED, colEnter);
							board[0][col].addEventHandler(MouseEvent.MOUSE_EXITED, colExit);
						}
					}
					else {
						board[row][col] = new ImageView(cellEmpty);
						board[row][col].setFitHeight(100);
						board[row][col].setFitWidth(100);
					}
					grid.add(board[row][col],col,row);
				}
			}
			renderBoardOnline();
			launchOnlineGame();
		}

		rootPane.getChildren().addAll(menu,grid);
		// Create a scene and place it in the stage
		//Scene scene = new Scene(grid, 1366,768);
		Scene scene = new Scene(rootPane, 1200,768);
		scene.getStylesheets().addAll(this.getClass().getResource("application.css").toExternalForm());
		primaryStage.setTitle("Connect 4"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
	}
}