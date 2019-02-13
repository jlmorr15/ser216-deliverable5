package core;

import java.util.Random;

public class Connect4ComputerPlayer {
	public String name;
	private Connect4.Player player;
	private int difficulty;
	
	/**
	 * Constructor creates AI player, names it, and associates it with a Player object
	 * @param player
	 */
	public Connect4ComputerPlayer(Connect4.Player player) {
		String[] names = {"Charles", "Becky", "Jeeves", "Ray", "Sally", "Jess", "Richard"};
		Random r = new Random();
		this.name = "AI-"+names[r.nextInt(6)];
		this.difficulty = 1;
		
		player.setName(this.name);
		System.out.println(this.name+" has joined the game. Good luck!");
	}
	
	/**
	 * returns the AI's column choice (currently locked at level 1: random moves)
	 * @param game
	 * @return
	 */
	public int move(Connect4 game) {
		int col;
		Random r = new Random();
		switch(this.difficulty) {
		case 1:	
			// blindly return any column 1-7
			col = r.nextInt(6)+1;
			break;
		case 2:
			col = game.getBestWinRatedColumn(game,2);
			break;
		default:
			col = r.nextInt(6)+1; // blindly return any column 1-7
		}
		return col;
	}
	
	
}
