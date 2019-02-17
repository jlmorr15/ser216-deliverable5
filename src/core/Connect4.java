package core;

import java.util.NoSuchElementException;
import java.util.Random;

public class Connect4 {
    private GamePiece[][] board;
    private boolean gameRunning;
    private Player[] players;
    private boolean playerOnesTurn;
    private boolean multiplayerGame;

    /**
     * Constructor sets up the board and starts the game.
     * @param gameType P or C
     */
    public Connect4(String gameType) {
        this.board = new GamePiece[6][7];
        this.gameRunning = true;
        if(gameType.equalsIgnoreCase("P")) {
        	multiplayerGame = true;
        	twoPlayerGame(this);
        } else if(gameType.equalsIgnoreCase("C")) {
        	multiplayerGame = false;
        	computerGame(this);
        }
    }
    
    /**
     * Returns true if the game is in progress
     * @return true if game is in progress
     */
    public boolean gameIsActive()
    {
    	return gameRunning;
    }
    
    /**
     * Ends the current game
     */
    public void endGame()
    {
    	gameRunning = false;
    }
    
    /**
     * returns true if the current game is a multiplayer game
     * @return true if the current game is a multiplayer gam
     */
    public boolean multiplayerGame() {
    	return multiplayerGame;
    }
    
    /**
     * Switch active player
     */
    public void switchTurns()
    {
    	playerOnesTurn = !playerOnesTurn;
    }
    
    /**
     * Returns the player who's turn it is.
     * @return The active Player object.
     */
    public Player getActivePlayer() {
    	if(playerOnesTurn) {
    		return players[0];
    	}
    	return players[1];
    }
    
    /**
     * returns the player specified (1 or 2)
     * @param n 1 or 2 (for player one or two)
     * @return Player 1/2 Object
     */
    public Player getPlayer(int n) {
    	if(n-1<0 || n-1>1)
    		throw new NoSuchElementException("Invalid PLayer Number");
    	return players[n-1];
    }
    
    /**
     * returns true if it is player one's turn
     * @return True for player ones turn
     */
    public boolean currentlyPlayerOnesTurn() {
    	return playerOnesTurn;
    }
    
    /**
     * Start a game with AI
     * @param game Game object
     */
    private static void computerGame(Connect4 game) {
    	Player px = game.new Player("Player X", "X");
        Player po = game.new Player("Player O", "O");

        game.players = new Player[2];
        game.players[0] = px;
        game.players[1] = po;
        game.playerOnesTurn = true;
    }
    

    /**
     * Initiate a standard 2 player game
     */
    private static void twoPlayerGame(Connect4 game) {
        Player px = game.new Player("Player X", "X");
        Player po = game.new Player("Player O", "O");

        game.players = new Player[2];
        game.players[0] = px;
        game.players[1] = po;
        game.playerOnesTurn = true;
        
    }

