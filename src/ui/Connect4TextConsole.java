package ui;

import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import core.Connect4;
import core.Connect4ComputerPlayer;

public class Connect4TextConsole {

    private Connect4 game;
    private Scanner s;
    private Connect4ComputerPlayer ai;

    /**
     * Connect4TextConsole Instantiates the UI and assigns it the given Connect4 game object 
     * as well as instantiates the Scanner for collecting user input.
     */
    public Connect4TextConsole() {
        s = new Scanner(System.in); // prepare for input
        String option;
		do {
			option = displayMenu();
		} while(!option.equalsIgnoreCase("P") && !option.equalsIgnoreCase("C"));
        this.game = new Connect4(option); //start a new game.
        if(!game.multiplayerGame()) {
        	this.ai = new Connect4ComputerPlayer(game.getPlayer(2));
        }
    }
    
    /**
     * Main method starts the UI and initiates a game or tests.
     * @param args Args will control stuff like difficulty in the future.
     */
    public static void main(String[] args) {    	
    	Connect4TextConsole ui = new Connect4TextConsole();
		int move=0;
        do {
        	Connect4.Player activePlayer = ui.game.getActivePlayer();
            do {
                ui.renderBoard();
                if(activePlayer.movesRemaining()<1) {
                    ui.game.endGame();
                    System.out.println("Game Over! The game ended in a DRAW!");
                    break;
                } else {
                    move = ui.promptPlayer(activePlayer);
                    if(!ui.game.multiplayerGame() && !ui.game.currentlyPlayerOnesTurn())
                    	System.out.println(activePlayer.getName()+" is attempting to play column "+move);
                    if(move<0 || move>6) {
                        System.out.println("Please don't throw pieces on the floor... Not cool.");
                    }
                }
            } while(!ui.game.move(activePlayer,move-1));
            try {
            	Connect4.GamePiece piece = ui.game.getTopPieceInColumn(move-1);
            	if(ui.game.checkWinConditions(piece)) {
                    System.out.println("WINNER WINNER CHICKEN DINNER! "+activePlayer.getName()+" WINS! Final Move: ["+(piece.getRow()+1)+", "+(piece.getCol()+1)+"]");
                    ui.game.endGame();
                    ui.renderBoard();
                }
            } catch (NoSuchElementException ex) {
            	//ignore.
            }
            ui.game.switchTurns();
        } while(ui.game.gameIsActive());
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
     * Displays the main menu text and gathers input.
     * @return
     */
    private String displayMenu() {
    	System.out.println("Begin Game. Enter 'P' if you want to play against another player; enter 'C' to play against the computer.");
    	return s.next();
    }


    /**
     * Ask player to enter a column number between 1 and 7
     * @param player Instantiated core.Connect4.Player object
     * @return Returns the players integer selection.
     */
    private int promptPlayer(Connect4.Player player) {
    	if(!game.multiplayerGame() && !game.currentlyPlayerOnesTurn()) {
    		return ai.move(this.game);
    	}
        int output=0;
        System.out.println(player.getName()+" it's your move. Enter a number between 1 and 7 for the column you wish to play.");
    	do {
            try {
                output = s.nextInt();
            } catch (InputMismatchException ex) {
                System.out.println("Invalid value, please enter a value from 1 to 7: ");
                s.next();
            }
        } while(output<1 || output>7);
    	return output;
    }

    /**
     * Draw the game board to the console.
     */
    private void renderBoard() {
        System.out.println(" 1 2 3 4 5 6 7");
        for(int row=0; row<6; row++) {
            for(int col=0; col<7; col++) {
                System.out.print("|" + (game.getItemInSlot(row,col)==null ? " ":game.getItemInSlot(row,col).player.signature));
            }
            System.out.print("|\n");
        }
    }



}