    /**
     * Attempt to move place a game piece in the column for the player.
     * @param player player object the move is for.
     * @param col column number
     * @return Returns true if move was successful
     */
    public boolean move(Player player, int col) {
    	if(col<0 || col>6) {
            return false;
        }
        int row = 0;
        if(this.columnHasOpenSlots(col)) {
            //iterate down until we find a GamePiece or the bottom
            while(row < 5 && getItemInSlot(row+1,col)==null) {
                row++;
            }
            GamePiece piece = new GamePiece(player, row, col);
            this.board[row][col] = piece;
            player.decrementMoves();
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @param col
     * @return
     */
    public boolean columnIsEmpty(int col) {
    	int row=0;
        //iterate down until we find a GamePiece or the bottom
        while(row < 6 && getItemInSlot(row,col)==null) {
        	if(row==5)
        		return true;
            row++;
        }
        return false;
    }
    
    /**
     * returns true if there is no playable spot in the given column
     * @param col
     * @return
     */
    public boolean columnIsFull(int col) throws NoSuchElementException {
    	if(col<0 || col>6)
    		throw new NoSuchElementException("Column doesn't exist.");
    	return getItemInSlot(0,col)!=null;
    }
    
    /**
     * 
     * @param col
     * @return returns the top most piece in a column
     * @throws NoSuchElementException
     */
    public GamePiece getTopPieceInColumn(int col) throws NoSuchElementException {
    	if(!this.columnIsEmpty(col)) {
    		int row=0;
            //iterate down until we find a GamePiece or the bottom
            while(row < 5 && getItemInSlot(row,col)==null) {
                row++;
            }
            return getItemInSlot(row,col);
        } else {
        	throw new NoSuchElementException("Column is empty");
        }
    }
    
    /**
     * returns the column with the best win rating (not implemented)
     * @param game Connect4 object
     * @param p int
     * @return

    public int getBestWinRatedColumn(Connect4 game, int p) {
    	return 0;
    }
    */
    
    /**
     * returns the win rating for a player on a specific column (no implemented)
     * @param game
     * @param player
     * @param col
     * @return

    public int getWinRating(Connect4 game, int player, int col) {
    	int horizontal = 0;
    	int vertical = 0;
    	int diag1 = 0;
    	int diag2 = 0;
    	
    	Player p = game.getPlayer(player);
    	GamePiece pc;
    	int row;
    	int plusOne = 0;
    	if(!game.columnIsEmpty(col)) {
    		try {
    			pc = game.getTopPieceInColumn(col);
    			if(pc==null) {
    				row=5;
    			} else {
    				row = pc.getRow();
    				if(pc.player==p) {
            			plusOne = 1;
            		}
    				horizontal = matchingMovesHorizontal("left",row,col) + matchingMovesHorizontal("right",row,col) + plusOne;
        	    	vertical = matchingMovesVertical(row,col) + plusOne;
        	    	diag1 = matchingMovesDiagonal("DUR",row,col)+matchingMovesDiagonal("DDL",row,col) + plusOne;
        	    	diag2 = matchingMovesDiagonal("DUL",row,col)+matchingMovesDiagonal("DDR",row,col) + plusOne;
    			}
    		} catch (NoSuchElementException ex) {
    			row = 5;
    		}
    	} else {
    		row = 5;
    		horizontal = matchingMovesHorizontal("left",row,col) + matchingMovesHorizontal("right",row,col);
	    	vertical = matchingMovesVertical(row,col);
	    	diag1 = matchingMovesDiagonal("DUR",row,col)+matchingMovesDiagonal("DDL",row,col);
	    	diag2 = matchingMovesDiagonal("DUL",row,col)+matchingMovesDiagonal("DDR",row,col);
    	}
    	
    	return horizontal+vertical+diag1+diag2;
    }

     */

    /**
     * Checks how many continuously matching game pieces are in the given direction.
     * @param direction left or right
     * @param row row to start in
     * @param col column to start in
     * @return Returns the number of consecutive matching pieces in the given direction
     */
    public int matchingMovesHorizontal(String direction, int row, int col) {
        GamePiece origin = getItemInSlot(row,col);
        if(origin==null)
            return 0;
        int o = 1; //offset to walk the board
        int matchingMoves = 0; //number of matching pieces we've found
        int d = direction.equals("left") ? -1:1;
        GamePiece nextSlot;
        do {
            if((col-o>=0 && direction.equals("left")) || (col+o<=6 && direction.equals("right"))) {
                nextSlot = getItemInSlot(row,col+(o*d));
                if(nextSlot != null && nextSlot.player == origin.player) {
                    matchingMoves++;
                }
            } else {
                return matchingMoves;
            }
            o++;
        } while(nextSlot != null && nextSlot.player == origin.player);

        return matchingMoves;
    }

    /**
     * Checks how many continuously matching pieces are below the piece at the given coordinates.
     * @param row The row to check
     * @param col The column to check
     * @return Returns the number of consecutive matching pieces in the given direction
     */
    public int matchingMovesVertical(int row, int col) {
        //since the pieces are dropped from the top we only need to check down the next 3 slots.
        GamePiece origin = getItemInSlot(row,col);
        GamePiece next;
        int matchingMoves = 0;

        for(int i=1; i<4; i++) {
            if(row+i < 6) {
                next = getItemInSlot(row+i,col);
                if(next!=null && next.player==origin.player) {
                    matchingMoves++;
                } else {
                    return matchingMoves;
                }
            }
        }
        return matchingMoves;
    }

    /**
     * Checks how many continuously matching pieces are in the given diagonal direction.
     * @param direction
     * @param row The row to check
     * @param col The column to check
     * @return Returns the number of consecutive matching pieces in the given direction
     */
    public int matchingMovesDiagonal(String direction, int row, int col) {
        GamePiece origin = getItemInSlot(row,col);
        GamePiece next;
        int matchingMoves = 0;
        int dRow=0,dCol=0; //direction modifiers
        // Directions:
        // DUR = Diagonally Up/Right
        // DUL = Diagonally Up/Left
        // DDR = Diagonally Down/Right
        // DDL = Diagonally Down/Left
        switch (direction) {
            case "DUR":
                dRow = -1;
                dCol = 1;
                break;
            case "DUL":
                dRow = -1;
                dCol = -1;
                break;
            case "DDR":
                dRow = 1;
                dCol = 1;
                break;
            case "DDL":
                dRow = 1;
                dCol = -1;
                break;
        }
        int o=1;
        do {
            if(col+(o*dCol)>=0 && col+(o*dCol)<7 && row+(o*dRow)>=0 && row+(o*dRow)<6) {
                next = getItemInSlot(row+(o*dRow),col+(o*dCol));
                if(next != null && next.player == origin.player) {
                    matchingMoves++;
                }
            } else {
                return matchingMoves;
            }
            o++;
        } while(next != null && next.player == origin.player);

        return matchingMoves;
    }


    /**
     * Checks the number of pieces in the various directions and counts them to detect a winning move.
     * @param row The row to check
     * @param col The column to check
     * @return Returns true if the move wins the game.

    public boolean checkWinConditions(int row, int col) {
        GamePiece piece = getItemInSlot(row,col);
        if(piece==null)
            throw new NullPointerException("Nothing in that slot.");

        return checkWinConditions(piece);
    }
    */

    /**
     * 
     * @param piece
     * @return
     */
    public boolean checkWinConditions(GamePiece piece) {
    	int row = piece.getRow();
    	int col = piece.getCol();

        boolean horizontalWin = (matchingMovesHorizontal("left",row,col) + matchingMovesHorizontal("right",row,col) + 1 >= 4);
        boolean verticalWin = (matchingMovesVertical(row,col) + 1 >= 4);
        boolean diagonalWin1 = (matchingMovesDiagonal("DUR",row,col)+matchingMovesDiagonal("DDL",row,col) + 1 >= 4);
        boolean diagonalWin2 = (matchingMovesDiagonal("DUL",row,col)+matchingMovesDiagonal("DDR",row,col) + 1 >= 4);

        //System.out.println("Horizontal Score: "+(matchingMovesHorizontal("left",row,col)+matchingMovesHorizontal("right",row,col) + 1));
        //System.out.println("Vertical Score: "+(matchingMovesDiagonal("DUR",row,col)+matchingMovesDiagonal("DDL",row,col) + 1));
        //System.out.println("Diagonal UR/DL Score: "+(matchingMovesDiagonal("DUR",row,col)+matchingMovesDiagonal("DDL",row,col) + 1));
        //System.out.println("Diagonal UL/DR Score: "+(matchingMovesDiagonal("DUL",row,col)+matchingMovesDiagonal("DDR",row,col) + 1));

        return horizontalWin || verticalWin || diagonalWin1 || diagonalWin2;
    }
    

    /**
     * Returns the item in the given row/column
     * @param row The row to check
     * @param col The column to check
     * @return Returns the GamePiece at the given row/col
     */
    public GamePiece getItemInSlot(int row, int col) {
    	//System.out.println("Row: "+row+" Col: "+col);
        return this.board[row][col];
    }

    /**
     * Returns true if the column isn't full and false if it is.
     * @param col The column to check
     * @return Returns true if column is not full
     */
    public boolean columnHasOpenSlots(int col) {
        return this.board[0][col] == null;
    }


    /**
     * Player Object stores player information.
     * @author Justin Morris
     *
     */
    public class Player {
        private int moves;
        private String name;
        public final String signature;

        /**
         * Instantiates a new player with given name/signature
         * @param name
         * @param signature
         */
        public Player(String name, String signature) {
            this.name = name;
            this.moves = 21;
            this.signature = signature;
        }
        
        public int movesRemaining() {
        	return moves;
        }
        
        public void decrementMoves() {
        	if(moves>0)
        		moves--;
        }
        
        public void setName(String name) {
        	this.name = name;
        }
        
        public String getName() {
        	return name;
        }
    }

    /**
     * GamePiece Object that represents the game piece players use to play the game.
     * @author Justin Morris
     *
     */
    public class GamePiece {
        public final Player player;
        private int row,col;

        /**
         * Instantiates the GamePiece object and assigns it the given player.
         * @param player
         */
        GamePiece(Player player,int row,int col) {
            this.player = player;
            this.row = row;
            this.col = col;
        }
        
        /**
         * Gets the row of the piece
         * @return
         */
        public int getRow() {
        	return row;
        }
        
        /**
         * Gets the column of the piece
         * @return
         */
        public int getCol() {
        	return col;
        }
    }
}
